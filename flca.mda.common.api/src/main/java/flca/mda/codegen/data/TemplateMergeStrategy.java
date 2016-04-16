package flca.mda.codegen.data;

public enum TemplateMergeStrategy {

	JAVA_MERGE, 
	OVERWRITE, 
	MERGE,
	SIMPLE_MERGE, 
	SKIP;

	public static TemplateMergeStrategy make(String aName)
	{
		if ("SKIP".equals(aName)) {
			return TemplateMergeStrategy.SKIP;
		} else if ("SIMPLE_MERGE".equals(aName)) {
			return TemplateMergeStrategy.SIMPLE_MERGE;
		} else if ("MERGE".equals(aName)) {
			return TemplateMergeStrategy.MERGE;
		} else if ("OVERWRITE".equals(aName)) {
			return TemplateMergeStrategy.OVERWRITE;
		} else {
			return TemplateMergeStrategy.JAVA_MERGE;
		}
	}
}
