package flca.test.base;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.mock.MockJavaProject;
import test.mock.MockMonitor;
import test.mock.MockPath;
import test.mock.MockProject;

import com.flca.mda.codegen.engine.TemplateProcessor;
import com.flca.mda.codegen.engine.data.CartridgeClasspathData;
import com.flca.mda.codegen.helpers.CartridgeHelper;
import com.flca.mda.codegen.helpers.ClassloaderHelper;
import com.flca.mda.codegen.helpers.LogHelper;
import com.flca.mda.codegen.helpers.PluginHelper;
import com.flca.mda.codegen.helpers.ProjectInstanceHelper;
import com.flca.mda.codegen.helpers.SimpleClibboardHelper;

import flca.mda.codegen.CodegenConstants;
import flca.mda.codegen.data.DataStore;
import flca.mda.codegen.data.ITemplate;
import flca.mda.codegen.data.SubsValue;
import flca.mda.codegen.data.TemplatesStore;
import flca.mda.codegen.helpers.AdditionalGeneratesHelper;
import flca.mda.codegen.helpers.IniFileHelper;
import flca.mda.codegen.helpers.ShellUtils;
import flca.mda.codegen.helpers.SubClassesHelper;

/**
 * Abstract base class that can be user flca.mda.test.xxxx project to test generate templates.
 * @author ed45064
 *
 */
public abstract class AbstractTestTemplates {
	protected static Logger logger = LoggerFactory.getLogger(AbstractTestTemplates.class);

//	private static TestTemplatesData providedData;
	protected static List<String> selectedClassnames = new ArrayList<String>();

	private static ClassLoader sLoader;
//	private static SaveGeneratedCodeHelper saver;
	protected static int startLogbackErrorCount;

	private static TemplateProcessor tp; 

	
	protected static void beforeOnceBase(AbstractTestTemplatesData aProvidedData) {
		try {
			System.setProperty(CodegenConstants.JUNIT_TEST, "true");
//			System.setProperty(CodegenConstants.OVERWRITE_WITHOUT_MERGING, "true");

			IJavaProject modelProject = makeJavaProject(aProvidedData.getModelDir().getPath());
			PluginHelper.getInstance().setJunitTestPluginRootdir(aProvidedData.getPluginDir());

			ProjectInstanceHelper.getInstance().setModelProject(modelProject, selectedClassnames);
			CartridgeHelper.getInstance().initialize(CartridgeHelper.getInstance().getAllCartridgeClassPaths());
			ProjectInstanceHelper.getInstance().refresh();
			sLoader = ClassloaderHelper.getInstance().getClassLoader();

			DataStore.getInstance().readSavedSubsValues(new File(ProjectInstanceHelper.getInstance().getCurrentProjectLocation()));
			
			SubClassesHelper.initialize();

			IniFileHelper.initialize(aProvidedData.getPluginDir());

			mergeIniFilesFromTemplateAndModel();

			setupSubsValues(aProvidedData);

			DataStore.getInstance().cloneForApi(sLoader);
			TemplatesStore.getInstance().cloneForApi(sLoader);

			IJavaProject modelproj = makeJavaProject(aProvidedData.getModelDir().getPath());
//			saver = new SaveGeneratedCodeHelper(modelproj, new MockMonitor());
			tp = new TemplateProcessor(modelproj, new MockMonitor());
			
			LogHelper.initialize();
			startLogbackErrorCount = LogHelper.getLogbackErrorCount();
		} catch (Throwable e) {
			LogHelper.error("error in beforeOnceBase", e);
			Assert.fail("error during startup " + e.getMessage());
		}
	}
	

	private static void setupSubsValues(AbstractTestTemplatesData aProvidedData) {
		// local overwrites (values are set in super class
		for (SubsValue subsval : aProvidedData.getSubsvalues()) {
			DataStore.getInstance().mergeSubsValue(subsval, true);
		}

		Collection<SubsValue> subsvalues = CartridgeHelper.getInstance().getSubsValues();
		DataStore.getInstance().mergeSubsValues(subsvalues, false);
		LogHelper.info(DataStore.getInstance().getSubsvalues().toString());
	}

	private static void mergeIniFilesFromTemplateAndModel() {
		for (CartridgeClasspathData data : CartridgeHelper.getInstance().getAllCartridgeClassPaths()) {
			IniFileHelper.merge(data.getFile());
		}
		String modelDir = ProjectInstanceHelper.getInstance().getCurrentProjectLocation();
		IniFileHelper.merge(new File(modelDir));
	}

	// --- helper methods
	protected static String getProjectDir() {
		String currentDir = System.getProperty("user.dir");
		return new File(currentDir).getParent();
	}

	protected static IJavaProject makeJavaProject(String aRootdir) {
		IPath path = new MockPath(aRootdir);
		IProject project = new MockProject(path);
		IJavaProject result = new MockJavaProject(project);
		return result;
	}

	protected String generate(Class<?> aClass, ITemplate aTemplate) {
		try {
			AdditionalGeneratesHelper.reset();
			String result = tp.generate(aClass, aTemplate, null, false);
			generateAdditionClasses();
			if (result != null) {
				new SimpleClibboardHelper().save(result);
				return result;
			} else {
				return null;
			}
		} catch(Throwable t) {
			System.out.println("error generating " + aClass + " with " + aTemplate.getName());
			org.junit.Assert.fail();
			return null;
		}
	}
	
	
	private void generateAdditionClasses()	{
		for (int i=0; i < AdditionalGeneratesHelper.size(); i++) {
			tp.generate(AdditionalGeneratesHelper.getClass(i), 
					AdditionalGeneratesHelper.getTemplate(i), 
					AdditionalGeneratesHelper.getArguments(i), true);
		}
	}
	
	// --- inner file-filter classes

	class FindFileFilter implements FileFilter {
		private String lookfor;

		public FindFileFilter(Class<?> clazz) {
			lookfor = clazz.getName().replace(".", ShellUtils.getPathDelim()) + ".java";
		}

		@Override
		public boolean accept(File pathname) {
			// System.out.println(pathname.getPath());
			return pathname.isDirectory() || pathname.getPath().endsWith(lookfor);
		}
	}

	class FindGeneratorFilter implements FileFilter {
		private String lookfor;

		public FindGeneratorFilter(ITemplate aTemplate) {
			lookfor = aTemplate.getGeneratorFqn().replace(".", ShellUtils.getPathDelim()) + ".class";
		}

		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory() || pathname.getPath().endsWith(lookfor);
		}
	}

}
