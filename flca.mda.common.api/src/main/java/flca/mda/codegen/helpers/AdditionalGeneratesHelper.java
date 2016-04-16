package flca.mda.codegen.helpers;

import java.util.ArrayList;
import java.util.List;

import flca.mda.codegen.data.ITemplate;

/**
 * helper class to maintain addtional classes that should be generated with a
 * given template. this class is triggered by the engine after all selected
 * classes and templates have been processed. during this process, one (or more)
 * generators may put an additional class plus template to the list maintained
 * here that will then be processed by the engine. For example, the Rest jet
 * template put's the Entity or Dto class plus the JsonArray template to this
 * list when it finds out the return parameter is a collection.
 * 
 * @author robin
 * 
 */
public class AdditionalGeneratesHelper {

	private static List<Data> list; // we can use not a Map because we may have several templates for the same class

	public static void add(Class<?> aClass, ITemplate aTemplate, Object ... aArguments) {
		if (list == null) {
			list = new ArrayList<Data>();
		}

		AdditionalGeneratesHelper.Data adddata = new AdditionalGeneratesHelper.Data(aClass, aTemplate, aArguments);
		if (!list.contains(adddata)) {
			list.add(adddata);
		}
	}

	public static int size() {
		return (list != null) ? list.size() : 0;
	}

	public static Class<?> getClass(int aIndex) {
		if (list != null) {
			return list.get(aIndex).clazz;
		} else {
			return null;
		}
	}

	public static ITemplate getTemplate(int aIndex) {
		if (list != null) {
			return list.get(aIndex).template;
		} else {
			return null;
		}
	}

	public static Object[] getArguments(int aIndex) {
		if (list != null) {
			return list.get(aIndex).arguments;
		} else {
			return null;
		}
	}
	
	public static void reset() {
		list = new ArrayList<AdditionalGeneratesHelper.Data>();
	}

	// ----------- inner class to hold class + corr template ---
	static class Data {
		Class<?> clazz;
		ITemplate template;
		Object arguments[];

		public Data(Class<?> clazz, ITemplate aTemplate, Object ... aArguments) {
			super();
			this.clazz = clazz;
			this.template = aTemplate;
			this.arguments = aArguments;
			
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
			result = prime * result + ((template == null) ? 0 : template.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			Data other = (Data) obj;
			if (!clazz.equals(other.clazz))
				return false;
			if (!template.equals(other.template))
				return false;
			return true;
		}

	}
}
