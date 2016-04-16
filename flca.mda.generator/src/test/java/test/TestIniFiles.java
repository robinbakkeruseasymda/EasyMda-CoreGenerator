package test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import flca.mda.codegen.CodegenConstants;
import flca.mda.codegen.data.DataStore;
import flca.mda.codegen.helpers.IniFileHelper;

public class TestIniFiles {
	@BeforeClass
	public static void beforeEach() {
		System.setProperty(CodegenConstants.JUNIT_TEST, "true");
	}

	@Test
	public void ctorIniFileHelper() {
		IniFileHelper.initialize(getRootdir());
	}

	@Test
	public void readInifile() {
		IniFileHelper.initialize(getRootdir());
		String r = DataStore.getInstance().getSectionValue("java.default.value", "float");
		Assert.assertTrue("0".equals(r));
	}

	private static final String MULTI_LINE_START = "(\\S*)(\\s*)(=\\s*\\{\\s*)$";
	private static final String MULTI_LINE_END = "^\\s*}\\s*$";

	@Test
	public void testRegex() {
		String s = "test =  { ";
		Boolean b = s.matches(MULTI_LINE_START);
		Assert.assertTrue(s, b);

		s = "test =  {";
		b = s.matches(MULTI_LINE_START);
		Assert.assertTrue(s, b);

		s = "* = {";
		b = s.matches(MULTI_LINE_START);
		Assert.assertTrue(s, b);

		s = "}";
		b = s.matches(MULTI_LINE_END);
		Assert.assertTrue(b);

		s = "} ";
		b = s.matches(MULTI_LINE_END);
		Assert.assertTrue(b);

		s = "}  ";
		b = s.matches(MULTI_LINE_END);
		Assert.assertTrue(b);

	}

	private File getRootdir() {
		return getClasspathBinDir();
	}

	public File getClasspathBinDir() {
		try {
			String path = new File(".").getCanonicalPath();
			URLClassLoader cl = (URLClassLoader) ClassLoader.getSystemClassLoader();
			URL urls[] = cl.getURLs();
			for (URL url : urls) {
				String fname = url.getFile();
				if (fname.indexOf(path) >= 0 && fname.indexOf(".jar") < 0) {
					return new File(fname);
				}
			}
			throw new RuntimeException("no bin path found");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
