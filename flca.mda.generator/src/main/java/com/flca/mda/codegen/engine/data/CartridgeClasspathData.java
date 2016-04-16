package com.flca.mda.codegen.engine.data;

import java.io.File;

import flca.mda.codegen.helpers.ShellUtils;

public class CartridgeClasspathData {

	private String key;
	private File file;
	private boolean enabled;

	public CartridgeClasspathData(File file, boolean enabled) {
		super();
		this.file = file;
		this.enabled = enabled;
		this.key = getClasspathKey(file);
	}

	public String getKey() {
		return key;
	}


	public void setKey(String key) {
		this.key = key;
	}


	public File getFile() {
		return file;
	}


	public void setFile(File file) {
		this.file = file;
	}


	public boolean isEnabled() {
		return enabled;
	}


	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}


	/**
	 * given the tha File, return the corresponding String key used in the cartridgeClassPaths Map.
	 * for a jar-file this is jat filename.
	 * for a bin, it is the name of the parent folder. (TODO this is not good if the bin folder is something like target/classes. 
	 * So ii should be imporoved by checking if the parent folder has a .project file)
	 * @param aFile
	 * @return
	 */
	public static String getClasspathKey(File aFile) {
		if (aFile.getAbsolutePath().toLowerCase().endsWith(".jar")) {
			return aFile.getName();
		} else {
			showTargetFile(aFile);
			return new File(aFile.getParent()).getName() + ShellUtils.getPathDelim() + aFile.getName();
		}
	}
	
	
	private static void showTargetFile(File aFile) {
		if (!aFile.exists()) {
			System.err.println("Cannot find the cartrigde bin folder " + aFile);
		}
	}
}
