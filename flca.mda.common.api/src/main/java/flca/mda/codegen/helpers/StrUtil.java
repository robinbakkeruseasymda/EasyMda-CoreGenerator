package flca.mda.codegen.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import flca.mda.codegen.data.SubsValue;

/**
 * This is an extented commons StringUtils
 * 
 * @author nly36776
 *
 */
public class StrUtil {

	private static Logger logger = LoggerFactory.getLogger(StrUtil.class);

	public static String replace(String text, String searchString, String replacement) {
		return replace(text, searchString, replacement, -1);
	}

	/**
	 * <p>
	 * Replaces a String with another String inside a larger String, for the
	 * first <code>max</code> values of the search String.
	 * </p>
	 *
	 * <p>
	 * A <code>null</code> reference passed to this method is a no-op.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.replace(null, *, *, *)         = null
	 * StringUtils.replace("", *, *, *)           = ""
	 * StringUtils.replace("any", null, *, *)     = "any"
	 * StringUtils.replace("any", *, null, *)     = "any"
	 * StringUtils.replace("any", "", *, *)       = "any"
	 * StringUtils.replace("any", *, *, 0)        = "any"
	 * StringUtils.replace("abaa", "a", null, -1) = "abaa"
	 * StringUtils.replace("abaa", "a", "", -1)   = "b"
	 * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
	 * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
	 * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
	 * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
	 * </pre>
	 *
	 * @param text
	 *           text to search and replace in, may be null
	 * @param searchString
	 *           the String to search for, may be null
	 * @param replacement
	 *           the String to replace it with, may be null
	 * @param max
	 *           maximum number of values to replace, or <code>-1</code> if no
	 *           maximum
	 * @return the text with any replacements processed, <code>null</code> if
	 *         null String input
	 */
	public static String replace(String text, String searchString, String replacement, int max) {
		if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
			return text;
		}
		int start = 0;
		int end = text.indexOf(searchString, start);
		if (end == -1) {
			return text;
		}
		int replLength = searchString.length();
		int increase = replacement.length() - replLength;
		increase = (increase < 0 ? 0 : increase);
		increase *= (max < 0 ? 16 : (max > 64 ? 64 : max));
		StringBuffer buf = new StringBuffer(text.length() + increase);
		while (end != -1) {
			buf.append(text.substring(start, end)).append(replacement);
			start = end + replLength;
			if (--max == 0) {
				break;
			}
			end = text.indexOf(searchString, start);
		}
		buf.append(text.substring(start));
		return buf.toString();
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static String[] split(String str, char separatorChar) {
		return splitWorker(str, separatorChar, false);
	}

	public static String[] split(String str, String separatorChars) {
		return splitWorker(str, separatorChars, -1, false);
	}

	/**
	 * Performs the logic for the <code>split</code> and
	 * <code>splitPreserveAllTokens</code> methods that do not return a maximum
	 * array length.
	 *
	 * @param str
	 *           the String to parse, may be <code>null</code>
	 * @param separatorChar
	 *           the separate character
	 * @param preserveAllTokens
	 *           if <code>true</code>, adjacent separators are treated as empty
	 *           token separators; if <code>false</code>, adjacent separators are
	 *           treated as one separator.
	 * @return an array of parsed Strings, <code>null</code> if null String input
	 */
	private static String[] splitWorker(String str, char separatorChar, boolean preserveAllTokens) {
		// Performance tuned for 2.0 (JDK1.4)

		if (str == null) {
			return null;
		}
		int len = str.length();
		if (len == 0) {
			return new String[0];
		}
		List<String> list = new ArrayList<String>();
		int i = 0, start = 0;
		boolean match = false;
		boolean lastMatch = false;
		while (i < len) {
			if (str.charAt(i) == separatorChar) {
				if (match || preserveAllTokens) {
					list.add(str.substring(start, i));
					match = false;
					lastMatch = true;
				}
				start = ++i;
				continue;
			}
			lastMatch = false;
			match = true;
			i++;
		}
		if (match || (preserveAllTokens && lastMatch)) {
			list.add(str.substring(start, i));
		}
		return (String[]) list.toArray(new String[list.size()]);
	}

	/**
	 * Performs the logic for the <code>split</code> and
	 * <code>splitPreserveAllTokens</code> methods that return a maximum array
	 * length.
	 *
	 * @param str
	 *           the String to parse, may be <code>null</code>
	 * @param separatorChars
	 *           the separate character
	 * @param max
	 *           the maximum number of elements to include in the array. A zero
	 *           or negative value implies no limit.
	 * @param preserveAllTokens
	 *           if <code>true</code>, adjacent separators are treated as empty
	 *           token separators; if <code>false</code>, adjacent separators are
	 *           treated as one separator.
	 * @return an array of parsed Strings, <code>null</code> if null String input
	 */
	private static String[] splitWorker(String str, String separatorChars, int max, boolean preserveAllTokens) {
		// Performance tuned for 2.0 (JDK1.4)
		// Direct code is quicker than StringTokenizer.
		// Also, StringTokenizer uses isSpace() not isWhitespace()

		if (str == null) {
			return null;
		}
		int len = str.length();
		if (len == 0) {
			return new String[0];
		}
		List<String> list = new ArrayList<String>();
		int sizePlus1 = 1;
		int i = 0, start = 0;
		boolean match = false;
		boolean lastMatch = false;
		if (separatorChars == null) {
			// Null separator means use whitespace
			while (i < len) {
				if (Character.isWhitespace(str.charAt(i))) {
					if (match || preserveAllTokens) {
						lastMatch = true;
						if (sizePlus1++ == max) {
							i = len;
							lastMatch = false;
						}
						list.add(str.substring(start, i));
						match = false;
					}
					start = ++i;
					continue;
				}
				lastMatch = false;
				match = true;
				i++;
			}
		} else if (separatorChars.length() == 1) {
			// Optimise 1 character case
			char sep = separatorChars.charAt(0);
			while (i < len) {
				if (str.charAt(i) == sep) {
					if (match || preserveAllTokens) {
						lastMatch = true;
						if (sizePlus1++ == max) {
							i = len;
							lastMatch = false;
						}
						list.add(str.substring(start, i));
						match = false;
					}
					start = ++i;
					continue;
				}
				lastMatch = false;
				match = true;
				i++;
			}
		} else {
			// standard case
			while (i < len) {
				if (separatorChars.indexOf(str.charAt(i)) >= 0) {
					if (match || preserveAllTokens) {
						lastMatch = true;
						if (sizePlus1++ == max) {
							i = len;
							lastMatch = false;
						}
						list.add(str.substring(start, i));
						match = false;
					}
					start = ++i;
					continue;
				}
				lastMatch = false;
				match = true;
				i++;
			}
		}
		if (match || (preserveAllTokens && lastMatch)) {
			list.add(str.substring(start, i));
		}
		return (String[]) list.toArray(new String[list.size()]);
	}

	public static String addPathDelim(String aPath) {
		if (aPath != null && !aPath.endsWith("/") && !aPath.endsWith("\\")) {
			return aPath + ShellUtils.getPathDelim();
		} else {
			return aPath;
		}
	}

	/**
	 * This replace all '<%=XXX%>' tags with the given Properties file
	 * 
	 * @param aSource
	 * @param aProperties
	 * @return substitued string
	 */
	public static String subsProperties(String aSource, Properties aProperties) {
		String result = aSource;
		if (aSource != null && aProperties != null) {
			Enumeration<?> propenum = aProperties.keys();
			while (propenum.hasMoreElements()) {
				String key = propenum.nextElement().toString();
				String fromStr = (key.startsWith("<%=")) ? key : "<%=" + key + "%>"; 
				String toStr = aProperties.getProperty(key);
				if (toStr != null) {
					result = replace(result, fromStr, toStr);
				} else {
					logger.warn("subsProperties called with null toString for key " + key);
				}
			}
		}

		return result;
	}

	/**
	 * This replace all '<%=XXX%>' tags with the Set of SusbValue's
	 * 
	 * @param aSource
	 * @param aProperties
	 * @return substitued string
	 */
	public static String subsProperties(String aSource, Set<SubsValue> aSubstitutes) {
		Properties props = new Properties();

		for (SubsValue subsval : aSubstitutes) {
			props.put(subsval.getName(), subsval.getValue());
		}

		return subsProperties(aSource, props);
	}

	/**
	 * this will substitute all "<%=Xxx%>" items with "Xxx"
	 * 
	 * @param aSource
	 * @return
	 */
	public static String subsUnkownItemsInFilename(String aSource) {
		String result = aSource;
		if (result.indexOf("<%=") >= 0) {
			result = result.replace("<%=", "");
			result = result.replace("%>", "");
		}
		return result;
	}

	/**
	 * returns a fully qualified classname give a File and the classpath being
	 * uses
	 * 
	 * @param cp
	 * @param file
	 * @return
	 */
	public static String getFullyQualifiedClassname(String cp, File file) {
		String cpdots = replSlashsToDots(cp);

		String fqn = replSlashsToDots(file.getAbsolutePath());
		fqn = fqn.substring(cpdots.length() + 1);
		fqn = fqn.substring(0, fqn.length() - 6);
		return fqn;
	}

	/**
	 * this replace all (back)slash with '.'
	 * 
	 * @param s
	 * @return
	 */
	public static String replSlashsToDots(String s) {
		String r = StrUtil.replace(s, "/", ".");
		r = StrUtil.replace(r, "\\", ".");
		return r;
	}

	/**
	 * this return the given the fully qualified classname
	 */
	public static String getPackage(String aFqn) {
		int n = aFqn.lastIndexOf(".");
		if (n > 0) {
			return aFqn.substring(0, n);
		} else {
			return aFqn;
		}
	}

}
