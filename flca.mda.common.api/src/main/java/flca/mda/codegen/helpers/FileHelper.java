package flca.mda.codegen.helpers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileHelper {

	private static Logger logger = LoggerFactory.getLogger(FileHelper.class);

	private FileHelper() {
	}

	/**
	 * Read the contents of a file given URL
	 * 
	 * @param aFilename
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String readFile(String aFilename) throws FileNotFoundException, IOException {
		return readFile(new File(aFilename));
	}

	/**
	 * Read the contents of a file given URL
	 * 
	 * @param aFilename
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String readFile(URL aUrl) throws FileNotFoundException, IOException {
		return readFile(aUrl.getFile());
	}

	/**
	 * Read the contents of a file given a filename
	 * 
	 * @param aFilename
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String readFile(File aFile) throws FileNotFoundException, IOException {
		StringBuffer sb = new StringBuffer();

		FileInputStream fin = null;
		BufferedInputStream bin = null;
		try {
			fin = new FileInputStream(aFile);
			bin = new BufferedInputStream(fin);
			int ch = 0;
			while ((ch = bin.read()) > -1) {
				sb.append((char) ch);
			}

			return sb.toString();
		} catch (FileNotFoundException e) {
			logger.error("Could not find file: " + aFile);
			throw e;
		} catch (IOException e) {
			logger.error("Error reading file: " + aFile, e);
			throw e;
		} finally {
			if (bin != null) {
				bin.close();
			}
			if (fin != null) {
				fin.close();
			}
		}
	}

	/**
	 * Read the contents of a given resource, given a resourcename
	 * 
	 * @param aInstance
	 * @param aResourceName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String readResource(Object aInstance, String aResourceName) throws FileNotFoundException, IOException {
		return readResource(aInstance.getClass(), aResourceName);
	}

	/**
	 * Read the contents of a given resource, given a resourcename
	 * 
	 * @param aInstance
	 * @param aResourceName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String readResource(Class<?> aClass, String aResourceName) throws FileNotFoundException, IOException {
		InputStream is = null;
		BufferedReader reader = null;
		try {
			StringBuffer sb = new StringBuffer();

			is = aClass.getResourceAsStream(aResourceName);
			reader = new BufferedReader(new InputStreamReader(is));
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				sb.append(String.valueOf(buf, 0, numRead));
				buf = new char[1024];
			}
			return sb.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
			if (is != null) {
				is.close();
			}
		}
	}

	/**
	 * Save the given content to a given filename
	 * 
	 * @param aFilename
	 * @param aContent
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void saveFile(String aFilename, String aContent) throws FileNotFoundException, IOException {
		String dirs = null;
		if (aFilename.lastIndexOf("/") > 0) {
			dirs = aFilename.substring(0, aFilename.lastIndexOf("/"));
		} else if (aFilename.lastIndexOf("\\") > 0) {
			dirs = aFilename.substring(0, aFilename.lastIndexOf("\\"));
		}

		if (dirs != null) {
			new File(dirs).mkdirs();
			File f = new File(aFilename);
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(aContent.getBytes());
			fos.close();
		} else {
			logger.error("saveFile failed for " + aFilename);
		}
	}

	public static void saveFile(File aFile, String aContent) throws FileNotFoundException, IOException {
		saveFile(aFile.getPath(), aContent);
	}

	public static List<String> findDirs(String aSeachRootDir, boolean aRecursive) {
		List<String> result = new ArrayList<String>();

		doFindDirs(aSeachRootDir, new DirFilter(), aRecursive, result);

		return result;
	}

	public static List<File> findFiles(String aSeachRootDir, boolean aRecursive) {
		return findFiles(aSeachRootDir, null, aRecursive);
	}

	public static List<File> findFiles(String aSeachRootDir, FileFilter aFilter, boolean aRecursive) {
		List<File> result = new ArrayList<File>();

		doFindFiles(aSeachRootDir, aFilter, aRecursive, result);

		return result;
	}

	private static void doFindFiles(String aSeachRootDir, FileFilter aFilter, boolean aRecursive, List<File> aFiles) {
		File f = new File(aSeachRootDir);
		File files[] = null;

		if (!f.exists()) {
			System.out.println(f.getAbsolutePath() + " does not exist");
			return;
		}
		
		if (f != null) {
			if (aFilter == null) {
				files = f.listFiles();
			} else {
				files = f.listFiles(aFilter);
			}
		}

		if (files != null) {
			for (File file : files) {
				if (!file.isDirectory() && !file.isHidden()) {
					aFiles.add(file);
				} else if (file.isDirectory() && aRecursive) {
					String pathname = file.getPath();
					doFindFiles(pathname, aFilter, aRecursive, aFiles);
				}
			}
		}
	}

	private static void doFindDirs(String aSeachRootDir, FileFilter aFilter, boolean aRecursive, List<String> aFiles) {
		File f = new File(aSeachRootDir);
		File files[] = null;
		files = f.listFiles(aFilter);

		for (File file : files) {
			String fname = file.getPath();
			aFiles.add(fname);
		}
	}

	static class DirFilter implements FileFilter {

		public boolean accept(File file) {
			return file.isDirectory() && !file.isHidden();
		}
	}

	/**
	 * This resturn the URL given a resourcename (eq "/resources/my-file.txt") or
	 * full filename "/home/tmp/my-file.txt" or something
	 * http://www.robin.com/resources/my-file.txt If the resource or file does
	 * not exist, null will be returned
	 * 
	 * @param aFileOrResourceName
	 * @return
	 */
	public static URL getExistingUrl(String aFileOrResourceName) {
		return getExistingUrl(FileHelper.class, aFileOrResourceName);
	}

	/**
	 * This resturn the URL given a resourcename (eq "/resources/my-file.txt") or
	 * full filename "/home/tmp/my-file.txt" or something
	 * http://www.robin.com/resources/my-file.txt If the resource or file does
	 * not exist, null will be returned
	 * 
	 * @param aFileOrResourceName
	 * @return
	 */
	public static URL getExistingUrl(Class<?> aClass, String aFileOrResourceName) {
		URL url = aClass.getResource(aFileOrResourceName);
		if (url != null) {
			return url;
		} else {
			File f = new File(aFileOrResourceName);
			if (f.exists()) {
				try {
					return new URL("file://" + f.getPath());
				} catch (MalformedURLException e) {
					logger.error("error creating URL from filespec " + e);
					return null;
				}
			} else {
				return null;
			}
		}
	}

	public static String FILE_SEP = null;

	public static String appendSeperator(String aValue) {
		if (FILE_SEP == null) {
			FILE_SEP = System.getProperty("file.separator");
		}

		if (aValue != null && aValue.endsWith("/") || aValue.endsWith("\\")) {
			return aValue;
		} else {
			return aValue + FILE_SEP;
		}
	}

	public static String getFilenameOnly(File aFile) {
		if (aFile != null) {
			String s = aFile.getName();
			return s;
		} else {
			return null;
		}
	}

	public static String getNameWithoutExt(File aFile) {
		if (aFile != null) {
			String s = aFile.getName();
			int n = s.lastIndexOf(".");
			if (n > 0) {
				return s.substring(0, n);
			} else {
				return s;
			}
		} else {
			return null;
		}
	}

	public static String getFileExt(File aFile) {
		if (aFile != null) {
			String s = aFile.getName();
			int n = s.lastIndexOf(".");
			if (n > 0) {
				return s.substring(n + 1);
			}
		}

		return null;
	}

	/**
	 * return the OS specific file given the filename
	 * @param aFilename
	 * @return
	 */
	public static File getFile(String aFilename) {
		String fname = null;
		if (ShellUtils.isLinux()) {
			fname = StrUtil.replace(aFilename, "\\", "/");
			fname = StrUtil.replace(fname, "//", "/");
		} else {
			fname = StrUtil.replace(aFilename, "/", "\\");
			fname = StrUtil.replace(fname, "\\\\", "\\");
		}
		return new File(fname);
	}
	/**
	 * The returns a valid fully qualified classname, based on the given file and
	 * the the Basedir
	 * 
	 * @param aFile
	 * @param aBasedir
	 * @return
	 * @see getBindir()
	 */
	public static String getFqn(File aFile, String aBasedir) {
		String result = fromFileToPackage(aFile);

		if (aBasedir != null) {
			String basedir = fromFileToPackage(new File(aBasedir));

			if (result.startsWith(basedir)) {
				return result.substring(basedir.length() + 1);
			}
		}

		return result;
	}

	private static String fromFileToPackage(File aFile) {
		String result = aFile.getPath().replace("/", ".");
		result = result.replace("\\", ".");
		int lastdot = result.lastIndexOf(".");

		if (!aFile.isDirectory() && lastdot > 0) {
			result = result.substring(0, lastdot);
		}

		if (result.startsWith(".")) {
			result = result.substring(1);
		}

		return result;
	}

	/**
	 * Returns the /bin dir given a Class that also exist on this bindir
	 * 
	 * @param aClass
	 * @return
	 */
	public static String getBindir(Class<?> aClass) {
		URL url = aClass.getProtectionDomain().getCodeSource().getLocation();
		return url.getPath();
	}

	public static File getFileDirOnly(File aFile) {
		if (aFile != null) {
			String s1 = aFile.getPath();
			String s2 = aFile.getName();
			return new File(s1.substring(0, s1.length() - s2.length()));
		} else {
			return null;
		}
	}

	public static void copyFile(File aFromFile, String aToFilename) throws IOException {
		File outdir = getFileDirOnly(new File(aToFilename));
		outdir.mkdirs();

		FileInputStream fis = new FileInputStream(aFromFile.getAbsolutePath());
		FileChannel inChannel = fis.getChannel();
		FileOutputStream fos = new FileOutputStream(aToFilename);
		FileChannel outChannel = fos.getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException e) {
			throw e;
		} finally {
			if (fis != null)
				fis.close();
			if (fos != null)
				fos.close();
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}

	public static boolean isBinary(File aFile) {
		try {
			String s = FileHelper.readFile(aFile);
			char chars[] = s.toCharArray();
			for (char c : chars) {
				if ((int) c == 0) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	
}
