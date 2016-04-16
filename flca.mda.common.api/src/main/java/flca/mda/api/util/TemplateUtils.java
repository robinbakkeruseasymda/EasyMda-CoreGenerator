package flca.mda.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import flca.mda.codegen.data.DataStore;
import flca.mda.codegen.data.ITemplate;
import flca.mda.codegen.data.TemplatesStore;
import flca.mda.codegen.helpers.FilenameHelper;
import flca.mda.codegen.helpers.StrUtil;
import flca.mda.common.api.helpers.ImportHelper;

public class TemplateUtils {
	private static Logger logger = LoggerFactory.getLogger(TemplateUtils.class);

	private FilenameHelper fnh = new FilenameHelper();

	public TemplateUtils() {
		super();
	}

	/**
	 * return the ITemplate given the templatename
	 * 
	 * @param aTemplateName
	 * @return
	 */
	public ITemplate getTemplate(String aTemplateName) {
		ITemplate result = TemplatesStore.getInstance().getTemplate(aTemplateName);
		if (result == null) {
			logger.error("No template found for " + aTemplateName);
		}
		return result;
	}

	/**
	 * return the classname that belongs to the given class and templatename
	 * 
	 * @param aTemplateName
	 * @return
	 */
	public String getClassName(String aTemplateName) {
		return getClassName(aTemplateName, true);
	}

	/**
	 * return the classname that belongs to the current class and templatename
	 * 
	 * @param aTemplateName
	 * @return
	 */
	public String getClassName(String aTemplateName, boolean aImport) {
		ITemplate t = getTemplate(aTemplateName);
		if (t == null) {
			System.out.println("no template found for " + aTemplateName);
			logger.error("No template found for " + aTemplateName);
		}
		return getClassName(JetArgument.getCurrent().getElementClass(), t, aImport);
	}

	/**
	 * return the classname that belongs to the given class and templatename
	 * 
	 * @param aTemplateName
	 * @return
	 */
	public String getClassName(Class<?> aClass, String aTemplateName) {
		return getClassName(aClass, aTemplateName, true);
	}

	/**
	 * return the classname that belongs to the given class and templatename
	 * 
	 * @param aTemplateName
	 * @return
	 */
	public String getClassName(Class<?> aClass, String aTemplateName, boolean aImport) {
		ITemplate t = getTemplate(aTemplateName);
		return getClassName(aClass, t, aImport);
	}

	/**
	 * return the classname that belongs to the given class and ITemplate, and
	 * also import this class
	 * 
	 * @param aTemplateName
	 * @return
	 */
	public String getClassName(Class<?> aClass, ITemplate aTemplate) {
		return getClassName(aClass, aTemplate, true);
	}

	/**
	 * return the classname that belongs to the given class and ITemplate
	 * 
	 * @param aTemplateName
	 * @return
	 */
	public String getClassName(Class<?> aClass, ITemplate aTemplate, boolean aImport) {
		FilenameHelper fnh = new FilenameHelper();
		String clzname = fnh.formatClassName(aClass, aTemplate);
		String pck = fnh.formatPackage(aClass, aTemplate);
		if (aImport) {
			ImportHelper.addImport(pck + "." + clzname);
		}
		return clzname;
	}

	/**
	 * return the package that belongs to the given class and templatename
	 * 
	 * @param aTemplateName
	 * @return
	 */
	public String getPackage(Class<?> aClass, String aTemplateName) {
		ITemplate t = getTemplate(aTemplateName);
		return getPackage(aClass, t);
	}

	/**
	 * return the package that belongs to the current class and ITemplate string
	 * 
	 * @param aTemplateName
	 * @return
	 */
	public String getPackage(String aTemplateName) {
		ITemplate t = getTemplate(aTemplateName);
		return getPackage(JetArgument.getCurrent().getElementClass(), t);
	}

	/**
	 * return the package that belongs to the current class and ITemplate
	 * 
	 * @param aTemplateName
	 * @return
	 */
	public String getPackage(ITemplate aTemplate) {
		return getPackage(JetArgument.getCurrent().getElementClass(), aTemplate);
	}

	/**
	 * return the package that belongs to the given class and ITemplate
	 * 
	 * @param aTemplateName
	 * @return
	 */
	public String getPackage(Class<?> aClass, ITemplate aTemplate) {
		String result = new FilenameHelper().formatPackage(aClass, aTemplate);
		return result;
	}

	/**
	 * "see getAppClassName(String aTemplateName, boolean aImport)
	 */
	public String getAppClassName(String aTemplateName) {
		return getAppClassName(aTemplateName, true);
	}

	/**
	 * return the actual 'application' classname of the class that belongs to the
	 * given ITemplate. An application class, is a class that not belongs to a
	 * particul entity, dto or service but to the app as a whole. Hence this
	 * method should be used for templates like APP_CONTEXT, APP_CONSTANTS etc
	 * 
	 * @param aTemplate
	 *           "param aImport
	 * @return
	 */
	public String getAppClassName(String aTemplateName, boolean aImport) {
		ITemplate t = getTemplate(aTemplateName);
		String pck = DataStore.getInstance().getAppPackage();
		if (pck == null) {
			pck = DataStore.getInstance().getBasePackage();
		}
		String result = fnh.formatClassName(DataStore.getInstance().getAppName(), t);
		if (aImport && !StrUtil.isEmpty(result)) {
			ImportHelper.addImport(pck + "." + result);
		}
		return result;
	}

}
