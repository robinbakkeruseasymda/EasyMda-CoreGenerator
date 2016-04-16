package com.flca.mda.codegen.helpers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

public class ClassloaderHelper {
	private static ClassloaderHelper sInstance;

	// this property should be setup with setupCurrentClassloader()
	private ClassLoader loader;

	public static ClassloaderHelper getInstance() {
		if (sInstance == null) {
			sInstance = new ClassloaderHelper();
		}
		return sInstance;
	}

	private ClassloaderHelper() {
	}

	public ClassLoader getClassLoader() {
		return loader;
	}

	/***
	 * This will setup the ClassLoader to be used by instantiating the generator
	 * and model class. It is based on the current active (plugin) classloader
	 * plus the bin-dirs of all jet project with the jet-templates plus the bin
	 * dir of the current select java project.
	 * 
	 * @param aProject
	 * @return
	 * @throws JavaModelException
	 * @throws MalformedURLException
	 */
	public ClassLoader setupCurrentClassloader() {
		LogHelper.debug("setupCurrentClassloader");
		try {
			Collection<URL> cpUrls = CartridgeHelper.getInstance().getCartridgesClasspathUrls();
			cpUrls.addAll(CartridgeHelper.getInstance().getApilibsClasspathUrls());
			cpUrls.add(getProjectBindir());
			URL[] urls = (URL[]) cpUrls.toArray(new URL[cpUrls.size()]);

			loader = new URLClassLoader(urls, this.getClass().getClassLoader());
			return loader;
		} catch (Exception e) {
			LogHelper.error("error setupCurrentClassloader ", e);
			throw new RuntimeException(e);
		}
	}

	private URL getProjectBindir() throws MalformedURLException, JavaModelException {
		IJavaProject proj = ProjectInstanceHelper.getInstance().getCurrentProject();
		String rootdir = proj.getProject().getLocation().toPortableString();
		return new File(rootdir + "/bin").toURI().toURL();
	}
}
