package flca.mda.api.util;

import java.lang.reflect.Field;
import java.util.Properties;


public class SnippetUtils
{
	protected NameUtils nu = new NameUtils();
	protected TypeUtils tu = new TypeUtils();
	
	/**
	 * Return the string from a given section and key from an ini file. The content of string may contain a number of 
	 * SubsValue's. They are indicated in similar way in a jet file, using the <%=xxx%> syntax. These SubsValues are passed
	 * via the last Properties param. 
	 * You dont need to give the name of the ini file, because the generator will scan all ini files from classpath from the cartridge.
	 * @param aSectionName
	 * @param aSectionKey
	 * @param aSubswith
	 * @return
	 */
	public String getSnippet(String aSectionName, String aSectionKey, Properties aSubswith)
	{
		String result = nu.substituteSnippet(aSectionName, aSectionKey, aSubswith);
		return result;
	}
	
	/**
	 * Return the string from a given section and key from an ini file. The content of string may contain a number of 
	 * SubsValue's. They are indicated in similar way in a jet file, using the <%=xxx%> syntax. These SubsValues are passed
	 * via the last <b>pair</b> of from-to value(s). 
	 * You dont need to give the name of the ini file, because the generator will scan all ini files from classpath from the cartridge.
	 * @param aSectionName
	 * @param aSectionKey
	 * @param aSubswith
	 * @return
	 */
	public String getSnippet(String aSectionName, String aSectionKey, String ... aSubswith)
	{
		Properties subsprops = new Properties();
		if (aSubswith != null) {
			for (int i=0; i < aSubswith.length; i=i+2) {
				subsprops.put(aSubswith[i], aSubswith[i+1]);
			}
		}
		String result = nu.substituteSnippet(aSectionName, aSectionKey, subsprops);
		return result;
	}

	
	/**
	 * Return the string from a given section and key from an ini file. The content of string may contain a number of 
	 * SubsValue's. They are indicated in similar way in a jet file, using the <%=xxx%> syntax. These SubsValues are passed
	 * via the last <b>pair</b> of from-to value(s). 
	 * You dont need to give the name of the ini file, because the generator will scan all ini files from classpath from the cartridge.
	 * Note the key being used the type of the input Field in lowercase 
	 * @param aSectionName
	 * @param aSectionKey
	 * @param aSubswith
	 * @return
	 */
	public String getSnippet(Fw fw, String aSectionName, Properties aSubswith)
	{
		String fldtyp = fw.typeName();
		if (fw.isEnum()) {
			fldtyp = "enum";
		}
		String result = nu.substituteSnippet(aSectionName, fldtyp.toLowerCase(), aSubswith);
		return result;
	}
	
	/**
	 * Alias for the TypeUtils.include() method 
	 * use this method if you want to include generated code from a jet template
	 * indicated with the snippet class. The second param is the argument that
	 * is passed to this jet template. The major difference with the
	 * <b>generate</b> method is that this is not called via an ITemplate but
	 * with class of the jet file.
	 * @param aSnippetClass
	 * @param aArguments
	 * @return
	 */
	public String getSnippet(Class<?> aSnippetClass, Object ...aArguments) {
		return tu.include(aSnippetClass, aArguments);
	}

	/**
	 * Alias for the TypeUtils.include() method 
	 * use this method if you want to include generated code from a jet template
	 * indicated with the snippet class. The second param is the argument that
	 * is passed to this jet template. The major difference with the
	 * <b>generate</b> method is that this is not called via an ITemplate but
	 * with class of the jet file.
	 * @param aSnippetClass
	 * @param aArguments
	 * @return
	 */
	public String getSnippet(Class<?> aSnippetClass, JetArgument aJetArgument) {
		return tu.include(aSnippetClass, aJetArgument);
	}
}
