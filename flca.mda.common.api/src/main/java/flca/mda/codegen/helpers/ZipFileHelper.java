package flca.mda.codegen.helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import flca.mda.codegen.CodegenConstants;
import flca.mda.codegen.data.DataStore;

/**
 * this class will actually execute the copy and substitute actions
 * 
 * @author nly36776
 * 
 */
public class ZipFileHelper {

	private ZipFile zipfile;
	private Map<String, String> subsFromTos;
	private File targetDir;

	private List<String> sortedKeys;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ZipFileHelper.class);	
	
	public ZipFileHelper(ZipFile zipfile, Map<String, String> aSubsFromTos, File aTargetDir) {
		super();
		this.zipfile = zipfile;
		this.subsFromTos = expandFromTos(aSubsFromTos);
		this.targetDir = aTargetDir;
	}


	public void doGenerate() {
		LOGGER.info("Processing zipfile : " + this.zipfile.getName());
		Enumeration<?> entries = zipfile.entries();
		while (entries.hasMoreElements()) {
			processEntry(zipfile, (ZipEntry) entries.nextElement());
		}
	}

	/**
	 * also append the "<%=XXX%>" tag, and fill the 'sortedKey' so that the <%=XXX%> tags are processed first.
	 * @param aSubsFromTos
	 * @return
	 */
	private Map<String, String> expandFromTos(Map<String, String> aSubsFromTos) {
		Map<String, String> result = new HashMap<>();
		sortedKeys = new ArrayList<>(); 
		for (String key : aSubsFromTos.keySet()) {
			String key1 = "<%=" + key + "%>";
			String key2 = key;
			result.put(key1, aSubsFromTos.get(key));
			result.put(key2, aSubsFromTos.get(key));
			sortedKeys.add(key1);
			sortedKeys.add(key2);
		}
		return result;
	}
	
	
	private void processEntry(ZipFile aZip, ZipEntry aEntry) {
		if (aEntry.isDirectory()) {
			processZipFolder(aZip, aEntry);
		} else {
			processZipEntry(aZip, aEntry);
		}
	}

	private void processZipFolder(ZipFile aZipFile, ZipEntry aZipEntry) {
		try {
			String tofileName = generateToFile(aZipEntry.getName());
			new File(tofileName).mkdirs();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void processZipEntry(ZipFile aZipFile, ZipEntry aZipEntry) {
		try {
			String tofileName = generateToFile(aZipEntry.getName());
			if (!(new File(tofileName).exists())) {
				new File(new File(tofileName).getParent()).mkdirs();
				String source = readZipEntry(aZipFile, aZipEntry);
				if (isBinary(source)) {
					copyBinaryFile(tofileName, aZipFile, aZipEntry);
				} else {
					substituteAndWriteTargerfile(tofileName, source);
				}
			} else {
				LOGGER.info("skipping existing " + tofileName);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void substituteAndWriteTargerfile(String tofileName, String source) throws IOException {
		String target = substituteFromToValues(source, false);
		LOGGER.info("generating " + tofileName);
		write(target.getBytes(), new File(tofileName));
	}

	private static void copyBinaryFile(String tofileName, ZipFile aZipFile, ZipEntry aZipEntry) {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(new File(tofileName));
			copyFile(aZipFile.getInputStream(aZipEntry), os);
		} catch (Exception e) {
			LOGGER.error("error copying binary file " + e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private String generateToFile(String aFromFileName) {
		if (generateToTempdir()) {
			return getTempdir() + "/" + aFromFileName;
		} else {
			String result = targetDir.getAbsolutePath() + "/" + aFromFileName;
			return substituteFromToValues(result, true);
		}
	}
	
	private boolean generateToTempdir() {
		return DataStore.getInstance().getBooleanValue(CodegenConstants.SUBSVAL_GENERATE_ALL_TMPDIR);
	}	

	public static String getTempdir() {
		return System.getProperty("java.io.tmpdir") + ShellUtils.getPathDelim() + "codegen";
	}
	
	private String substituteFromToValues(String aSource, boolean aPath) {
		String result = aSource;

		for (String fromstr : sortedKeys) {
			String tostr = subsFromTos.get(fromstr);
			result = StrUtil.replace(result, fromstr, tostr);
		}

		return result;
	}

	
	private static boolean isBinary(String aSource) {
		char chars[] = aSource.toCharArray();
		for (char c : chars) {
			if ((int) c == 0) {
				return true;
			}
		}
		return false;
	}

	private static long copyFile(InputStream input, OutputStream output) throws IOException {
		byte buffer[] = new byte[2048];

		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
	
	private static String readZipEntry(ZipFile aZipFile, ZipEntry aZipEntry) throws FileNotFoundException, IOException {
		StringBuffer sb = new StringBuffer();
		InputStream is = aZipFile.getInputStream(aZipEntry);
		try {
			int ch = 0;
			while ((ch = is.read()) > -1) {
				sb.append((char) ch);
			}
			return sb.toString();
		} finally {
			if (is != null)
				is.close();
		}
	}

	private static void write(byte[] from, File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		write(from, fos);
	}
	
	
	private static void write(byte[] from, OutputStream to) throws IOException {
		try {
			to.write(from);
		} finally {
			to.close();
		}
	}
	
	//-------
	
	public static ZipFile getNestedZipfile(ZipFile aZipFile, ZipEntry aZipEntry) throws Exception {
		String tofname = ShellUtils.getTempDir() + "/" + aZipEntry.getName();
		copyBinaryFile(tofname, aZipFile, aZipEntry);
		ZipFile result = new ZipFile(tofname);
		return result;
	}
}
