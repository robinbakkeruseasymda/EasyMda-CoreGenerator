package com.flca.mda.codegen.helpers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mda.annotation.KeepThisFile;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.codegen.merge.java.JControlModel;
import org.eclipse.emf.codegen.merge.java.JMerger;
import org.eclipse.emf.codegen.util.CodeGenUtil;
import org.eclipse.jdt.core.IJavaProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import flca.mda.codegen.CodegenConstants;
import flca.mda.codegen.data.DataStore;
import flca.mda.codegen.data.ITemplate;
import flca.mda.codegen.data.TemplateMergeStrategy;
import flca.mda.codegen.helpers.FileHelper;
import flca.mda.codegen.helpers.FilenameHelper;
import flca.mda.codegen.helpers.ShellUtils;
import flca.mda.codegen.helpers.StrUtil;

public class SaveGeneratedCodeHelper {
	
	private IProgressMonitor monitor;
	private IJavaProject project;

	private Map<Target, OutputData> outputData = new HashMap<SaveGeneratedCodeHelper.Target, SaveGeneratedCodeHelper.OutputData>();
	private ITemplate template;

	private String generatedFilename;
	private String targetOutputFname;

	private static FilenameHelper fnh = new FilenameHelper();

	private static final Logger LOGGER = LoggerFactory.getLogger(SaveGeneratedCodeHelper.class);
	
	public SaveGeneratedCodeHelper(IJavaProject aProject, IProgressMonitor monitor) {
		super();
		this.project = aProject;
		this.monitor = monitor;
	}

	/**
	 * This will save the generated code to the target and also to three temp
	 * directories. The target dir is indicated by (required) substitute keys
	 * like Backend, Frontend etc. The temp directories are resp.
	 * <tmp>/codegen/backup, <tmp>/codegen/merged and <tmp>/codegen/unmerged.
	 * The latter tmp dir is identical to the target dir. One can force that the
	 * target dir is NOT used however by selecting the checkbox (Skip all
	 * existing files, and/or Generate all to TMP dir). example:
	 * Backend=C:/mydocs/robin/Project/flexcrm/projects/FlcaCrmBackend/ This
	 * will be the default target dir. This can be superseded by adding the
	 * package name like: output.dir.backend_comflca.demo=C:/mydocs/DemoProject/
	 * If the checkbox "generate to temp dir"  is selected, we will not write (and log) the Target.OUTPUTFILE target
	 * but instead write (and log) the Target.MERGED target. 
	 * 
	 * @param element
	 * @param aGeneratedCode
	 * @param aTemplate
	 * @throws CoreException
	 */
	public void save(Class<?> aModelClass, String aGeneratedCode, ITemplate aTemplate) throws CoreException {
		template = aTemplate;
		InputStream is = null;

		try {
			monitor.worked(1);

			generatedFilename = fnh.generateTargetFilename(template.getOutputFilename(), aModelClass, aTemplate);
			generatedFilename = replaceFilenameViaHook(generatedFilename, aModelClass, aTemplate);
			targetOutputFname = handleRelativeFilenameFromModelDir(generatedFilename);
			setupOutputData(aModelClass, aTemplate);

			if (template.getInsertionTag() != null) {
				insertIntoFile(targetOutputFname, aGeneratedCode, template);
			} else {
				writeNewFile(aGeneratedCode, aModelClass, aTemplate);
			}
		} catch (Exception e) {
			LOGGER.error("error in save ", e);
		} finally {
			closeInputstream(is);
		}
	}

	private String replaceFilenameViaHook(String aGeneratedFname, Class<?> aModelClass, ITemplate aTemplate) {
		String result = aGeneratedFname;
		
		if (aTemplate.getHooks()!=null) {
			String replFname = aTemplate.getHooks().doGenerateFilename(aGeneratedFname, aTemplate, aModelClass);
			if (replFname != null) {result = replFname;}
		}
		
		return result;
	}
	
	private void writeNewFile(String aGeneratedCode, Class<?> aModelClass, ITemplate aTemplate) throws FileNotFoundException, IOException {
		File targetFile = new File(targetOutputFname);

		boolean targetExists = targetFile.exists();
		if (targetExists && !ShellUtils.forceOverwriteWithoutMerging()) {
			// we may have to merge
			handleExistingFile(aGeneratedCode, aModelClass, aTemplate);
		} else {
			outputData.get(Target.MERGED).content = aGeneratedCode;
			outputData.get(Target.OUTPUTFILE).content = aGeneratedCode;
		}

		outputData.get(Target.UNMERGED).content = aGeneratedCode;
		writeFiles(targetExists);
		new SimpleClibboardHelper().save(aGeneratedCode);
	}

	private void writeFiles(boolean targetExists) {
		for (Target target : Target.values()) {
			OutputData data = outputData.get(target);
			try {
				if (!skipTargetFile(targetExists, target)) {
					writeToFile(data.outputFile, data.content, target);
				} else {
					if (logWriteMsg(target)) {
						LogHelper.console("existing file " + data.outputFile + " is not overwritten");
					}
				}
			} catch (Exception e) {
				LogHelper.console("unable to write targetfile " + data.outputFile + " :" + e);
			}
		}
	}

	private boolean skipTargetFile(boolean targetExists, Target aTarget) {
		if (ShellUtils.forceOverwriteWithoutMerging()) {
			return false;
		} else if (Target.OUTPUTFILE.equals(aTarget) && generateToTempdir()) {
			return true;
		} else if (targetExists) {
			return (TemplateMergeStrategy.SKIP.equals(template.getMergeStrategy())
					|| skipAllExisting()
					|| hasKeepFileAnnotation())
					|| hasMovedFileToUserFolder();
		} else {
			if (hasMovedFileToUserFolder()) {
				File targetfile = outputData.get(Target.OUTPUTFILE).outputFile;
				LogHelper.console("target: " + targetfile.getName() + " already exists in a user folder");
				return true;
			} else {
				return false;
			}
		}
	}

	private boolean hasKeepFileAnnotation() {
		String content = outputData.get(Target.OUTPUTFILE).content;
		if (content != null) {
			return hasKeepFileAnnotation(content);
		} else {
			return false;
		}
	}
	
	private boolean generateToTempdir() {
		return DataStore.getInstance().getBooleanValue(CodegenConstants.SUBSVAL_GENERATE_ALL_TMPDIR);
	}

	private boolean skipAllExisting() {
		return DataStore.getInstance().getBooleanValue(CodegenConstants.SUBSVAL_SKIP_ALL_EXISTING);
	}
	/**
	 * If a class file exists, but there is no file in the target output folder,
	 * than the user moved the file to a user folder to indicate that it should
	 * not be re-generated anymore.
	 * 
	 * @return
	 */
	private boolean hasMovedFileToUserFolder() {
		File file = outputData.get(Target.OUTPUTFILE).outputFile;

		for (String userdir : userDirs()) {
			String testdir = handleRelativeFilenameFromModelDir(userdir);
			List<File> findfiles = FileHelper.findFiles(testdir, new MyFileFilter(file), true);
			if (findfiles != null && findfiles.size() > 0) {
				return true;
			}
		}

		return false;
	}

	private String[] userDirs() {
		String userdirs = DataStore.getInstance().getValue(CodegenConstants.SUBSVAL_USER_SOURCE_DIRS);
		if (userdirs != null) {
			userdirs = StrUtil.replace(userdirs, ";", ",");
			if (!userdirs.endsWith(",")) {
				userdirs += ",";
			}
			return StrUtil.split(userdirs, ",");
		} else {
			return new String[] {};
		}
	}

	private boolean hasKeepFileAnnotation(String content) {
		boolean result = (content.indexOf("@" + KeepThisFile.class.getSimpleName()) > 0 || 
								content.indexOf("@" + KeepThisFile.class.getName()) > 0);

		if (result) {
			LogHelper.console("skipping file because it contains the @KeepThisFile annotation");
		}

		return result;
	}

	private void setupOutputData(Class<?> aModelClass, ITemplate aTemplate) {
		String templateOutfilename = template.getOutputFilename();
		templateOutfilename = templateOutfilename.substring(templateOutfilename.indexOf("/"));
		String tmpdirOutputFname = fnh.generateTargetFilename(templateOutfilename, aModelClass, aTemplate);

		for (Target target : Target.values()) {
			if (!target.equals(Target.OUTPUTFILE)) {
				File f = getExplicitTempFile(target, tmpdirOutputFname);
				outputData.put(target, new OutputData(f));
			}
		}

		outputData.put(Target.OUTPUTFILE, new OutputData(new File(targetOutputFname)));
	}

	/*
	 * Existing files may be merged.
	 */
	private void handleExistingFile(String newCode, Class<?> aModelClass, ITemplate aTemplate) throws FileNotFoundException, IOException {
		String mergedCode = newCode;
		if (isJavaMergeNeeded(aTemplate)) {
			InputStream is = new FileInputStream(outputData.get(Target.OUTPUTFILE).outputFile);
			mergedCode = javaMerge(newCode, is);
			closeInputstream(is);
		} else {
			if (aTemplate.getHooks() != null) {
				mergedCode = aTemplate.getHooks().doMerge(newCode, outputData.get(Target.OUTPUTFILE).outputFile, aModelClass, aTemplate);
			}
		}

		outputData.get(Target.BACKUP).content = readFromFile(outputData.get(Target.OUTPUTFILE).outputFile.getAbsolutePath());
		outputData.get(Target.MERGED).content = mergedCode;
		outputData.get(Target.OUTPUTFILE).content = mergedCode;
	}

	
	private boolean isJavaMergeNeeded(ITemplate aTemplate) {
		if (TemplateMergeStrategy.JAVA_MERGE.equals(aTemplate.getMergeStrategy())) {
			String filename = outputData.get(Target.OUTPUTFILE).outputFile.getAbsolutePath();
			return (filename.toLowerCase().endsWith(".java"));		
		} else {
			return false;
		}
	}
	


	/**
	 * If the target output filename ends with "#<insertion-point>, it indicated
	 * that instead of creating a new file, the content should be insert at the
	 * given insertion point in the given file. This method true if the target
	 * output file ends with
	 * 
	 * @param element
	 * @param aTemplate
	 * @return
	 */
	protected boolean shouldInsertIntoFile(String aFilename) {
		return aFilename.indexOf("#") > 0;
	}

	protected void insertIntoFile(String aFilename, String aGeneratedCode, ITemplate aTemplate) {
		String filename = aFilename;
		String insertionPnt = aTemplate.getInsertionTag();

		try {
			File currFile = FileHelper.getFile(aFilename);
			if (currFile.exists()) {
				insertIntoFile(aGeneratedCode, filename, insertionPnt, currFile);
			} else {
				LogHelper.console("File for insertIntoFile " + currFile + " does not exist");
			}

		} catch (Exception ex) {
			LogHelper.console("could not insert generated newCode into " + filename + " : " + ex);

		}
	}
	

	private void insertIntoFile(String aGeneratedCode, String filename,
			String insertionPnt, File currFile) throws FileNotFoundException,
			IOException {
		StringBuffer sb = new StringBuffer();
		String currSource = FileHelper.readFile(new File((filename)));
		if (currSource.indexOf(aGeneratedCode.trim()) < 0) {
			String currLines[] = currSource.split("\n");
			for (String line : currLines) {
				if (line.indexOf(insertionPnt) > 0) {
					sb.append(line + "\n");
					sb.append(linesToInsert(aGeneratedCode, currLines));
				} else {
					sb.append(line + "\n");
				}
			}
			LogHelper.console("inserting lines into " + currFile);
			FileHelper.saveFile(currFile, sb.toString());
		}
	}
	
	private String linesToInsert(String aGeneratedCode, String aCurrLines[]) {
		StringBuffer sb = new StringBuffer();

		String newlines[] = aGeneratedCode.split("\n");
		for (String newline : newlines) {
			if (!lineExists(newline, aCurrLines)) {
				sb.append(newline + "\n");
			}
		}
		
		return sb.toString();
	}
	
	private boolean lineExists(String newline, String aCurrLines[]) {
		for (String currline : aCurrLines) {
			if (currline.trim().equals(newline.trim())) {
				return true;
			}
		}
		return false;
	}

	private String handleRelativeFilenameFromModelDir(String aFname) {
		if (aFname.startsWith("/") || aFname.indexOf(":") > 0) {
			return aFname;
		} else {
			String projdir = ProjectInstanceHelper.getInstance().getProjectLocation(project);
			String result = projdir + "/" + aFname;
			return result;
		}
	}

	public static String getTempdir() {
		return System.getProperty("java.io.tmpdir") + ShellUtils.getPathDelim() + "codegen";
	}

	private File getExplicitTempFile(Target aTarget, String aOutputFilename) {
		String targetdir = getTempdir() + ShellUtils.getPathDelim() + aTarget.name().toLowerCase();
		String filename = targetdir + ShellUtils.getPathDelim() + aOutputFilename;
		return new File(filename);
	}

	private void closeInputstream(InputStream is) {
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * @param file
	 * @param aContent
	 * @throws Exception
	 */
	private void writeToFile(File file, String aContent, Target aTarget) throws Exception {
		if (file != null) {
			if (aContent != null && !aContent.trim().isEmpty()) {
				prepareToFile(file);
				writeBytesToFile(file, aContent);
				if (logWriteMsg(aTarget)) {LogHelper.console("generated " + file.getAbsolutePath());}
			} else {
				if (logWriteMsg(aTarget) && 
					file.getAbsolutePath().indexOf(CodegenConstants.NOT_APPLICABLE) < 0) {
					LogHelper.console("skipped empty file " + file.getAbsolutePath());
				}
			}
		}
	}

	private boolean logWriteMsg(Target aTarget) {
		return Target.OUTPUTFILE.equals(aTarget) || 
				Target.MERGED.equals(aTarget) && generateToTempdir();
	}
	
	private void writeBytesToFile(File file, String aContent) throws FileNotFoundException, IOException {
		OutputStream os = new FileOutputStream(file);
		os.write(aContent.getBytes());
		os.flush();
		os.close();
	}

	private void prepareToFile(File file) {
		if (!file.exists()) {
			File fileDir = new File(file.getParent());
			fileDir.mkdirs();
		} else {
			file.delete();
		}
	}

	private static String readFromFile(String aFilename) throws IOException {
		StringBuffer sb = new StringBuffer();
		FileInputStream fin = new FileInputStream(aFilename);
		BufferedInputStream bin = new BufferedInputStream(fin);
		int ch = 0;
		while ((ch = bin.read()) > -1) {
			sb.append((char) ch);
		}
		bin.close();
		fin.close();
		return sb.toString();
	}

	protected void prepareFolder(IFolder folder) throws CoreException {
		IContainer parent = folder.getParent();
		if (parent instanceof IFolder) {
			prepareFolder((IFolder) parent);
		}
		if (!folder.exists()) {
			folder.create(true, true, monitor);
		}
	}

	protected String javaMerge(String generated, InputStream contents) {
		try {
			// GeneratorPlugin.getDefault().log("generated: " + generated);
			JMerger merger = getJMerger();
			merger.setSourceCompilationUnit(merger.createCompilationUnitForContents(generated));
			merger.setTargetCompilationUnit(merger.createCompilationUnitForInputStream(contents));
			merger.merge();

			return merger.getTargetCompilationUnitContents();
		} catch (Throwable e) {
			LOGGER.error("error merging: " + generated, e);
			return generated;
		}
	}

	protected JMerger getJMerger() {
		String uri = PluginHelper.getInstance().getTemplatesConfigDir() + "/merge.xml";
		JControlModel jControlModel = new JControlModel();
		jControlModel.initialize(CodeGenUtil.instantiateFacadeHelper(JMerger.DEFAULT_FACADE_HELPER_CLASS), uri);
		JMerger jmerger = new JMerger(jControlModel);
		return jmerger;
	}

	static enum Target {
		BACKUP, UNMERGED, MERGED, OUTPUTFILE;
	}

	// -- helper class
	static class OutputData {
		File outputFile;
		String content;

		// boolean doWrite = true;

		public OutputData(File outputFile) {
			super();
			this.outputFile = outputFile;
		}
	}

	static class MyFileFilter implements FileFilter {
		private File findFile;

		public MyFileFilter(File findFile) {
			super();
			this.findFile = findFile;
		}

		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory() || pathname.getName().equals(findFile.getName());
		}

	}

}
