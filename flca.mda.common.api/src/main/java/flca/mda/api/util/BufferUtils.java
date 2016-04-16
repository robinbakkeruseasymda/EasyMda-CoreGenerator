package flca.mda.api.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class BufferUtils
{
	private static Logger logger = LoggerFactory.getLogger(BufferUtils.class);
	
	//we can have several buffers
	private Map<String, List<String>> sBuffers = new HashMap<String, List<String>>(); 
	
	private static String BOOKMARK_PREFIX = "//-- FlcaBufferHelper:BOOKMARK-";
	/**
	 * create a new list (clearing the existing if there is one!)
	 * it returns a bookmark, that you write to the outputfile. This will be used to substitube with the
	 * the actual content
	 * @param aName
	 */
	public String initBookmark(String aName) 
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
	public void append(String aName, String aText) 
	{
		if (!sBuffers.containsKey(aName)) {
			logger.error("no buffer found with name : " + aName);
		} else {
			if (!sBuffers.get(aName).contains(aText)) {
				sBuffers.get(aName).add(aText);
			}
		}
	}
	
	/**
	 * This will write the content of the buffer to the outputfile stringBuffer
	 * using the bookmark set earlier.
	 * @param aName
	 * @param aTargetBuffer
	 */
	public void write(String aName, StringBuffer aTargetBuffer)
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
