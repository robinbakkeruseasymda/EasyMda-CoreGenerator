package flca.mda.api.util;

public enum FieldFormat
{
	NAME("%n"),  // fieldname 
	TYPE("%t"),  // datatype
	VAR("%v"), // used in Scala to set val or var or Dart to set var
	DEFAULT("%d"), // default value
	OPTION("%o"),  // datatype surrounded with option if needed
	OPTIONTYP("%o[%t]"), // combi of option + type
	FUNCTION("%f");  // function like this: %f<funcname>
	
	private String value;
	
	FieldFormat(final String aValue) {
		value = aValue;
	}

	public String getValue() {
		return value;
	}
}
