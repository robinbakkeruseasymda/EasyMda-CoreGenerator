package com.flca.mda.codegen.helpers.beanutils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.flca.mda.codegen.helpers.LogHelper;


public class MethodHelper {

	public static Method getGetter(Object aSourceObject, String aPropertyName) 
	{
		Map<String, MethodCacheData> getters = MethodCache.getGettersFromModel(aSourceObject);
		
		String propname = capName(aPropertyName);
		
		if (getters.containsKey(propname)) {
			MethodCacheData mdata = getters.get(propname);
			return mdata.getMethod();
		} else {
			return null;
		}
	}
	
		public static Method getSetter(Object aSourceObject, String aPropertyName) 
	{
		Map<String, MethodCacheData> setters = MethodCache.getSettersFromModel(aSourceObject);

		String propname = capName(aPropertyName);

		if (setters.containsKey(propname)) {
			MethodCacheData mdata = setters.get(propname);
			return mdata.getMethod();
		} else {
			return null;
		}
	}
	
	private static String capName(String aInputPropertyName)
	{
		if (aInputPropertyName.length() == 0) {
			return aInputPropertyName;
		} else {
			return (aInputPropertyName.substring(0, 1).toUpperCase() + 
					aInputPropertyName.substring(1)).replace(" ", "");
		}
	}

	@SuppressWarnings("unchecked")
	public  static Class<Object> getNestedDtype(MethodCacheData mdata) 
	{
		Class<Object> result = null;
		
    	if (mdata != null && mdata.getDtype() != null) {
    		Class<?> dtype = mdata.getDtype();
   
    		if (dtype.getName().equals("java.util.Set") || dtype.getName().equals("java.util.List")) {
    			
    			Type t = mdata.getMethod().getGenericReturnType();
    			if (t != null && t instanceof ParameterizedType) {
        			ParameterizedType paramtype = (ParameterizedType) t;
        			Type actualTypes[] = paramtype.getActualTypeArguments();
        			if (actualTypes != null && actualTypes.length == 1) {
        				result = (Class<Object>) actualTypes[0];
        			}
    			}
    		} 
    	}
    	
    	return result;
	}
	
	
	/**
	 * This return (a not null) list of MethodCacheData object given the object.
	 * @param aSourceObject
	 * @return
	 */
	public static List<MethodCacheData> getMethodDataList(Object aSourceObject)
	{
		List<MethodCacheData> result = new ArrayList<MethodCacheData>();
		
		Map<String, MethodCacheData> getters = MethodCache.getGettersFromModel(aSourceObject);
		
		for (MethodCacheData methodCacheData : getters.values()) {
			result.add(methodCacheData);
		}
		return result;
	}
	

	/**
	 * This return (a not null) list of MethodCacheData object given the object.
	 * @param aSourceObject
	 * @return
	 */
	public static List<MethodCacheData> getMethodDataList(String aFqClassname)
	{
		List<MethodCacheData> result = new ArrayList<MethodCacheData>();
		
		try {
			Class<?> cls = Class.forName(aFqClassname);
			Object obj = cls.newInstance();
			result = getMethodDataList(obj);
		} catch(Exception ex) {
			LogHelper.error("error in getMethodDataList() ",ex); 
		}
		
		return result;
	}

	/**
	 * This return (a not null) list of all relevant getXxx() methods of the given instance
	 * @param aSourceObject
	 * @return
	 */
	public static List<Method> getAllGetters(Object aSourceObject, boolean aMayHaveParameters)
	{
		List<Method> r = new ArrayList<Method>();
		
		List<MethodCacheData> methods = getMethodDataList(aSourceObject);
		for (MethodCacheData mdata : methods) {
			Method m = mdata.getMethod();
			if (m.getName().startsWith("get")) {
				if (aMayHaveParameters || (!aMayHaveParameters && m.getParameterTypes().length == 0)) {
					r.add(m);
				}
			}
		}
		
		return r;
	}
}
