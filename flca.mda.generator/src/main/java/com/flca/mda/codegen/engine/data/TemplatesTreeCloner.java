package com.flca.mda.codegen.engine.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import flca.mda.codegen.data.TemplatesBranch;
import flca.mda.codegen.data.TemplatesTree;

/**
 * This class copies the data from a source TemplatesTree to a target Templates
 * tree
 * <p>
 * We have to this because of classloading issues
 * <p>
 * We could something like Dozer or Xstream but I dont want these dependecies
 * for these simple classes
 * 
 * @author nly36776
 * 
 */
public class TemplatesTreeCloner {

	public TemplatesTreeCloner() {
		super();
	}

	/**
	 * This will create a TemplatesTree from the instance that implements
	 * IRegisterTemplates.
	 * 
	 * @param aSource
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public TemplatesTree clone(Object aSource) throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		TemplatesTree result = new TemplatesTree();

		Class<?> clz = aSource.getClass();

		Method m1 = clz.getMethod("getName", new Class[] {});
		result.setName((String) m1.invoke(aSource, new Object[] {}));

		Method m2 = clz.getMethod("getDescription", new Class[] {});
		result.setDescription((String) m2.invoke(aSource, new Object[] {}));

		Method m3 = clz.getMethod("getTemplateBranches", new Class[] {});
		List<?> branches = (List<?>) m3.invoke(aSource, new Object[] {});
		for (Object object : branches) {
			result.addBranch( ((TemplatesBranch) object).cloneMe());
		}

		return result;
	}

}
