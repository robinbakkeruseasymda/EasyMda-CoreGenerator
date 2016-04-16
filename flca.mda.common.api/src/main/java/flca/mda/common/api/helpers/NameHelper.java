package flca.mda.common.api.helpers;

public class NameHelper
{
	public static String uncapSafeName(String name) {
		return uncapPrefixedName(name);
	}

	public static String uncapPrefixedName(String name) {
		// lower all except the last upper case character if there are
		// more than one upper case characters in the beginning.
		// e.g. XSDElementContent -> xsdElementContent
		// However if the whole string is uppercase, the whole string
		// is turned into lower case.
		// e.g. CPU -> cpu
		if (name == null || name.length() == 0) {
			return name;
		} else {
			String lowerName = name.toLowerCase();
			int i;
			for (i = 0; i < name.length(); i++) {
				if (name.charAt(i) == lowerName.charAt(i)) {
					break;
				}
			}
			if (i > 1 && i < name.length()) {
				--i;
			}
			return name.substring(0, i).toLowerCase() + name.substring(i);
		}
	}
}
