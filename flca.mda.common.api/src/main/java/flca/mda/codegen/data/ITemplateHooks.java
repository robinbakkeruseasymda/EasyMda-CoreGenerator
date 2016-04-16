package flca.mda.codegen.data;

import java.io.File;


public interface ITemplateHooks {

	/**
	 * use this method to initialize action before the generator starts
	 * Here you may, for example set the ImportFilter
	 * @param aCurrentClass, the class that is going to be generated
	 * @param aTemplate with this ITemplate
	 */
	void doBeforeGenerate(Class<?> aCurrentClass, ITemplate aTemplate);

	/**
	 * use this method to initialize actions after the generator created the source, but before this file is saved, to targetfile 
	 * This can be used for example to run postprocessors on the generated source code.
	 * 
	 * @param aGeneratedSource String generated source
	 * @param aCurrentClass, the class that is going to be generated
	 * @param aTemplate with this ITemplate
	 * @param aTargetFile, the target that will be used to save the generated source code, unless the result string is null or empty  
	 */
	String doAfterGenerate(String aGeneratedSource, Class<?> aCurrentClass, ITemplate aTemplate);
	
	/**
	 * Use this code to merge the new generated code with the existing old code.
	 * @param aGeneratedCode
	 * @param aOldCode
	 * @param aCurrentClass
	 * @param aTemplate
	 * @return
	 */
	String doMerge(String aNewCode, File aOldFile, Class<?> aCurrentClass, ITemplate aTemplate);
	
	/**
	 * Use this code to change the default generated output filename with your own definition.
	 * This will be called inside SaveGeneratedCodeHelper.save() if it is supplied.
	 * @param aGeneratedFilename
	 * @param aTemplate
	 * @param aCurrentClass
	 * @return
	 */
	String doGenerateFilename(String aGeneratedFilename, ITemplate aTemplate, Class<?> aCurrentClass);
	
	/**
	 * use this method if you want to copy a certain kind of Java model class, as-is
	 * @param aModelclass
	 * @return
	 */
	boolean copyModelClass(Class<?> aModelclass);

}
