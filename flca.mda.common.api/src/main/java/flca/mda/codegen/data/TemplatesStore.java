package flca.mda.codegen.data;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mda.type.IRegisterTemplates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplatesStore {
	private static TemplatesStore sInstance;

	// List to hold available cartrdiges (either bin of jar)
	private List<File> allCartridges = new ArrayList<File>();

	// Map to hold TemplatesStoreData of enabled cartrdiges. Key is classpath
	// File
	private Map<File, TemplatesStoreData> dataMap = new HashMap<File, TemplatesStoreData>();

	private static Logger logger = LoggerFactory.getLogger(TemplatesStore.class);

	public static TemplatesStore getInstance() {
		if (sInstance == null) {
			sInstance = new TemplatesStore();
		}
		return sInstance;
	}

	public Map<File, TemplatesStoreData> getData() {
		return dataMap;
	}

	public void setData(Map<File, TemplatesStoreData> aValue) {
		dataMap = aValue;
	}

	public void clear() {
		dataMap.clear();
		allCartridges.clear();
		// postProcessors.clear();
	}

	/**
	 * return all cartridge classpath File(s)
	 * 
	 * @return
	 */
	public Set<File> getAllTemplatePaths() {
		return dataMap.keySet();
	}

	/**
	 * returns the cartridge that belong to given classpath
	 * 
	 * @param aFoldersName
	 * @return
	 */
	public TemplatesTree getTemplatesTree(String aFoldersName) {
		if (dataMap.get(aFoldersName) != null) {
			return dataMap.get(aFoldersName).getTemplatesTree();
		} else {
			return null;
		}
	}

	/**
	 * This returns the IRegisterTemplates given a template
	 * 
	 * @param aFoldersName
	 * @return
	 */
	public IRegisterTemplates getRegisterTemplates(ITemplate aTemplate)  {
		Class<?> clz = findTemplatesStoreData(aTemplate);
		if (clz != null) {
			try {
				return (IRegisterTemplates) clz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException("Unable to find class that implement IRegisterType");
			}
		} else {
			throw new RuntimeException("Unable to find class that implement IRegisterType");
		}
	}

	private Class<?> findTemplatesStoreData(ITemplate aTemplate) {
		for (TemplatesStoreData data : dataMap.values()) {
			for (TemplatesBranch branch : data.getTemplatesTree().getBranches()) {
				for (ITemplate template : branch.getTemplates()) {
					if (template.equals(aTemplate)) {
						return data.getRegisterClass();
					}
				}
			}
		}
		return null;
	}

	/**
	 * This will add a TemplatesStoreData to the allCartridges list. We need this
	 * when we are going to save preferences
	 * 
	 * @param aFoldersName
	 * @param aRegisterTemplates
	 * @param aTemplatesTree
	 */
	public void add(File aFoldersName) {
		allCartridges.add(aFoldersName);
	}

	public List<File> getAllCartridges() {
		return allCartridges;
	}

	/**
	 * This will add a TemplatesStoreData to the map
	 * 
	 * @param aFoldersName
	 * @param aRegisterTemplates
	 * @param aTemplatesTree
	 */
	public void add(File aFoldersName, Class<?> aRegisterTemplates, TemplatesTree aTemplatesTree) {
		TemplatesStoreData data = new TemplatesStoreData(aFoldersName, aRegisterTemplates, aTemplatesTree);
		dataMap.put(aFoldersName, data);
	}

	/**
	 * This return a ITemplate given the identifier (the name) of the template.
	 * It is assumed that all templates names are unique!
	 * 
	 * @param aTemplateName
	 * @return
	 */
	public ITemplate getTemplate(String aTemplateName) {
		for (TemplatesStoreData data : dataMap.values()) {
			if (data.getTemplatesTree() != null) {
				for (TemplatesBranch branch : data.getTemplatesTree().getBranches()) {
					for (ITemplate template : branch.getTemplates()) {
						if (aTemplateName.equals(template.getName())) {
							return template;
						}
					}
				}
			}
		}
		logger.error("no template found for " + aTemplateName);
		return null;
	}

	// -----------
	public void cloneForApi(ClassLoader aLoader) throws Exception {
		// TemplatesStore.sLoader = aLoader;
		Class<?> clz = aLoader.loadClass(TemplatesStore.class.getName());
		Method m1 = clz.getMethod("getInstance", new Class[] {});
		Object templatestoreClone = m1.invoke(null, new Object[] {});
		Method m2 = clz.getMethod("setData", new Class[] { Map.class });
		m2.invoke(templatestoreClone, new Object[] { TemplatesStore.getInstance().getData() });
	}

}
