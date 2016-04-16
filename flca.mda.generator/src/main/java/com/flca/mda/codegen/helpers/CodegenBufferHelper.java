package com.flca.mda.codegen.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flca.mda.codegen.helpers.StrUtil;

/**
 * Helper class identical to importhelper. 
 * It can collect lines that must be inserted in beginning of the file
 * <code>
 * FlcaBufferHelper.init("ABC");
 * FlcaBufferHelper.bookmark("ABC"); //text will inserted here
 * FlcaBufferHelper.append("ABC", "text ..");
 * :
 * FlcaBufferHelper.write("ABC");
 * </code>
 * @author robin
 *
 */
public class CodegenBufferHelper
{
	//we can have several buffers
	private static Map<String, List<String>> sBuffers = new HashMap<String, List<String>>(); 
	
	private static String BOOKMARK_PREFIX = "//-- FlcaBufferHelper:BOOKMARK-";
	/**
	 * create a new list (clearing the existing if there is one!)
	 * it returns a bookmark, that you write to the outputfile. This will be used to substitube with the
	 * the actual content
	 * @param aName
	 */
	public static String initBookmark(String aName) 
	{
		if (!sBuffers.containsKey(aName)) {
			sBuffers.put(aName, new ArrayList<String>());
		} else {
			sBuffers.get(aName).clear();
		}
		return BOOKMARK_PREFIX + aName;
	}
	
	/**
	 * Add text to existing buffer. The buffer must exist
	 * @param aName
	 * @param aText
	 */
	public static void append(String aName, String aText) 
	{
		if (!sBuffers.containsKey(aName)) {
			LogHelper.error("no buffer found with name : " + aName);
		} else {
			sBuffers.get(aName).add(aText);
		}
	}
	
	/**
	 * This will write the content of the buffer to the outputfile stringBuffer
	 * using the bookmark set earlier.
	 * @param aName
	 * @param aTargetBuffer
	 */
	public static void write(String aName, StringBuffer aTargetBuffer)
	{
		StringBuffer sb = new StringBuffer();
		if (sBuffers.containsKey(aName)) {
			for (String s : sBuffers.get(aName)) {
				sb.append(s + " \n");
			}
		}
		
		String s = aTargetBuffer.toString();
		s = StrUtil.replace(s, BOOKMARK_PREFIX + aName, sb.toString());
		aTargetBuffer.delete(0, aTargetBuffer.length());
		aTargetBuffer.append(s);
	}
	
}
