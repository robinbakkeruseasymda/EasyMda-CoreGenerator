package flca.mda.codegen.helpers;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import flca.mda.common.api.helpers.ImportHelper;

public class TypeHelper {

	private TypeHelper() {}
	
	/**
	 * return the generic type of the given field or the actual type if this is
	 * not a generic type
	 * 
	 * @param aField
	 * @return
	 */
	public static Class<?> _getGenericOrActualType(Field aField) {
		if (aField == null) {
			return null;
		}

		Type gentyp = aField.getGenericType();
		if (gentyp instanceof ParameterizedType) {
			ParameterizedType partyp = (ParameterizedType) gentyp;
			Type[] fieldArgTypes = partyp.getActualTypeArguments();
			if (fieldArgTypes.length == 1) {
				Class<?> fieldArgClass = (Class<?>) fieldArgTypes[0];
				ImportHelper.addImport(fieldArgClass);
				return fieldArgClass;
			} else {
				return null;
			}
		} else {
			ImportHelper.addImport(aField.getType());
			return aField.getType();
		}
	}

}
