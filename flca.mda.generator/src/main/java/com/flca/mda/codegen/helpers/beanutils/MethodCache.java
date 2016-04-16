package com.flca.mda.codegen.helpers.beanutils;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * CZ
 * Name: MethodCache
 * @author Robin Bakkerus 870777
 * created: 11-feb-04
 */
public class MethodCache {
	private static HashMap<String, HashMap<String,MethodCacheData>> sGettersCache = new HashMap<String, HashMap<String,MethodCacheData>>();
	private static HashMap<String, HashMap<String,MethodCacheData>> sSettersCache = new HashMap<String, HashMap<String,MethodCacheData>>();

	/**
	 * @param model
	 * @return HashMap mat daarin 0 of meerdere MethodCache.Data objecten
	 */
	public static HashMap<String, MethodCacheData> getGettersFromModel(Object model) {
		String key = model.getClass().getName();

		if (!sGettersCache.containsKey(key)) {
			fillCache(key, model);
		}

		return sGettersCache.get(key);
	}

	/**
	 * @param model Object
	 * @return
	 */
	public static HashMap<String, MethodCacheData> getSettersFromModel(Object model) {
		String key = model.getClass().getName();

		if (!sSettersCache.containsKey(key)) {
			fillCache(key, model);
		}

		return sSettersCache.get(key);
	}

	private static synchronized void fillCache(String key, Object model) {
		sGettersCache.put(key, getGetters(model));
		sSettersCache.put(key, getSetters(model));
	}

	/**
	 * @param model Object
	 * @return
	 */
	private static HashMap<String,MethodCacheData> getGetters(Object model) {
		HashMap<String,MethodCacheData> map = new HashMap<String,MethodCacheData>();
//		FastClass fastclazz = FastClass.create(model.getClass());
		Method methods[] = model.getClass().getMethods();

		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
//			FastMethod fm = fastclazz.getMethod(m);
			if (isGetter(m)) {
				String prop = getProperty(m.getName());
				Class<?> dtype = m.getReturnType();
				if (!dtype.equals(model.getClass())) {
					MethodCacheData data =	new MethodCacheData(prop, dtype, m);
					
					map.put(prop, data);
				}
			}
		}

		return map;
	}

	/**
	 * Method getSetters.
	 * @param model
	 * @return HashMap
	 */
	private static HashMap<String, MethodCacheData> getSetters(Object model) {
		HashMap<String, MethodCacheData> map = new HashMap<String, MethodCacheData>();
		Method[] methods = model.getClass().getMethods();

		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			if (isSetter(m)) {
				String prop = getProperty(m.getName());
				//Er moet 1 propertie voor zijn en niet meer en niet minder!
				if (m.getParameterTypes().length == 1) {
					Class<?> dtype = m.getParameterTypes()[0];
					MethodCacheData data =
						new MethodCacheData(prop, dtype, m);

					map.put(prop, data);
				}
			}
		}

		return map;
	}

	//TODO DAT ER EEN CORRESPONDERENDE SETTER MET ZELFDE ARGUMENT
	private static boolean isGetter(final Method aMethod) {
		String methodName = aMethod.getName();
		if (methodName.equals("getClass")) {
			return false;
		} else {
			if (methodName.startsWith("get")) {
				Class<?> args[] = aMethod.getParameterTypes();
				if (args == null || args.length == 0) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	private static boolean isSetter(final Method aMethod) {
		String methodName = aMethod.getName();
		if (methodName.startsWith("set")) {
			Class<?> args[] = aMethod.getParameterTypes();
			if (args != null && args.length == 1) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private static String getProperty(final String methodName) {
		if (methodName.startsWith("set") || methodName.startsWith("get")) {
			return methodName.substring(3);
		} else if (methodName.startsWith("is")) {
			return methodName.substring(2);			
		} else {
			return null;
		}
	}
}
