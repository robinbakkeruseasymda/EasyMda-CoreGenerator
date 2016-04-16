package flca.mda.api.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;

import flca.mda.codegen.data.DataStore;
import flca.mda.codegen.data.ITemplate;
import flca.mda.codegen.helpers.StrUtil;
import flca.mda.common.api.helpers.ImportHelper;
import flca.mda.common.api.helpers.NameHelper;

public class NameUtils {

	private static AppUtils au = new AppUtils();
	/**
	 * formats a string with multiple words with spaces to single camel-cased string
	 * ex: "aaa bbb" => "AaaBbb" 
	 * @param value
	 * @return
	 */
	public String toUpperCamel(String value) {
		StringBuilder builder = new StringBuilder();
		String[] tokens = value.split(" ");
		for (String string : tokens) {
			string = capName(string);
			builder.append(string);
		}
		return builder.toString();
	}

	public String uncapName(String name) {
		if (name.length() == 0)
			return name;
		else
			return (name.substring(0, 1).toLowerCase() + name.substring(1)).replace(" ", "");
	}

	public String uncapName(Field field) {
		return uncapName(field.getName());
	}

	public String uncapNameNonPlural(Object parameter) {
		ImportHelper.addImport(parameter.getClass());
		String name = uncapName(parameter);
		return nonPural(name);
	}

	public String capName(String name) {
		if (name.length() == 0)
			return name;
		else
			return (name.substring(0, 1).toUpperCase() + name.substring(1)).replace(" ", "");
	}

	public String capName(Object element) {
		ImportHelper.addImport(element.getClass());
		return capName(element.getClass().getName());
	}

	public String capName(Field field) {
		return capName(field.getName());
	}

	public String capNameNonPlural(Object parameter) {
		ImportHelper.addImport(parameter.getClass());
		String name = capName(parameter);
		return nonPural(name);
	}

	public String nonPural(String name) {
		if (name.endsWith("ies")) {
			name = name.substring(0, name.length() - 3) + "y";
		}
		if (name.endsWith("s")) {
			name = name.substring(0, name.length() - 1);
		}
		return name;
	}
	
	public String plural(String name) {
		if (!name.endsWith("s")) {
			return name + "s";
		} else {
			return name;
		}
		
	}

	// public String getFormattedName(Object element) {
	// return format(getCapName(element), ' ', null, false);
	// }
	//

	 /** 
	  * This return a String in uppercase with "_" at Camel elements
	  * ex: UserDao = USER_DAO
	  * @param name
	  * @return
	  */
	// TODO
	 public String capUpperCamel(String name) {
		 return name.toUpperCase();
	 }

	public String removeSpaces(String name) {
		return name.replaceAll(" ", "");
	}

	public String uncapName(Object element) {
		return NameHelper.uncapPrefixedName(element.getClass().getName());
	}

	/**
	 * This makes the input string lowercase except for the first char
	 * ex field orderStatus -> Orderstatus
	 * @param string
	 * @return
	 */
	public String capToLower(String string) {
		return capName(string.toLowerCase());
	}
	
	/**
	 * returns the package of the current class/object and current template
	 * 
	 * @return
	 */
	public String getCurrentPackage() {
		Class<?> currClz = JetArgument.getCurrent().getElementClass();
		ITemplate currTemplate = JetArgument.getCurrent().getTemplate();
		String result = new TemplateUtils().getPackage(currClz, currTemplate);

		return result;
	}

	/**
	 * returns the package of the current class/object and current template
	 * 
	 * @return
	 */
	public String getCurrentClassname() {
		Class<?> currClz = JetArgument.getCurrent().getElementClass();
		ITemplate currTemplate = JetArgument.getCurrent().getTemplate();
		return new TemplateUtils().getClassName(currClz, currTemplate);
	}

	/**
	 * return the classname only given the fully qualified name
	 * 
	 * @param aFqn
	 * @return
	 */
	public String getSimplename(String aFqn) {
		if (aFqn != null && aFqn.indexOf(".") >= 0) {
			return aFqn.substring(aFqn.lastIndexOf(".") + 1);
		} else {
			return aFqn;
		}
	}

	/**
	 * return the value from an inifile, given the section and key. 
	 * Note that all ini files from the plugin and templates are cached at the start of the program
	 * 
	 * @param aSectionName
	 * @param aKey
	 * @return
	 */
	public String getIniFileValue(String aSectionName, String aKey) {
		return DataStore.getInstance().getSectionValue(aSectionName, aKey);
	}

	/**
	 * return the value from an inifile, given the section and key. 
	 * Note that all ini files from the plugin and templates are cached at the start of the program
	 * If null or empty then teh default will be returned
	 * 
	 * @param aSectionName
	 * @param aKey
	 * @return
	 */
	public String getIniFileValue(String aSectionName, String aKey, String aDefault) {
		String result = DataStore.getInstance().getSectionValue(aSectionName, aKey);
		if (result == null || result.isEmpty()) {
			return aDefault;
		} else {
			return result;
		}
	}

	/**
	 * return the value that you gave a value via the gui
	 * @param aKey
	 * @return
	 */
	public String getSubsValue(String aKey) {
		return DataStore.getInstance().getValue(aKey);
	}

	/**
	 * return the value that you gave a value via the gui. If null or empty then teh default will be returned
	 * @param aKey
	 * @return
	 */
	public String getSubsValue(String aKey, String aDefault) {
		String result = DataStore.getInstance().getValue(aKey);
		if (result == null || result.isEmpty()) {
			return aDefault;
		} else {
			return result;
		}
	}

	/**
	 * Shortcut to add the simplenname and also import the class
	 * 
	 * @param aClass
	 * @return
	 */
	public String use(Class<?> aClass) {
		ImportHelper.addImport(aClass);
		return aClass.getSimpleName();
	}

	/**
	 * Shortcut to add the simplenname and also import the class
	 * 
	 * @param aClass
	 * @return
	 */
	public String use(String aFqn) {
		ImportHelper.addImport(aFqn);
		if (aFqn.indexOf(".") > 0) {
			return aFqn.substring(aFqn.lastIndexOf(".") + 1);
		} else {
			return aFqn;
		}
	}

	public String substituteSnippet(String aSectionName, String aKey, Properties aSubsWith) {
		String result = getIniFileValue(aSectionName, aKey);
		if (result != null) {
			return StrUtil.subsProperties(result, aSubsWith);
		} else {
			return null;
		}
	}

	/**
	 * This substitutes in the given the string, the from values from the Proprties with the 
	 * corresponding to value.
	 * Note: the from values are indicated in the source string with "<%=xxx%>
	 * In the properties only the "xxx" must be supplied.
	 * @param aSource
	 * @param aProps
	 * @return
	 */
	public String substitute(String aSource, Properties aProps) {
		return StrUtil.subsProperties(aSource, aProps);
	}
	
	/**
	 * This creates a Properties from the given pair of from-to string
	 * @param fromTos
	 * @return
	 */
	public Properties makeProperties(String ...fromTos) {
		if (fromTos.length%2 != 0) {
			throw new RuntimeException("NameUtils.makeProperties() should be called with even number of from-to values");
		}
		Properties props = new Properties();
			for (int i=0; i<fromTos.length; i=i+2) {
				props.put(fromTos[i], fromTos[i+1]);
			}
		
		return props;
	}
	
	/**
	 * return n spaces 
	 * @param n
	 * @return
	 */
	public String spaces(int n) {
		return "                                                                                                              ".substring(0, n);
	}

	/**
	 * return n spaces 
	 * @param n
	 * @return
	 */
	public String tabs(int n) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i < n; i++) {
			sb.append("\t");
		}
		return sb.toString();
	}

	/**
	 * return a concatenated string from the given stringlist, with a startstring on the left
	 * a string between the list items and a string at the end.
	 * For example:
	 * String args = nu.join(argumentNames, "", ",", "");
	 * @param aStringList
	 * @param aLeft
	 * @param aSep
	 * @param aRight
	 * @return
	 */
	public String join(List<String> aStringList, String aLeft, String aSep, String aRight) {
		StringBuffer sb = new StringBuffer(aLeft);
		for (int i=0; i<aStringList.size(); i++) {
			sb.append(aStringList.get(i));
			if (i < aStringList.size()-1) {
				sb.append(aSep);
			}
		}
		sb.append(aRight);
		return sb.toString();
	}

	public String join(List<String> aStringList, String aSep) {
		return join(aStringList, "", aSep, "");
	}
	public String join(String aStringArray[], String aLeft, String aSep, String aRight) {
		StringBuffer sb = new StringBuffer(aLeft);
		for (int i=0; i<aStringArray.length; i++) {
			sb.append(aStringArray[i]);
			if (i < aStringArray.length-1) {
				sb.append(aSep);
			}
		}
		sb.append(aRight);
		return sb.toString();
	}

	public String trimRightComma(String aString) {
		return trimRight(aString, ",");
	}

	public String trimRight(String aString, String aToRemove) {
		if (aString != null && aString.trim().endsWith(aToRemove)) {
			int idx = aString.lastIndexOf(aToRemove);
			return aString.substring(0, idx);
		} else {
			return aString;
		}
	}

	public String trimLeft(String aString, String aToRemove) {
		if (aString != null && aString.startsWith(aToRemove)) {
			int idx = aString.indexOf(aToRemove);
			return aString.substring(idx + aToRemove.length());
		} else {
			return aString;
		}
	}

	public String trimChars(String aString, String aToRemove) {
		String result = trimLeft(aString, aToRemove);
		return trimRight(result, aToRemove);
	}

	public String splitLongLine(String line, int len) {
		return line;
		// TODO
		// if (line.length() > len) {
		// StringBuffer sb = new StringBuffer();
		// int idx=0, startIdx=0;
		// while (startIdx <= line.length() && idx >= startIdx) {
		// idx = getSplit(line, startIdx, len);
		// sb.append(line.substring(startIdx, idx) + NL);
		// startIdx = idx;
		// }
		// return sb.toString();
		// } else {
		// return line;
		// }
	}

	// private int getSplit(String string, int startIdx, int len) {
	// int idx=startIdx;
	// int at = string.indexOf(",", idx);
	// while (at < len && idx < string.length()) {
	// idx = at;
	// at = string.indexOf(",", idx);
	// }
	// return at+1;
	// }
	
	/**
	 * Format a camelcased string to lowercase string with the give delim
	 * ex: TestClassAbc => test_class_abc
	 * @param value
	 * @param delim
	 * @return
	 */
	public String unCamelLower(String value, char delim) {
		return unCamel(value, delim).toLowerCase();
	}

	/**
	 * Format a camelcased string to lowercase string with the give delim
	 * ex: TestClassAbc => test_class_abc
	 * @param value
	 * @param delim
	 * @return
	 */
	public String unCamelUpper(String value, char delim) {
		return unCamel(value, delim).toUpperCase();
	}

	/**
	 * Format a camelcased string to lowercase string with the give delim
	 * ex: TestClassAbc => test_class_abc
	 * @param value
	 * @param delim
	 * @return
	 */
	public String unCamel(String value, char delim) {
		StringBuffer sb = new StringBuffer();
		Byte prev = null;
		for (Byte b : value.getBytes()) {
			if (unCamelAddDelim(b, prev, sb.length())) {
				sb.append(delim); 
			}
			sb.append(new String(new byte[] {b}));
			prev = b;
		}
		return sb.toString();
	}
	
	private boolean unCamelAddDelim(Byte curr, Byte prev, int strlen) {
		return (isUpper(curr) && strlen > 0 && 
				(isUpper(prev) || isLower(prev)) &&
				!isUpper(prev) );
	}
	
	public boolean isUpper(char c) {
		return c >= 65 && c <= 90;
	}
	
	public boolean isLower(char c) {
		return c >= 90 && c <= 122;
	}
	
	public boolean isUpper(byte c) {
		return c >= 65 && c <= 90;
	}
	
	public boolean isLower(byte c) {
		return c >= 90 && c <= 122;
	}
	
	
	/**
	 * replace in the given input string, the given appname with the target appname 
	 * and apppck with the tarhet apppck
	 * @param source
	 * @return
	 */
	public String substituteAppVarbs(String aSource, String fromAppname, String fromApppck) {
		String toAppname = au.getApplicationName();
		String toApppck = au.getApplicationPackage();
		Properties props = new Properties();
		props.put(fromAppname, toAppname);
		props.put(fromApppck, toApppck);
		String result = StrUtil.subsProperties(aSource, props);
		return result;
	}
	
}
