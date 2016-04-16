package com.flca.mda.codegen.helpers.beanutils;

import java.lang.reflect.Method;

/**
 * CZ
 * Name: MethodCachData
 * Hulpclass voor MethodCace
 * @author Robin Bakkerus 870777
 * created: 11-feb-04
 */
public class MethodCacheData {

    private String property;
	private Class<?> dtype;
    private Method method;

	MethodCacheData(String aProp, Class<?> aClass, Method aMethod) 
	{
        property = aProp;
        dtype = aClass;
        method = aMethod;
    }

    /**
     * Returns the dtype.
     * @return Class
     */
    public Class<?> getDtype() {
        return dtype;
    }

    /**
     * Returns the method.
     * @return Method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns the property.
     * @return String
     */
    public String getProperty() {
        return property;
    }

}


