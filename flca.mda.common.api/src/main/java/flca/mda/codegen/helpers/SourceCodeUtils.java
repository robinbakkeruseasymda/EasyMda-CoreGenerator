package flca.mda.codegen.helpers;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import flca.mda.codegen.data.DataStore;

public class SourceCodeUtils {
	
	private static Logger logger = LoggerFactory.getLogger(SourceCodeUtils.class);

	private static final String IMPORT = "(^import\\s)(.*)(\\s*;)";
	private static final String STATIC_IMPORT = "(^import\\sstatic\\s)(.*)(\\s*;)";
	private static final String CLASS_DECL = "^public\\s+class\\s+";
	private static final String PACKAGE_STMT = "^\\s?package\\s+(.*)(\\s?;)";
	
	private static final Pattern pImport = Pattern.compile(IMPORT);
	private static final Pattern pImportStatic = Pattern.compile(STATIC_IMPORT);
	private static final Pattern pClassDecl = Pattern.compile(CLASS_DECL);
	private static final Pattern pPackStmt = Pattern.compile(PACKAGE_STMT);

	/**
	 * return the sourcecode as-is as a collection.
	 * 
	 * @param aSource
	 * @return
	 */
	public List<String> getSourcecodeLines(String aSource) {
		List<String> result = new ArrayList<String>();

		if (aSource != null) {
			String lines[] = StrUtil.split(aSource, '\n');
			for (String line : lines) {
				line = line.replace("\r", "");
				result.add(line);
			}
		}
		
		return result;
	}

	/**
	 * return the 'formatted' sourcecode as a collection. 'Formatted' means that
	 * all comments are removed, and all imports are at the top.
	 * 
	 * @param aSource
	 * @return
	 */
	public List<String> getFormattedSourcecodeLines(String aSource) {
		List<String> result = new ArrayList<String>();
		List<String> lines = getSourcecodeLines(aSource);

		lines = removeCommentLines(lines);

		// add all imports to the top
		for (String line : lines) {
			if (isImportStatement(line)) {
				result.add(line);
			}
		}

		// now the other lines
		for (String line : lines) {
			if (!isImportStatement(line)) {
				result.add(line);
			}
		}

		return result;
	}

	/**
	 * read all inport statements from the given source code
	 * @param aSource
	 * @return
	 */
	public List<String> getAllImports(String aSource) {
		return getAllImports(getFormattedSourcecodeLines(aSource));
	}

	/**
	 * return all Fully qualified classnames of all import statements as Astrings (@see getAllImports)
	 * 
	 * @param aLines
	 * @return
	 */
	public List<String> getAllImports(List<String> aLines) {
		List<String> result = new ArrayList<String>();

		for (String line : aLines) {
			String fqn = getImportClassname(line);
			if (fqn != null) {
				result.add(fqn);
			}
		}

		return result;
	}

	/**
	 * return boolean to indicate if this sourcecode line is an import statement. 
	 * @param line
	 * @return
	 */
	public boolean isImportStatement(String line) {
		return getImportClassname(line) != null;
	}

	/**
	 * given a sourcecode line, return the classname if this is an import statement
	 * @param line
	 * @return
	 */
	public String getImportClassname(String line) {
		Matcher mImport = pImport.matcher(line.trim());
		if (mImport.matches()) {
			return mImport.group(2);
		} else {
			return null;
		}
	}

	/**
	 * return boolean to indicate if this sourcecode line is an import statement. 
	 * @param line
	 * @return
	 */
	public boolean isPackageStatement(String line) {
		return getPackageClassname(line) != null;
	}

	/**
	 * given a sourcecode line, return the classname if this is an import statement
	 * @param line
	 * @return
	 */
	public String getPackageClassname(String line) {
		Matcher mPackage = pPackStmt.matcher(line.trim());
		if (mPackage.matches()) {
			return mPackage.group(1);
		} else {
			return null;
		}
	}


//	private static String getImportFqn(String aLine) {
//		String line = aLine.trim();
//		Matcher mImport = pImport.matcher(line);
//		if (mImport.matches()) {
//			return mImport.group(2);
//		} else {
//			return null;
//		}
//	}

	private List<String> removeCommentLines(List<String> aLines) {
		List<String> result = new ArrayList<String>();

		boolean commentFlag = false;

		for (String line : aLines) {
			if (line.trim().startsWith("/*")) {
				commentFlag = true;
			}

			if (!commentFlag && line.trim().length() > 0) {
				result.add(line);
			}

			if (line.trim().endsWith("*/")) {
				commentFlag = false;
			}
		}

		return result;
	}

	/**
	 * @deprecated use standard Class.getAnnoatation()
	 * return as-is all class or interface annotations
	 * @param aSource
	 * @return
	 */
	public List<String> getClassAnnotations(String aSource) {
		List<String> result = new ArrayList<String>();
		boolean annotFound = false;
		for (String line : getFormattedSourcecodeLines(aSource)) {
			if (line.trim().startsWith("@")) annotFound = true;
			Matcher m = pClassDecl.matcher(line.trim());
			if (m.find()) {
				break;
			} else if (annotFound) {
				result.add(line);
			}
		}
		return result;
	}

	/**
	 *  @deprecated use standard Field.getAnnoatation()
	 * return as-is all class or interface annotations
	 * @param aSource
	 * @return
	 */
	public List<String> getFieldAnnotations(Field aField, String aSource) {
		List<String> result = new ArrayList<String>();
		List<String> lines = getFormattedSourcecodeLines(aSource);
		boolean fieldFound = false;
		for (int i = lines.size()-1; i > 0; i--) {
			String line = lines.get(i);
			if (line.contains(aField.getName())) {
				fieldFound = true;
			} else if (line.trim().startsWith("@") && fieldFound) {
				result.add(line);
			} else if (!line.trim().startsWith("@") && fieldFound && line.trim().length() > 0) {
				break;
			}
			
		}
		return result;
	}
	

	/**
	 * return all import Classes (@see getAllImports)
	 * 
	 * @param aLines
	 * @return
	 */
	public List<Class<?>> getImports(String aSource) {
		List<Class<?>> result = new ArrayList<Class<?>>();
		for (String line : getFormattedSourcecodeLines(aSource)) {
			Matcher mImport = pImport.matcher(line);
			Matcher mImportStatic = pImportStatic.matcher(line);
			if (mImport.matches() && !mImportStatic.matches()) {
				try {
					Class<?> clazz = Class.forName(mImport.group(2));
					result.add(clazz);
				} catch (ClassNotFoundException e) {
					System.err.println("class " + mImport.group(2) + " not found");
				}
			} 
		}
		return result;
	}
	
	public String readSource(String aFqName) 
	{
		try {
			String modeldir = DataStore.getInstance().getModelProjectDir().getAbsolutePath();
			List<File> sourcefiles = FileHelper.findFiles(modeldir, new JavaSourceFilter(aFqName), true);
			if (sourcefiles.size() == 1) {
				return FileHelper.readFile(sourcefiles.get(0));
			} 
			logger.warn("*No source found for " + aFqName);
			return null;
		} catch (Exception e) {
			logger.error("error in ModelSourceCodeHelper.readsource ",e);
			return null;
		}
	}

	static class JavaSourceFilter implements FileFilter
	{
		private String fqn;
		
		
		public JavaSourceFilter(String fqn)
		{
			super();
			this.fqn = fqn;
		}


		@Override
		public boolean accept(File pathname)
		{
			if (pathname.isDirectory()) {
				return true;
			} else {
				if (pathname.getName().endsWith(".java")) {
					String s = StrUtil.replSlashsToDots(pathname.getPath());
					return (s.indexOf("." + fqn + "." ) > 0);
				} 
			}
			
			return false;
		}
	}

}
