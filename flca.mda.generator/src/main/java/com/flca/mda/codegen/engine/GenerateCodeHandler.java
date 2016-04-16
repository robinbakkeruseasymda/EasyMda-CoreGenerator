package com.flca.mda.codegen.engine;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flca.mda.codegen.helpers.CartridgeHelper;
import com.flca.mda.codegen.helpers.LogHelper;
import com.flca.mda.codegen.helpers.ProjectInstanceHelper;
import com.flca.mda.codegen.ui.CodegenWizardDialog;
import com.flca.mda.codegen.ui.CodegenWizzard;

import flca.mda.codegen.CodegenConstants;
import flca.mda.codegen.data.DataStore;
import flca.mda.codegen.data.SubsValue;
import flca.mda.codegen.data.SubsValueType;
import flca.mda.codegen.helpers.IniFileHelper;
import flca.mda.codegen.helpers.IniFileSection;
import flca.mda.codegen.helpers.SubClassesHelper;

public class GenerateCodeHandler extends AbstractHandler {
	
	private IJavaProject modelProject;
	private List<String> fqClassnames;
	private Logger logger = LoggerFactory.getLogger(GenerateCodeHandler.class);
	
	public GenerateCodeHandler() {
		modelProject = null;
		fqClassnames = new ArrayList<String>();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		LogHelper.initialize();
		showCurrentClasspath();
		modelProject = null;
		fqClassnames.clear();

		collectSelectedClasses(event);
		runGenerator(event);

		return null;
	}

	private void runGenerator(ExecutionEvent event) {
		// trigger the actual generators, after the wizard is finished
		if (fqClassnames.size() > 0) {
			prepare();
			if (showWizzard(event)) {
				try {
					RunAction action = new RunAction();
					action.run();
				} catch (Throwable t) {
					LogHelper.error("error in generatecodehandler ", t);
				}
			}
		}
	}

	private void collectSelectedClasses(ExecutionEvent event) {
		// collect data over the selected classes or packages
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getActiveMenuSelection(event);
		Iterator<?> selectionIter = selection.iterator();
		while (selectionIter.hasNext()) {
			Object selcElem = selectionIter.next();
			if (selcElem instanceof ICompilationUnit) {
				processClasses(selection);
			} else if (selcElem instanceof IPackageFragment) {
				processPackage((IPackageFragment) selcElem);
			} else {
				MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Information",
						"Please select a Java source file or package");
			}
		}
	}

	private void showCurrentClasspath() {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		 
		logger.info("crrent classpath = ");
        for(URL url: ((URLClassLoader)cl).getURLs()){
        	logger.info(url.getFile());
        }
        
        //logger.info("plugindir = " + PluginHelper.getInstance().getRootDir());
	}
	/**
	 * This method is called just before the wizard is started. At this point we
	 * only have to retrieve the (saved) settings from easymda-codegen.values to
	 * find out which cartridge(s) are enabled. Note if corresponding setting is
	 * not there, then the cartridge is enabled
	 * 
	 * @throws JavaModelException
	 */
	private void prepare() {
		ProjectInstanceHelper.getInstance().setModelProject(modelProject, fqClassnames);
		DataStore.getInstance().reset();
		readCartridgesStatusValues(getModelDir());
		storeModelDir();
		SubClassesHelper.initialize();
	}

	private File getModelDir() {
		return new File(ProjectInstanceHelper.getInstance().getCurrentProjectLocation());
	}

	private void storeModelDir() {
		SubsValue subsval = new SubsValue(CodegenConstants.DATASTORE_MODEL_DIR, getModelDir().getAbsolutePath());
		subsval.setType(SubsValueType.NONE);
		DataStore.getInstance().addSubsValue(subsval);
	}
	
	private void processPackage(IPackageFragment pckfragment) {
		try {
			IJavaElement classfiles[] = pckfragment.getChildren();
			if (modelProject == null) {
				modelProject = pckfragment.getJavaProject();
			}

			for (IJavaElement javaelem : classfiles) {
				String pck = pckfragment.getElementName();
				if (pck.length() > 0) {
					pck += ".";
				}
				String clsname = pck + javaelem.getElementName();
				fqClassnames.add(clsname.substring(0, clsname.length() - 5));
			}
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
	}

	private void processClasses(IStructuredSelection selection) {
		Iterator<?> sourcesIter = selection.iterator();

		while (sourcesIter.hasNext()) {
			Object obj = sourcesIter.next();
			String clsname = null;
			try {
				ICompilationUnit cu = (ICompilationUnit) obj;
				String pck = (cu.getPackageDeclarations().length > 0) ? cu.getPackageDeclarations()[0].getElementName()
						+ "." : "";
				String name = cu.getElementName();
				clsname = pck + name.substring(0, name.length() - 5); // get rid of
																						// .java
				fqClassnames.add(clsname);
				if (modelProject == null) {
					modelProject = cu.getJavaProject();
				}
			} catch (JavaModelException e) {
				LogHelper.error("error processClasses : " + clsname, e);
			}
		}
	}

	private boolean showWizzard(ExecutionEvent event) {
		IWizard wizard = new CodegenWizzard();
		CodegenWizardDialog dialog = new CodegenWizardDialog(HandlerUtil.getActiveShell(event), wizard);
		dialog.create();
		int n = dialog.open();
		return n == 0;
	}

	private static class RunAction extends Action {
		public RunAction() {
		}

		protected void dialog() {
			MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Generate " + getText(), "Generate "
					+ " completed.\n" + "See the console for trace information and errors/exceptions.");
		}

		@Override
		public void run() {
			Engine engine = new Engine();
			engine.generate();
			dialog();
		}
	}

	/**
	 * This method will parse the easymda.ini file and make SubsVals
	 * for all keys under [use.cartridges] This will be used on wizpage-1 to
	 * en/dis-able the cartridge checkbox
	 * 
	 * @param aModelProjectDir
	 */
	public void readCartridgesStatusValues(File aModelProjectDir) {
		Map<String, IniFileSection> sectmap = IniFileHelper.parseIniFiles(getModelDir(), new PrefsFileFilter(), false);
		if (sectmap != null) {
			if (sectmap.containsKey(CodegenConstants.PREFERENCES_TEMPLATES_SECTION)) {
				IniFileSection sect = sectmap.get(CodegenConstants.PREFERENCES_TEMPLATES_SECTION);
				for (String key : sect.getMap().keySet()) {
					String value = sect.getValue(key);
					Boolean b = (value != null && value.toLowerCase().startsWith("f")) ? Boolean.FALSE : Boolean.TRUE;
					CartridgeHelper.getInstance().setCartridgeEnableStatus(key, b);
				}
			}
		}
	}

	// --------
	static class PrefsFileFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			return pathname.getPath().endsWith(CodegenConstants.PREFERENCES_VALUES_FNAME);
		}
	}
}
