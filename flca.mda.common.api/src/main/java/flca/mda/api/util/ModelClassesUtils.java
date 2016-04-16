package flca.mda.api.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mda.type.IApplicationType;


import flca.mda.codegen.data.DataStore;
import flca.mda.codegen.helpers.AnnotationsHelper;


public class ModelClassesUtils {

	public static Logger logger = LoggerFactory.getLogger(ModelClassesUtils.class);

	private static TypeUtils tu = new TypeUtils();
	
	/**
	 * find all (not only the selected ones) classes that contains the given class annotation
	 * @param annot
	 * @return
	 */
	public static List<Class<?>> findModelClassesWithAnnotation(Annotation annot) {
		List<Class<?>> result = new ArrayList<Class<?>>();
		
		for (Class<?> clazz : DataStore.getInstance().getModelClasses()) {
			if (AnnotationsHelper.hasAnnotation(clazz, annot)) {
				result.add(clazz);
			}
		}
		return result;
	}

	/**
	 * find all (not only the selected ones) classes that implement the given interface
	 * @param annot
	 * @return
	 */
	public static List<Class<?>> findModelClassesWithInterface(Class<?> aInterface) {
		List<Class<?>> result = new ArrayList<Class<?>>();
		
		for (Class<?> clazz : DataStore.getInstance().getModelClasses()) {
			if (tu.hasType(clazz, aInterface)) {
				result.add(clazz);
			}
		}
		return result;
	}
	
	/**
	 * find all (not only the selected ones) classes that implement the given interface
	 * @param annot
	 * @return
	 */
	public static IApplicationType findApplicationType() {
		
		List<Class<?>>  clazzes = findModelClassesWithInterface(IApplicationType.class);
		if (clazzes != null && clazzes.size() == 1) {
			try {
				return (IApplicationType) clazzes.get(0).newInstance();
			} catch(Exception ex) {
				logger.error("error creating ApplicationType class ", ex);
				return null;
			}
		} else {
			if (clazzes == null || clazzes.size() == 0) {
				logger.error("Could not find a class that extends ApplicationBaseType in the model project");
			} else {
				logger.error("Found more then one class that extends ApplicationBaseType in the model project");
			}
			return null;
		}
	}
	
	/**
	 * find all (not only the selected ones) enums
	 * @return
	 */
	public static List<Class<?>> findModelEnums() {
		List<Class<?>> result = new ArrayList<Class<?>>();
		
		for (Class<?> clazz : DataStore.getInstance().getModelClasses()) {
			if (tu.isEnum(clazz)) {
				result.add(clazz);
			}
		}
		return result;
	}
	
}
