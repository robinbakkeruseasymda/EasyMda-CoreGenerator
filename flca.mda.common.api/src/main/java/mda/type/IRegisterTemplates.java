package mda.type;

import java.util.Collection;

import flca.mda.codegen.data.SubsValue;
import flca.mda.codegen.data.TemplatesBranch;

/**
 * This interface should be used in project that contain templates
 * @author robin
 *
 */
public interface IRegisterTemplates
{
	/**
	 * this return the name of this tree of template branches
	 * @return 
	 */
	String getName();
	
	/**
	 * this return the description
	 * @return 
	 */
	String getDescription();
	
	/**
	 * returns the version (handy for debugging)
	 * @return
	 */
	String getVersion();
	
	/**
	 * This should return all the templates that you want to publish for this cartridge
	 * @return
	 */
	Collection<TemplatesBranch> getTemplateBranches();
	
	/**
	 * This should return values that can be used by the codegenerator to substitute the corr value
	 * common substitues are the ones that are used to generate output filename
	 * <pre>
	  	Map<Targets, String> result = new HashMap<Targets, String>();
		result.put("Backend", "c:/temp/demo/backend");
		result.put("Frontend", "c:/temp/demo/frontend");
		return p;
	 * </pre>
	 * @return
	 */
	Collection<SubsValue> getSubstituteValues();

	/**
	 * Use this method to init action(s) before the first class is generated. 
	 * Note this is NOT template and/or class specific!
	 */
	void doBefore();
	
	/**
	 * Use this method execute actions after all classes have been generated and saved!
	 * Note this is NOT template and/or class specific!
	 */
	void doAfter();
	
}
