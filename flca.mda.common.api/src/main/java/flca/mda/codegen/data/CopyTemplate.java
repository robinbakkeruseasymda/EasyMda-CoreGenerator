package flca.mda.codegen.data;

import flca.mda.codegen.CodegenConstants;

/**
 * This is common template that will be used for "as-is" classes like Exceptions Enums and classes that 
 * have the @CopyFile annotation
 * This annotation can also be used to override values from below
 * @author nly36776
 *
 */
public class CopyTemplate extends Template 
{
	public CopyTemplate()
	{
		super("CopyTemplate", 
				null, 
				null, 
				"<%=Backend%>/<%=src-gen%>", 
				"<%=" + CodegenConstants.PACKAGE + "%>", 
				"<%="+ CodegenConstants.CLASSNAME + "%>", 
				".java", 
				null,
				new Class[]{}, 
				TemplateMergeStrategy.JAVA_MERGE,
				5,
				null);
	}


}
