package flca.mda.codegen.helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * helper class to ini files. Note this parser allows to read multiple lines
 * when the ini file looks like this: [ sSectionAbc ]
 * 
 * key1 = value1 key2 = < line 1 blabla line 2 blsbls >
 * 
 * key3 = value3
 * 
 * @author rbakkerus
 * 
 */
public class IniFileParser {

	private static Logger logger = LoggerFactory.getLogger(IniFileParser.class);

	IniFileSection section = null;
	StringBuffer multilineSb = null;
	String multilineKey = null;

	private static final String COMMENT[] = new String[] { "#", ";" };

	private static final String MULTI_LINE_START = "(\\S*)(\\s*=\\s*)(\\{\\{\\s*)$"; // foo
	// =
	// {{
	private static final String MULTI_LINE_END = "^\\}}\\s*"; // }}
	private static final String INI_SECTION = "(^\\[\\s*)(\\S*)(\\s*\\]\\s*$)"; // [
	// foo
	// ]
	private static final String INI_PROP = "(.*)(=)(.*$)"; // foo = bar

	private static final Pattern pSection = Pattern.compile(INI_SECTION);
	private static final Pattern pMultiStart = Pattern.compile(MULTI_LINE_START);
	private static final Pattern pMultiEnd = Pattern.compile(MULTI_LINE_END);
	private static final Pattern pIniProp = Pattern.compile(INI_PROP);

	public Collection<IniFileSection> parseFile(File aFile) {
		try {
			List<String> lines = readLines(aFile);
			return parseLines(lines, aFile.getPath());
		} catch (Exception e) {
			logger.error("error parsing " + aFile.getPath());
			return new ArrayList<IniFileSection>();
		}
	}

	public Collection<IniFileSection> parseZipEntry(ZipFile aZip, ZipEntry aEntry) {
		try {
			List<String> lines = readZipEntryLines(aZip, aEntry);
			return parseLines(lines, aEntry.getName());
		} catch (Exception e) {
			logger.error("error parsing " + aZip.getName() + " " + aEntry);
			return new ArrayList<IniFileSection>();
		}
	}

	private List<String> readZipEntryLines(ZipFile aZip, ZipEntry aEntry) throws FileNotFoundException, IOException {
		InputStream is = aZip.getInputStream(aEntry);
		try {
			StringBuffer sb = new StringBuffer();
			int ch = 0;
			while ((ch = is.read()) > -1) {
				sb.append((char) ch);
			}
			List<String> lines = new ArrayList<String>();
			String recs[] = sb.toString().split("\n");
			for (String rec : recs) {
				if (validLine(rec)) {
					lines.add(rec + "\n");
				}
			}
			return lines;
		} finally {
			if (is != null)
				is.close();
		}
	}

	public Collection<IniFileSection> parseLines(List<String> lines, String fname) {
		Collection<IniFileSection> result = new ArrayList<IniFileSection>();

		int n = 1;
		for (String line : lines) {
			Matcher sectionMatcher = pSection.matcher(line);
			Boolean multiEnd = pMultiEnd.matcher(line).find();

			if (sectionMatcher.find()) {
				handleSection(result, sectionMatcher.group(2));
			} else if (multilineKey == null) {
				handleSingleLine(line, fname);
			} else if (multilineKey != null && !multiEnd) {
				multilineSb.append(line + "\n");
			} else if (multilineKey != null && multiEnd) {
				handleMultiLines(fname);
			} else {
				logger.error("** This should not be possible, file:" + fname + " #" + n + " " + line);
			}
			n++;
		}

		if (section != null) {
			result.add(section.clone());
		}

		return result;
	}

	private void handleSection(Collection<IniFileSection> result, String sectionName) {
		if (section != null) {
			result.add(section.clone());
		}

		section = new IniFileSection(sectionName.trim());
		multilineKey = null;
		multilineSb = null;
	}

	private void handleMultiLines(String fname) {
		if (section != null) {
			section.add(multilineKey, multilineSb.toString());
			multilineKey = null;
			multilineSb = null;
		} else {
			logger.error("bad inifile, section expected " + fname);
		}
	}

	private void handleSingleLine(String line, String fname) {
		Matcher matcher1 = pMultiStart.matcher(line);
		Matcher matcher2 = pIniProp.matcher(line);

		if (matcher1.find()) {
			multilineKey = matcher1.group(1).trim();
			multilineSb = new StringBuffer();
		} else if (matcher2.find()) {
			section.add(matcher2.group(1).trim(), matcher2.group(3).trim());
		} else {
			logger.error("bad inifile, '=' expected " + fname);
		}
	}

	private List<String> readLines(File aFile) throws FileNotFoundException, IOException {
		List<String> lines = new ArrayList<String>();

		String s = FileHelper.readFile(aFile);
		String templines[] = StrUtil.split(s, "\n");

		for (String line : templines) {
			if (validLine(line)) {
				lines.add(line);
			}
		}
		return lines;
	}

	private boolean validLine(String aLine) {
		if (aLine.trim().length() == 0) {
			return false;
		} else {
			for (String c : COMMENT) {
				if (aLine.startsWith(c))
					return false;
			}
		}
		return true;
	}

}
