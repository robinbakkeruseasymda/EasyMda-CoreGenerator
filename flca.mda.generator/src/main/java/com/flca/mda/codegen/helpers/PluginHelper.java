package com.flca.mda.codegen.helpers;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Platform;

import com.flca.mda.codegen.Activator;

import flca.mda.codegen.CodegenConstants;
import flca.mda.codegen.helpers.ShellUtils;

public class PluginHelper {
	private static PluginHelper sInstance;

	private File junitTestPluginRootdir;

	private File uri = null;

	private PluginHelper() {
	}

	public static PluginHelper getInstance() {
		if (sInstance == null) {
			sInstance = new PluginHelper();
		}
		return sInstance;
	}

	public File getJunitTestPluginRootdir() {
		return junitTestPluginRootdir;
	}

	public void setJunitTestPluginRootdir(File junitTestPluginRootdir) {
		this.junitTestPluginRootdir = junitTestPluginRootdir;
	}

	@SuppressWarnings("deprecation")
	private File getPluginFolder() {
		URL url = Platform.getBundle(Activator.PLUGIN_ID).getEntry("/");
		try {
			url = Platform.resolve(url);
			return new File(url.getPath());
		} catch (Exception ex) {
			LogHelper.error("error in getPluginFolder " + url, ex);
			return null;
		}
	}

	public File getRootDir() {
		if (uri == null) {
			if (ShellUtils.isJunitTest()) {
				uri = getJunitTestPluginRootdir();
			} else {
				uri = getPluginFolder();
			}
		}

		if (uri == null) {
			LogHelper.error("plugin uri = null");
		}
		return uri;
	}

	public File getTemplatesConfigDir() {
		return new File(getRootDir() + "/" + CodegenConstants.PLUGIN_CONFIG);
	}

	public File getCartridgeDir() {
		return new File(getRootDir() + "/" + CodegenConstants.PLUGIN_CARTRIDGES);
	}

	public File getApilibDir() {
		return new File(getRootDir() + "/" + CodegenConstants.PLUGIN_API_LIB);
	}
}
