package flca.mda.api.util;

import java.util.Collection;

import flca.mda.codegen.helpers.SourceCodeUtils;
import flca.mda.common.api.helpers.ImportHelper;

public class ImportUtils {
	
	private static SourceCodeUtils su = new SourceCodeUtils();
	
	public ImportUtils() {
		this(true);
	}

	public ImportUtils(final boolean makeImportManager) {
		if (makeImportManager) {
			ImportHelper.makeImportManager();
		}
	}

	/**
	 * Add the given class to the ImportManager.
	 * @param template
	 */
	public void addImport(Class<?> aClass) {
		if (aClass == null) {
			throw new RuntimeException("Type to be imported is null.");
		}

		ImportHelper.addImport(aClass.getName());
	}

	/**
	 * Add the current String to the ImportManager, note just the fully qualified classname withoud "import "
	 * @param aImport
	 */
	public void addImport(String aImport) {
		ImportHelper.addImport(aImport);
	}

	/**
	 * Add the class that corresponds with the given template to the ImportManager.
	 * Note that this method is only suitable for templates that always have the same target class, 
	 * hence Application type templates
	 * @param template
	 */
	public void addTemplateImport(String template) {
		ImportHelper.addTemplateImport(template);
	}

	/**
	 * Add the class that corresponds with the given class ane template to the ImportManager.
	 * @param template
	 */
	public void addTemplateImport(Class<?> aClass, String template) {
		
		ImportHelper.addTemplateImport(aClass, template);
	}

	/**
	 * In Scala, classes can be defined in another class. With this method we can generated the import
	 * of the given string (param1) that is defined in the given class (param2)
	 * example: addScalaImport("FetchXxx", Xxx.class) -> import com.mycomp.model.FetchXxx
	 * @param aClass
	 */
	public void addScalaImport(String name, Class<?> clz, String template) {
		ImportHelper.addScalaImport(name, clz, template);
	}

	/**
	 * Shortcut to add the simplenname and also import the class
	 * 
	 * @param aClass
	 * @return
	 */
	public String use(Class<?> aClass) {
		addImport(aClass);
		return aClass.getSimpleName();
	}

	public void addCompilationUnitImports(String aImports) {
		ImportHelper.getImportManager().addCompilationUnitImports(aImports);
	}

	public String computeSortedImports() {
		return ImportHelper.getImportManager().computeSortedImports();
	}

	public void addAllImportsFromSource() {
		String src = JetArgument.getCurrent().getSourceCode();
		Collection<String> allImports = su.getAllImports(src);
		for (String fqn : allImports) {
			addImport(fqn);
		}
	}
}