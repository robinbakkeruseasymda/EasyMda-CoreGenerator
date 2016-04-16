package flca.mda.api.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import flca.mda.codegen.helpers.FileHelper;

public class SimpleMerge {

	private static Logger logger = LoggerFactory.getLogger(SimpleMerge.class);

	private static final String PREFIX = "//-- ";
	private static final String POSFIX = " --\n";
	private static final String YOURCODE = "manual-code";
	private static final String FREEBLOCK_START = YOURCODE + "-start:";
	private static final String FREEBLOCK_END = YOURCODE + "-end: ";
	private static final String MODIFY_CODE = "TODO modify this with your own code. ";
	private static final String YOUR_CODE_BELOW = "add your code below this line";

//	private static final Pattern pFreeBlockStart = Pattern.compile("(^\\s?//.*)" + FREEBLOCK_START + "\\s*"
//			+ "(\\w+)(.*)");
//	private static final Pattern pFreeBlockEnd = Pattern.compile("(^\\s?//.*)" + FREEBLOCK_END + "\\s*" + "(\\w+)(.*)");
//	private static final Pattern pYourCodeBelow = Pattern.compile("(^\\s?//.*)" + YOUR_CODE_BELOW + ".*");
	public static final String KEEP_THIS_FILE = "KeepThisFile";

	/*
	 * A number of methods that can be used in Jet files.
	 */

	/**
	 * add a freeblock like //--- manual code-start mytag -- //--- manual
	 * code-end mytag -- These tags will be recognized by SimpleMerge
	 * 
	 * @param aTag
	 * @param aContent
	 * @return
	 */
	public static String freeBlock(String aTag, String aContent) {
		return freeBlockStart(aTag) + aContent + freeBlockEnd(aTag);
	}

	public static String freeBlock(String aTag) {
		return freeBlock(aTag, "");
	}

	/**
	 * add a freeblock like //--- manual code-start mytag -- These tags will be
	 * recognized by SimpleMerge
	 * 
	 * @param aTag
	 * @return
	 */
	public static String freeBlockStart(String aTag) {
		return PREFIX + FREEBLOCK_START + aTag + POSFIX;
	}

	/**
	 * add a freeblock like //--- manual code-end mytag -- These tags will be
	 * recognized by SimpleMerge
	 * 
	 * @param aTag
	 * @return
	 */
	public static String freeBlockEnd(String aTag) {
		return PREFIX + FREEBLOCK_END + aTag + POSFIX;
	}

	/**
	 * shortcut to add this to the generated code: // TODO modify this with your
	 * own code.
	 * 
	 * @return
	 */
	public static String modifyThisCode() {
		return PREFIX + MODIFY_CODE + POSFIX;
	}

	/**
	 * add a freeblock like //--- add your code below this line -- These tags
	 * will be recognized by SimpleMerge
	 * 
	 * @return
	 */
	public static String addYourCodeBelow() {
		return PREFIX + YOUR_CODE_BELOW + POSFIX;
	}

	// --------------------------- SimpleMerge engine -------------------------

	/**
	 * this merges the content of the inputstream <b>is</b> with the content
	 * <b>code</b> of an existing file <b>aFilename</b> It copies the original
	 * content between 'freeblock_start' and 'freeblock_end' from the source to
	 * the target content. And it also copies the original content in a method
	 * when the original code is 'more than' 'return;' or 'return null;'
	 * 
	 * @param aNewContent
	 * @param aOriginalInputStream
	 * @param aOriginalFname
	 * @return
	 */
	public static String merge(String aNewContent, File aOriginalFile) {
		String result = aNewContent;
		try {
			String originalCode = FileHelper.readFile(aOriginalFile);
			if (originalCode != null) {
				if (originalCode.indexOf(KEEP_THIS_FILE) > 0) {
					logger.info("SimpleMerge, KEEP_THIS_FILE encountered in " + aOriginalFile);
					result = originalCode;
				} else {
					result = doMerge(aNewContent, originalCode, aOriginalFile);
				}
			}
		} catch(Exception ioEx) {
			logger.error("error reading " + aOriginalFile);
		}

		return result;
	}

	private static String doMerge(String aNewContent, String originalCode, File originalFile) {
		String result = aNewContent; //TODO
		return result;
	}


	// ---- inner classes

}
