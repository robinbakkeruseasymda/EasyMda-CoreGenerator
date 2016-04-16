package flca.mda.codegen.helpers;

import java.io.File;

import flca.mda.codegen.CodegenConstants;

public class ShellUtils {
	public static String getOs() {
		String result = System.getProperty("os.name");
		if (result.indexOf(" ") > 0) {
			result = result.substring(0, result.indexOf(" "));
		}
		return result;
	}

	public static boolean isWindows() {
		return getOs().toLowerCase().startsWith("win");
	}

	public static boolean isLinux() {
		return getOs().toLowerCase().startsWith("linux");
	}

	public static String getUserDir() {
		return System.getProperty("user.dir");
	}

	public static String getPathDelim() {
		if (isWindows()) {
			return "\\";
		} else {
			return "/";
		}
	}
	
	public static File getTempDir() {
		 return new File(System.getProperty("java.io.tmpdir"));
	}

	public static boolean isJunitTest() {
		String s = System.getProperty(CodegenConstants.JUNIT_TEST);
		return (s != null && s.trim().toLowerCase().equals("true"));
	}

	public static boolean forceOverwriteWithoutMerging() {
		String s = System.getProperty(CodegenConstants.OVERWRITE_WITHOUT_MERGING);
		return (s != null && s.trim().toLowerCase().equals("true"));
	}
}
