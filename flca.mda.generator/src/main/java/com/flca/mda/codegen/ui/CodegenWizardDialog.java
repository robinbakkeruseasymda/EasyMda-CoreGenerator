package com.flca.mda.codegen.ui;

import java.io.File;
import java.util.Collection;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import com.flca.mda.codegen.engine.data.CartridgeClasspathData;
import com.flca.mda.codegen.helpers.CartridgeHelper;
import com.flca.mda.codegen.helpers.ClassloaderHelper;
import com.flca.mda.codegen.helpers.LogHelper;
import com.flca.mda.codegen.helpers.PluginHelper;
import com.flca.mda.codegen.helpers.ProjectInstanceHelper;
import com.flca.mda.codegen.ui.model.TemplatesProvider;

import flca.mda.codegen.data.DataStore;
import flca.mda.codegen.data.SubsValue;
import flca.mda.codegen.data.TemplatesStore;
import flca.mda.codegen.helpers.IniFileHelper;

public class CodegenWizardDialog extends WizardDialog {
	private IWizard wizard;
	private boolean firstTime = true;

	public CodegenWizardDialog(Shell parentShell, IWizard wizard) {
		super(parentShell, wizard);
		this.wizard = wizard;
	}

	@Override
	protected void finishPressed() {
		super.finishPressed();
	}

	@Override
	public void updateButtons() {
		super.updateButtons();
	}

	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
		if (buttonId == 15) {
			IWizardPage pages[] = wizard.getPages();
			Object page = getSelectedPage();
			if ("p2".equals(page.toString())) {
				setupEnvironment();
				SelectTemplatesPage p2 = (SelectTemplatesPage) pages[1];
				p2.update();
			} else if ("p3".equals(page.toString())) {
				ShowSetupDataPage p3 = (ShowSetupDataPage) pages[2];
				p3.update();
			}
		}
	}

	/**
	 * At this point after the user selected the classpaths with jet templates,
	 * everything is in place, and we can now prepare the codegen environment.
	 */
	private void setupEnvironment() {
		if (firstTime) {
			try {
				prepareHelpers();
				mergeIniFilesFromTemplateAndModel();
				cloneStores();
			} catch (Throwable t) {
				LogHelper.error("error in setupEnvironment ", t);
			}
		}
	}

	/**
	 * This method is called just before the wizard is started. At this point we
	 * know the selected model project and we have all the classpath of the
	 * cartridge projects Here the environment is setup.
	 * 
	 * @param modelProject
	 * @param fqClassnames
	 * @throws JavaModelException
	 */
	private void prepareHelpers() {
		// setup generator environment
		try {
			IniFileHelper.initialize(PluginHelper.getInstance().getRootDir());

			CartridgeHelper.getInstance().initialize(CartridgeHelper.getInstance().getEnabledCartridgeClassPaths());
			Collection<SubsValue> subsvals = CartridgeHelper.getInstance().getSubsValues();
			DataStore.getInstance().mergeSubsValues(subsvals, false);
			DataStore.getInstance().readSavedSubsValues(new File(ProjectInstanceHelper.getInstance().getCurrentProjectLocation()));

			ProjectInstanceHelper.getInstance().refresh();
			TemplatesProvider.getInstance().reset();

		} catch (Exception e) {
			LogHelper.error("error processClasses " + e);
			throw new RuntimeException(e);
		}
	}
	
	private void mergeIniFilesFromTemplateAndModel() {
		for (CartridgeClasspathData data : CartridgeHelper.getInstance().getEnabledCartridgeClassPaths()) {
			IniFileHelper.merge(data.getFile());
		}
		String modelDir = ProjectInstanceHelper.getInstance().getCurrentProjectLocation();
		IniFileHelper.merge(new File(modelDir));
	}

	/**
	 * This methods clones sereval stores so that also available for the api's
	 */
	private void cloneStores() {
		try {
			ClassLoader loader = ClassloaderHelper.getInstance().getClassLoader();

			DataStore.getInstance().cloneForApi(loader);
			TemplatesStore.getInstance().cloneForApi(loader);
		} catch (Exception e) {
			LogHelper.error("error in cloneStores", e);
		}
	}
}