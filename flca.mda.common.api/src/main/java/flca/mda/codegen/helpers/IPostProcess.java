package flca.mda.codegen.helpers;

import flca.mda.codegen.data.ITemplate;

public interface IPostProcess {

	String parse(String aInput, ITemplate aTemplate, Class<?> aTargetClass);
	
	boolean isApplicable(ITemplate aTemplate);
}
