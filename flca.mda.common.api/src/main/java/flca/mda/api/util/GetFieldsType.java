package flca.mda.api.util;

public enum GetFieldsType {

	ALL, 
	NONE,  
	
	SIMPLE,  // int Integer, Date etc, enum
	ENUM, // enum field
	SUPER, // fields from the super class if any

	RELATIONS, // any of the ones below
	ONETOONE,
	ONETOMANY,
	MANYTOONE,
	MANYTOMANY,

	VAL, // applicable for reactive cartridge
	VAR, // applicable for reactive cartridge

	// special fields
	SPECIAL, 
	ID, // the implicit id, if no specific prop with @ID is available
	DISCRIMINATOR, // the disscriminator used for classes with @Inheritance
	OFD,  // ,,
	;
}
