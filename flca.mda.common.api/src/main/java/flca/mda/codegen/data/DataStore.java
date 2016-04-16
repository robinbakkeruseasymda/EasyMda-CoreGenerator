package flca.mda.codegen.data;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import mda.type.IBaseType;
import flca.mda.api.util.TypeUtils;
import flca.mda.codegen.CodegenConstants;
import flca.mda.codegen.helpers.IniFileHelper;
import flca.mda.codegen.helpers.IniFileSection;

/**
 * Preferences Data Store is a helper to maintain the following preferences: -
 * collection of SubsValue's - ini-file value - set of model classes - the model
 * project - the basePackage This datastore is filled during startup by other
 * classes like, via the setXxx methods
 * 
 * @author robin
 * 
 */
public class DataStore {
	private static DataStore sInstance;

	private Set<SubsValue> subsvalues;
	private File modelProjectDir;
	private Map<String, IniFileSection> sections;
	private Set<Class<?>> modelClasses;
	
	private static TypeUtils tu = new TypeUtils();

	public static DataStore getInstance() {
		if (sInstance == null) {
			sInstance = new DataStore();
		}
		return sInstance;
	}

	private DataStore() {
		sections = new HashMap<String, IniFileSection>();
		modelClasses = new HashSet<Class<?>>();

		reset();
	}

	public void reset() {
		subsvalues = new HashSet<SubsValue>();
		modelClasses.clear();
	}

	public Collection<SubsValue> getSubsvalues() {
		return subsvalues;
	}

	public void setSubsvalues(Set<SubsValue> subsvalues) {
		this.subsvalues = subsvalues;
	}

	public File getModelProjectDir() {
		return modelProjectDir;
	}

	public void setModelProjectDir(File modelProjectDir) {
		this.modelProjectDir = modelProjectDir;
	}

	public void setSections(Map<String, IniFileSection> aData) {
		sections = aData;
	}

	/**
	 * get Map of all IniFileSection's
	 * 
	 * @return
	 */
	public Map<String, IniFileSection> getSections() {
		if (sections != null) {
			return sections;
		} else {
			return new HashMap<String, IniFileSection>();
		}
	}

	/**
	 * get Map of all IniFileSection's
	 * 
	 * @return
	 */
	public IniFileSection getSection(String aSectionName) {
		if (sections != null) {
			return sections.get(aSectionName);
		} else {
			return null;
		}
	}

	public void addModelClass(Class<?> aClass) {
		// skip this class if it already exist, and only allow if it is a model class
		if (!modelClasses.contains(aClass) && (tu.hasType(aClass, IBaseType.class) || aClass.isEnum())) {
			modelClasses.add((aClass));
		}
	}


	public String getValue(String aKey) {
		SubsValue subsval = getSubsValue(aKey);
		if (subsval != null) {
			return subsval.getValue();
		} else {
			return null;
		}
	}

	public boolean getBooleanValue(String aKey) {
		if (getValue(aKey) != null) {
			String value = getValue(aKey);
			return value.trim().toLowerCase().startsWith("t");
		} else {
			return false;
		}
	}

	public void addSubsValue(SubsValue aSubsValue) {
		subsvalues.add(aSubsValue);
	}

	public Properties getSubstitutesAsProperties() {
		Properties result = new Properties();

		for (SubsValue subsval : subsvalues) {
			if (subsval.getName() != null && subsval.getValue() != null) {
				result.put(subsval.getName(), subsval.getValue());
			}
		}

		return result;
	}

	public SubsValue getSubsValue(String aName) {
		for (SubsValue subsval : subsvalues) {
			if (aName.equals(subsval.getName())) {
				return subsval;
			}
		}

		return null;
	}

	public Set<Class<?>> getModelClasses() {
		return modelClasses;
	}

	public void setModelClasses(Set<Class<?>> modelClasses) {
		this.modelClasses = modelClasses;
	}

	public boolean containsSubsFrom(String aName) {
		return getSubsValue(aName) != null;
	}

	public void editSubstitute(String aKey, String aValue) {
		mergeSubsValue(aKey, aValue, true);
	}

	public void editSubstitute(String aKey, boolean aValue) {
		String strval = (aValue) ? "true" : "false";
		mergeSubsValue(aKey, strval, true);
	}

	public void mergeSubsValues(Collection<SubsValue> aValues, boolean aOverwrite) {
		if (aValues != null) {
			for (SubsValue subsval : aValues) {
				mergeSubsValue(subsval, aOverwrite);
			}
		}
	}

	public void mergeSubsValue(String aName, String aValue, boolean aOverwrite) {
		SubsValue subval = new SubsValue(aName, aValue);
		mergeSubsValue(subval, aOverwrite);
	}

	public void mergeSubsValue(SubsValue aMergeSubsValue, boolean aOverwrite) {
		if (aMergeSubsValue != null) {
			if (containsSubsFrom(aMergeSubsValue.getName())) {
				SubsValue currSubsVal = getSubsValue(aMergeSubsValue.getName());
				if (aMergeSubsValue.getValue() != null && aMergeSubsValue.getValue().trim().length() > 0) {
					// overwrite existing not-empty value only if flag is true
					if (currSubsVal.getValue() != null && currSubsVal.getValue().trim().length() > 0) {
						if (aOverwrite) {
							currSubsVal.setValue(aMergeSubsValue.getValue());
						}
					} else {
						currSubsVal.setValue(aMergeSubsValue.getValue());
					}
				}
			} else {
				subsvalues.add(aMergeSubsValue);
			}
		}
	}

	/**
	 * This method will parse the easymda.ini file and merge SubsVals
	 * for all keys under [subsvalues] but only if a SubsVal exist for this key.
	 * 
	 * @param aModelProjectDir
	 */
	public void readSavedSubsValues(File aModelProjectDir) {
		modelProjectDir = aModelProjectDir;

		Map<String, IniFileSection> sectmap = IniFileHelper.parseIniFiles(modelProjectDir, new PrefsFileFilter(), false);
		if (sectmap != null) {
			if (sectmap.containsKey(CodegenConstants.PREFERENCES_SUBSVALS_SECTION)) {
				IniFileSection sect = sectmap.get(CodegenConstants.PREFERENCES_SUBSVALS_SECTION);
				for (String key : sect.getMap().keySet()) {
					SubsValue subsval = new SubsValue(key, sect.getMap().get(key), SubsValueType.NONE, null);
					if (getSubsValue(key) != null) {
						mergeSubsValue(subsval, true);
					}
				}
			}
		}
	}

	public String getSectionValue(String aSectionName, String aKey) {
		IniFileSection section = getSections().get(aSectionName);
		if (section != null) {
			String result = new String(section.getValue(aKey));
			return result;
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("template=[");

		for (SubsValue subsval : subsvalues) {
			sb.append(subsval.getName() + "=" + subsval.getValue() + ", ");
		}

		return sb.toString();
	}

	public String getAppName() {
		return getValue(CodegenConstants.APP_NAME);
	}

	public String getAppPackage() {
		return getValue(CodegenConstants.APP_PACKAGE);
	}

	public String getBasePackage() {
		return getValue(CodegenConstants.BASE_PACKAGE);
	}
	
	public boolean isProjectModelClass(String aFqn) {
		for (Class<?> clz : getModelClasses()) {
			if (aFqn.equals(clz.getName())) {
				return true;
			}
		}
		return false;
	}

	// ----------
	// instance
	// Collection<SubsValue> subsvalues;
	// Set<Class<?>> modelClasses;
	// IniFileSection templatesPrefs;
	// File modelProjectDir;
	// String basePackage;
	// Map<String, IniFileSection> sections;

	/**
	 * We need this method to copy the actual data of thee DataStore to a new DataStore object 
	 * because this new one is created by another classloader 
	 * @param loader
	 * @throws Exception
	 */
	public void cloneForApi(ClassLoader loader) throws Exception {
		Class<?> clz = loader.loadClass(DataStore.class.getName());
		Method m1 = clz.getMethod("getInstance", new Class[] {});
		Object datastoreClone = m1.invoke(null, new Object[] {});
		Method m2 = clz.getMethod("setSubsvalues", new Class[] { Set.class });
		m2.invoke(datastoreClone, new Object[] { DataStore.getInstance().getSubsvalues() });
		Method m3 = clz.getMethod("setModelClasses", new Class[] { Set.class });
		m3.invoke(datastoreClone, new Object[] { DataStore.getInstance().getModelClasses() });
		Method m4 = clz.getMethod("setSections", new Class[] { Map.class });
		m4.invoke(datastoreClone, new Object[] { DataStore.getInstance().getSections() });
		Method m5 = clz.getMethod("setModelProjectDir", new Class[] { File.class });
		m5.invoke(datastoreClone, new Object[] { DataStore.getInstance().getModelProjectDir() });
	}

	// --------
	static class PrefsFileFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			return pathname.getPath().endsWith(CodegenConstants.PREFERENCES_VALUES_FNAME);
		}
	}
}
