package flca.mda.codegen.data;




public interface ITemplate extends Comparable<ITemplate>
{
	/**
	 * Name (or Id) of this template
	 * @return
	 */
	String getName();
	
	/**
	 * filly qualified classname of corresponding jet generator
	 * @return
	 */
	String getGeneratorFqn();

	/**
	 * File of the corresponding jetFile
	 * @return
	 */
	String getJetPath();
	
	/**
	 * The generated package
	 * @return
	 */
	String getPackage();
	
	/**
	 * The generated classname
	 * @return
	 */
	String getClassname();

	/**
	 * example ".java"
	 * @return
	 */
	String getFileExtension();

	/**
	 * returns the <b>relative</b> target output folder  
	 * @return
	 */
	String getTargetDir();
	
	/**
	 * Returns the name of the targetoutput file (ex CustormerDao.java)
	 * @return
	 */
	String getOutputFilename();

	/**
	 * Indicates for waht class(es) this template is applicable
	 * @return
	 */
	Class<?>[] getApplyToClasses();
	
	/**
	 * inidicates on what model class(es) this template will be applied
	 * @param aSourceClass
	 * @return
	 */
	boolean appliesTo(Class<?> aSourceClass);

	/**
	 * Indicates what MergeStrategy will be used
	 * @return
	 */
	TemplateMergeStrategy getMergeStrategy();

	/**
	 * indicated in what order this template will be applied
	 */
	int getRank();
	
	/**
	 * returns the name of the (parent) cartridge to which this template belongs 
	 * @return
	 */
	String getCartridgeName();
	
	/**
	 * returns the name of the (parent) branch to which this template belongs 
	 * @return String
	 */
	String getBranchName();
	
//	/*
//	 * SubsValue(s) that explitely defined
//	 */
//	Collection<SubsValue> getAskForSubstitutes();

	/**
	 * returns the tag that is used to indicate that the same file is used to write the generated content at the given tag.
	 * the tag is indicated with the "#" character, in classname definition.
	 * In most cases this method will return null, because templates will generate new files!
	 */
	String getInsertionTag();
	
	/**
	 * returns the hooks that may be applies (null is allowed)
	 * @return
	 */
	ITemplateHooks getHooks();
	
   /**
    * clone this ITemplate	
    * @return
    */
	ITemplate cloneMe();
}
