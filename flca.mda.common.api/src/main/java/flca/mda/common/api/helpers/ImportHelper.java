package flca.mda.common.api.helpers;

import org.eclipse.emf.codegen.util.ImportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import flca.mda.api.util.TemplateUtils;
import flca.mda.api.util.TypeUtils;
import flca.mda.codegen.CodegenConstants;
import flca.mda.codegen.helpers.FilenameHelper;

public class ImportHelper {
	
	private static Logger LOGGER = LoggerFactory.getLogger(ImportHelper.class);
	
	private static ImportManager importManager = new ImportManager("");
	private static TypeUtils tu = new TypeUtils();
	private static TemplateUtils tplu = new TemplateUtils();
	private static FilenameHelper fnh = new FilenameHelper();

	public static ImportFilter importFilter = null;
	
	/**
	 * get the current active ImportManager
	 * @return
	 */
	public static ImportManager getImportManager() {
		return importManager;
	}

	/**
	 * Initialize and return a new ImportManager
	 * @return
	 */
	public static ImportManager makeImportManager() {
		importManager = new ImportManager("");
		return importManager;
	}
	
	/**
	 * Add the given class to the current active ImportManager
	 * @param aClass
	 */
	public static void addImport(Class<?> aClass) {
		if (aClass == null) {
			LOGGER.error("addImport: Type to be imported is null.");
		} else {
			String clsname = tu.getClassname(aClass);
			addImport(clsname);
		}
	}

	/**
	 * In Scala, classes can be defined in another class. With this method we can generated the import
	 * of the given string (param1) that is defined in the given class (param2)
	 * example: addScalaImport("FetchXxx", Xxx.class) -> import com.mycomp.model.FetchXxx
	 * @param aClass
	 */
	public static void addScalaImport(String aImportName, Class<?> aClass, String aTemplateName) {
		if (aImportName == null || aImportName.isEmpty() || aClass == null) {
			throw new RuntimeException("Type or name to be imported is null.");
		}

		String pck = new TemplateUtils().getPackage(aClass, aTemplateName);
		addImport(pck + "." + aImportName);
	}
	
	/**
	 * Add the given string to the current active ImportManager, note this should without the 'import ' 
	 * Typically is should a fully qualified classname but you may also add a wilcard like: com.util.*
	 * @param String
	 */

	public static void addImport(String aImport) {
		if (isApplicable(aImport)) {
			doAddImport(fnh.formatPackage(aImport));
		}
	}
	
	private static boolean isApplicable(final String aImport) {
		return aImport != null && aImport.indexOf(CodegenConstants.NOT_APPLICABLE) < 0;
	}

	/**
	 * Add the class that corresponds with the given template to the ImportManager.
	 * Note that this method is only suitable for templates that always have the same target class, 
	 * hence Application type templates
	 * @param template
	 */
	public static void addTemplateImport(String template) {
		if (template != null) {
			String pck = tplu.getPackage(template);
			String clz = tplu.getClassName(template, false);
			doAddImport(pck + "." + clz);
		} else {
			System.out.println("** addTemplateImport called with null template");
		}
	}
	
	/**
	 * Add the class that corresponds with the given class ane template to the ImportManager.
	 * @param template
	 */
	public static void addTemplateImport(Class<?> aClass, String template) {
		if (aClass != null && template != null) {
			String pck = tplu.getPackage(aClass, template);
			String clz = tplu.getClassName(aClass, template, false);
			doAddImport(pck + "." + clz);
		} else {
			System.out.println("** addTemplateImport called with null template and//or class");
		}
	}
	
	/**
	 * ultimately all import should only go through this method. Via the filter we can alter the bahavior
	 * @param aImport
	 */
	
	private static void doAddImport(String aImport) {
		String importStr = (importFilter == null) ? aImport : importFilter.filter(aImport);
		if (importStr != null) {
			if (aImport.endsWith("SetMock")) {
				System.out.print("setmock");
			}
			importManager.addImport(aImport);
		}
	}
}