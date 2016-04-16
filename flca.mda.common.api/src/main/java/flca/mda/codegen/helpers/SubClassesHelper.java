package flca.mda.codegen.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flca.mda.api.util.TypeUtils;
import flca.mda.codegen.data.DataStore;

public class SubClassesHelper 
{
	private static Map<Class<?>, List<Class<?>>> sSubclassesMap;
	
	private static TypeUtils tu = new TypeUtils();

	public static void initialize() {
		sSubclassesMap = null;
	}
	
	public static List<Class<?>> getAllSubClasses(Class<?> aFromClass) 
	{
		if (sSubclassesMap == null) {
			sSubclassesMap = fillSubclassesMap();
		}
		
		List<Class<?>> result = sSubclassesMap.get(aFromClass);
		if (result == null) {
			result = new ArrayList<Class<?>>();
		}
		
		return result;
	}
	
	private static Map<Class<?>, List<Class<?>>>  fillSubclassesMap() 
	{
		sSubclassesMap = new HashMap<Class<?>, List<Class<?>>>();

		for (Class<?> clz : DataStore.getInstance().getModelClasses()) {
			Class<?> superclasses[] = tu.getAllSuperTypes(clz);
			if (superclasses != null && superclasses.length > 0) {
				for (int i = superclasses.length-1; i >= 0; i--) {
					Class<?> c = superclasses[i];
					List<Class<?>> subclasses;
					if (sSubclassesMap.containsKey(c)) {
						subclasses = sSubclassesMap.get(c);
					} else {
						subclasses = new ArrayList<Class<?>>();
						sSubclassesMap.put(c, subclasses);
					}
					
					subclasses.add(clz);
					
					for (int j = i-1; j > 0; j--) {
						subclasses.add(superclasses[j]);
					}
				}
			}
		}
		
		return sSubclassesMap;
	}
}
