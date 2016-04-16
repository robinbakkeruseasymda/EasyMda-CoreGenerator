package flca.mda.api.util;

/**
 * This enum is used in the getFieldsExc() method to indicate what mode is used.
 * INCLUDE starts from an empty list and include the given field type(s)
 * EXCLUDE starts from a full list and excludes the given field type(s)
 * @author ed45064
 *
 */
public enum GetFieldsModus {

	INCLUDE,
	EXCLUDE;
}
