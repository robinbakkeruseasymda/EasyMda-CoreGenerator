package flca.mda.codegen.helpers;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import flca.mda.codegen.data.DataStore;

public class IniFileHelper {
	private static Logger logger = LoggerFactory.getLogger(IniFileHelper.class);

	public static void initialize(File aRootDIr) {
		Map<String, IniFileSection> initialSectionMap = parseIniFiles(aRootDIr);
		DataStore.getInstance().setSections(initialSectionMap);
	}

	public static void merge(File aRootDir) {
		Map<String, IniFileSection> mergeSectionMap = parseIniFiles(aRootDir);
		Map<String, IniFileSection> currSections = DataStore.getInstance().getSections();

		for (String sectionKey : mergeSectionMap.keySet()) {
			if (currSections.containsKey(sectionKey)) {
				IniFileSection section = currSections.get(sectionKey);
				section.merge(mergeSectionMap.get(sectionKey));
			} else {
				currSections.put(sectionKey, mergeSectionMap.get(sectionKey));
			}
		}

	}

	public static Map<String, IniFileSection> parseIniFiles(File aRootDir) {
		return parseIniFiles(aRootDir, new IniFileFilter(), true);
	}

	public static Map<String, IniFileSection> parseIniFiles(File aRootDir, FileFilter aFilter, boolean aRecursive) {
		if (aRootDir.getPath().toLowerCase().endsWith(".jar")) {
			return parseIniFilesFromZip(aRootDir, aFilter, aRecursive);
		} else {
			return parseIniFilesFromDir(aRootDir, aFilter, aRecursive);
		}
	}

	public static Map<String, IniFileSection> parseIniFilesFromDir(File aRootDir, FileFilter aFilter, boolean aRecursive) {
		Map<String, IniFileSection> result = new HashMap<String, IniFileSection>();

		List<File> inifiles = FileHelper.findFiles(aRootDir.getPath(), aFilter, aRecursive);
		if (inifiles != null) {
			for (File file : inifiles) {
				try {
					Collection<IniFileSection> sections = new IniFileParser().parseFile(file);
					for (IniFileSection section : sections) {
						if (!result.containsKey(section.getSectionName())) {
							result.put(section.getSectionName(), section);
						} else {
							logInfo("sectionname already defined " + section.getSectionName());
						}
					}
				} catch (Exception e) {
					logError("error during ctor IniFileHelper " + e);
					throw new RuntimeException(e);
				}
			}
		}

		return result;
	}

	public static Map<String, IniFileSection> parseIniFilesFromZip(File aRootDir, FileFilter aFilter, boolean aRecursive) {
		Map<String, IniFileSection> result = new HashMap<String, IniFileSection>();

		try {
			ZipFile zip = new ZipFile(aRootDir);
			Enumeration<?> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (!entry.isDirectory() && aFilter.accept(new File(entry.getName()))) {
					Collection<IniFileSection> sections = new IniFileParser().parseZipEntry(zip, entry);
					for (IniFileSection section : sections) {
						if (!result.containsKey(section.getSectionName())) {
							result.put(section.getSectionName(), section);
						} else {
							logInfo("sectionname already defined " + section.getSectionName());
						}
					}
					
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return result;
	}

	
	private static void logError(String aMsg) {
		logger.error(aMsg);
	}

	private static void logInfo(String aMsg) {
		logger.warn(aMsg);
	}

	// --------
	static class IniFileFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory() || pathname.toString().toLowerCase().endsWith(".ini");
		}
	}
}
