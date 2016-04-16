package test;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;

import test.mock.MockJavaProject;
import test.mock.MockPath;
import test.mock.MockProject;

import com.flca.mda.codegen.helpers.CartridgeHelper;
import com.flca.mda.codegen.helpers.ClassloaderHelper;
import com.flca.mda.codegen.helpers.PluginHelper;
import com.flca.mda.codegen.helpers.ProjectInstanceHelper;
import com.flca.mda.codegen.ui.WizzardMain;

import flca.mda.codegen.CodegenConstants;
import flca.mda.codegen.data.DataStore;
import flca.mda.codegen.data.SubsValue;
import flca.mda.codegen.helpers.ShellUtils;

public class TestCodegenWizzard {
	public static void main(String args[]) {
		System.setProperty(CodegenConstants.JUNIT_TEST, "true");
		PluginHelper.getInstance().setJunitTestPluginRootdir(new File(cartridgeRootdir()));
		// IJavaProject templateProject = makeJavaProject(templateRootdir());
		IJavaProject modelProject = makeJavaProject(modelRootdir());

		List<String> selectedFiles = new ArrayList<String>();
		selectedFiles.add("flca.demo.dto.TstDto");
		ProjectInstanceHelper.getInstance().setModelProject(modelProject, selectedFiles);

		ClassLoader clsloader = ClassloaderHelper.getInstance().setupCurrentClassloader();
		DataStore.getInstance().readSavedSubsValues(
				new File(ProjectInstanceHelper.getInstance().getCurrentProjectLocation()));

		CartridgeHelper.getInstance().initialize(CartridgeHelper.getInstance().getAllCartridgeClassPaths());
		addWebAppSubsValues(clsloader);

		WizzardMain wizzard = new WizzardMain(null);
		wizzard.setBlockOnOpen(true);
		wizzard.open();
	}

	@SuppressWarnings("unchecked")
	private static void addWebAppSubsValues(ClassLoader clsloader) {
		try {
			Class<?> regclz = clsloader.loadClass("webapp.RegisterTemplates");
			Object obj = regclz.newInstance();
			Method m = regclz.getMethod("getSubstituteValues", new Class<?>[] {});
			Object result = m.invoke(obj, new Object[] {});
			Collection<SubsValue> subsvalues = (Collection<SubsValue>) result;
			DataStore.getInstance().mergeSubsValues(subsvalues, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static IJavaProject makeJavaProject(String aRootdir) {
		IPath path = new MockPath(aRootdir);
		IProject project = new MockProject(path);
		IJavaProject result = new MockJavaProject(project);
		return result;
	}

	private static String MODEL_DIR = "flca.mda.test.model";
	private static String PLUGIN_DIR = "flca.mda.generator";
	
	private static String modelRootdir() {
		return getProjectDir() + "/" + MODEL_DIR;
	}

	private static String cartridgeRootdir() {
		return getProjectDir() + "/" + PLUGIN_DIR;
	}

	private static String getProjectDir() {
		String currentDir = System.getProperty("user.dir");
		return getParentDir(new File(currentDir)).getAbsolutePath();
	}

	private static File getParentDir(File aFile) {
		String filename = aFile.getAbsolutePath();
		String sep = ShellUtils.getPathDelim();
		if (filename.indexOf(sep) >= 0) {
			if (filename.endsWith(sep)) {
				filename = filename.substring(0, filename.length() - 1);
			}
			return new File(filename.substring(0, filename.lastIndexOf(sep)));
		} else {
			return aFile;
		}
	}

}
