package test.helper;


import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

import org.junit.Assert;
import org.junit.Test;

import flca.mda.codegen.helpers.FileHelper;
import flca.mda.codegen.helpers.ZipFileHelper;
import flca.mda.codegen.helpers.ShellUtils;

public class TestFilesHelper {

	@Test
	public void testGenerate() {
		try {
			ZipFileHelper fileshelper = new ZipFileHelper(getZipFile(), makeFromTos(), getTargetDir());
			fileshelper.doGenerate();
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	private ZipFile getZipFile() throws Exception {
		URL zip = FileHelper.getExistingUrl("/testzip.zip");
		return new ZipFile(zip.getFile());
	}
	
	private Map<String, String> makeFromTos() {
		Map<String, String> r = new HashMap<String, String>();
		
		r.put("APPNAME", "demo");
		
		return r;
	}
	
	private File getTargetDir() {
		if (ShellUtils.isLinux()) {
			return new File("/tmp");
		} else {
			return new File("c:/tmp");
		}
	}
}
