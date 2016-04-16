package flca.mda.api.util;

public interface TypeConstants {
	
	String Q = "\"";
	String NL = "\n";
	String ID = "id";
	String ID_SUFFIX = "Id";
	String TAB = "\t";
	String TAB2 = TAB + TAB;
	String TAB3 = TAB + TAB + TAB;

	String NUMERIC_FIELDS = "int integer short long double float bigdecimal ";
	String INT_FIELDS = "int integer short long";
	String DEC_FIELDS = "double float bigdecimal ";
	String DATE_FIELDS = "java.util.Date java.sql.Timestamp ";
	
	GetFieldsModus EXC = GetFieldsModus.EXCLUDE; 
	GetFieldsModus INC = GetFieldsModus.INCLUDE;

	FwSelectType O2O_FLD = FwSelectType.ONETOONE;
	FwSelectType O2M_FLD = FwSelectType.ONETOMANY;
	FwSelectType M2O_FLD = FwSelectType.MANYTOONE;
	FwSelectType M2M_FLD = FwSelectType.MANYTOMANY;
	FwSelectType ID_FLD = FwSelectType.ID;
	FwSelectType OFD_FLD = FwSelectType.OFD;
	FwSelectType DISC_FLD = FwSelectType.DISCRIMINATOR;
	FwSelectType REL_FLD = FwSelectType.RELATIONS;
	FwSelectType VAL_FLD = FwSelectType.VAL;
	FwSelectType VAR_FLD = FwSelectType.VAR;
}
