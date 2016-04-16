package flca.mda.codegen.helpers;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import flca.mda.codegen.CodegenConstants;
import flca.mda.codegen.data.DataStore;
import flca.mda.codegen.data.ITemplate;

/**
 * The targetfile is made up from the following element, all of which are defined in an ITemplate:
 * targerdir : via getTargetDir() ex: "/tmp/";
 * package   : via getPackage() ex: "test." + <%=PACKAGE%> + ".dao"
 * classname : via getClassname() ex: "Test" + <%=CLASSNAME%> + "Dao" 
 * extension : via getFileExtension() ex: ".java"
 * The above will generate the 'default' target output file.
 * 
 * Then there are also three special SubsVal's : APP_NAME, APP_PACKAGE and BASE_PACKAGE
 * APP_NAME : a string that defines the application name (like "Demo"). This name is often used to generated so-called app classes
 * like DemoConstants.java
 * APP_PACKAGE : If defined, this package will replace the following
 * BASE_PACKAGE : This is the start of the package that is the same for all model classes
 * For example: in the model project <b>all</b> classes may start with: "org.mycomp.mda.model" 
 * By setting APP_PACKAGE = "com.abcomp.app" 
 * The model class: "org.mycomp.mda.model.crm.Customer" will be generated to: com.abcomp.app.crm.Customer" 
 *  
 * In addition this default target filename can modified via the ITemplate tags:
 * overrideAppPackage() and getOverrideAppPackage()
 * If this is set, then the start of the package may be replaced. Th
 * 
 *  
 * @author robin
 *
 */
public class FilenameHelper {
	private static Logger logger = LoggerFactory.getLogger(FilenameHelper.class);

	/**
	 * this will return the name of the package of a given model classname and template
	 * 
	 * @param aClass
	 * @return
	 */
	public String formatClassName(Class<?> aModelClass, ITemplate aTemplate) {
		if (aModelClass != null) {
			return formatClassName(aModelClass.getSimpleName(), aTemplate);
		} else {
			return "?class=null?";
		}
	}

	/**
	 * this will return the name of the package of a given model class and template
	 * 
	 * @param aClass
	 * @return
	 */
	public String formatClassName(String aClassname, ITemplate aTemplate) {
		if (aTemplate != null) {
			Properties props = new Properties();
			props.put(CodegenConstants.CLASSNAME, aClassname);

			String appname = DataStore.getInstance().getAppName();
			if (appname != null) {
				props.put(CodegenConstants.APP_NAME, appname);
			} else {
				logger.warn("No application name is defined");
			}

			String result = StrUtil.subsProperties(aTemplate.getClassname(), props);
			result = StrUtil.subsProperties(result, getSubsValsAsProperties());
			result = StrUtil.subsUnkownItemsInFilename(result);
			return result;
		} else {
			logger.warn("formatClassName called with null template");
			return aClassname;
		}
	}

	/**
	 * this will return the to be generated output package
	 * @param aModelClass
	 * @param aTemplate
	 * @return
	 */
	public String formatPackage(Class<?> aModelClass, ITemplate aTemplate) {
		if (aTemplate != null) {
			Properties props = new Properties();
			props.put(CodegenConstants.PACKAGE,  formatPackage(aModelClass.getPackage().getName()));
			props.put(CodegenConstants.APP_PACKAGE, getOutputAppPackage());

			String result = StrUtil.subsProperties(aTemplate.getPackage(), props);
			result = StrUtil.subsProperties(result, getSubsValsAsProperties());
			result = StrUtil.subsUnkownItemsInFilename(result);
			result = result.replace("/", ".");
			result = result.replace("\\", ".");
			return result;
		} else {
			logger.warn("formatPackage called with null template");
			return aModelClass.getSimpleName();
		}
	}
	
	/**
	 * this will return the name of the package of a given classname, where the
	 * base-package (that are the start package dirs used in the model project
	 * for all classes for ex. com.model.) with app-package that was filled in
	 * the gui. This is used when generating import statements, and output
	 * files.
	 * 
	 * @param aClass
	 * @return
	 */
	public String formatPackage(String aClassName) {
		String result = aClassName;
		String apppck = DataStore.getInstance().getAppPackage();
		String basePck = DataStore.getInstance().getBasePackage();
		if (apppck != null && basePck != null) {
			if (result.startsWith(basePck)) {
				result = StrUtil.replace(result, basePck, apppck);
			}
		} else {
			//TODO alleen laten zien als plugin starten
			// logger.error("application package and/or basepackage is null, this should not be possible");
		}

		if (result.startsWith(".")) {result = result.substring(1); }
		return result;
	}

	/**
	 * return the package used for the 'APP' packages.
	 * @return
	 */
	public String getOutputAppPackage() {
		String apppck = DataStore.getInstance().getAppPackage();
		if (apppck != null) {
			return apppck;
		} else {
			String basePck = DataStore.getInstance().getBasePackage();
			if (basePck != null && basePck.length() > 0) {
				return basePck;
			} else {
				logger.error("No application package is defined, did you forgot to copy applicationsetup.ini file?");// TODO
				return "";
			}
		}
	}
	
	public String generateTargetFilename(String templateOutputFilename, Class<?> aModelClass, ITemplate aTemplate) {
		String result = templateOutputFilename;

		Properties plusprops = (Properties) getSubstitutesAsProperties().clone();

		plusprops.put(CodegenConstants.CLASSNAME, aModelClass.getSimpleName());
		plusprops.put(CodegenConstants.PACKAGE, getThePackage(aModelClass));
		plusprops.put(CodegenConstants.APP_PACKAGE, getTheAppPackage(aTemplate));
		final String appname = DataStore.getInstance().getAppName();
		if (appname != null) {
			plusprops.put(CodegenConstants.APP_NAME, DataStore.getInstance().getAppName());
		}

		result = StrUtil.subsProperties(result, plusprops);
		result = StrUtil.subsUnkownItemsInFilename(result);

		return result;
	}
	
	private String getThePackage(Class<?> aModelClass) {
		String pck = formatPackage(aModelClass.getPackage().getName());
		pck = StrUtil.replace(pck, ".", "/");
		return pck;
	}	

	private String getTheAppPackage(ITemplate aTemplate) {
		String result = getAppPackage();	
		
		if (!StrUtil.isEmpty(result)) {
			result = StrUtil.replace(result, ".", "/");
		}
		
		return result;
	}

	private String getAppPackage() {
		String result = DataStore.getInstance().getAppPackage();	
		
		if (result == null) {
			result = DataStore.getInstance().getBasePackage();
		}
		
		return result;
	}

	private Properties getSubstitutesAsProperties() {
		if (DataStore.getInstance() != null) {
			return DataStore.getInstance().getSubstitutesAsProperties();
		} else {
			return new Properties();
		}
	}

	public String getRelativeToRootPath(Class<?> aModelClass, ITemplate aTemplate) {
		return getRelativeToRootPath(aModelClass, aTemplate, 0);
	}

	/**
	 * return the relative path to the root folder, from the given Class
	 * ex: com.test.Test => "../../"
	 * and with aPlusdirs = -1; com.test.Test => "../"
	 * @param aModelClass
	 * @param aTemplate
	 * @param aPlusSubdirs
	 * @return
	 */
	public String getRelativeToRootPath(Class<?> aModelClass, ITemplate aTemplate, int aPlusSubdirs) {
		String pck = formatPackage(aModelClass, aTemplate);
		String subdirs[] = pck.split("\\.");
		StringBuffer sb = new StringBuffer();
		for (int i=0; i < (subdirs.length + aPlusSubdirs) ; i++) { 
			sb.append(".." + "/");
		}
		return sb.toString();
	}

	private Properties getSubsValsAsProperties() {
		return DataStore.getInstance().getSubstitutesAsProperties();
	}


}
