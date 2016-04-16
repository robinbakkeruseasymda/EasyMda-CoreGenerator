package flca.mda.codegen;

public interface CodegenConstants
{
	String TEMPLATES_EXT_POINT = "com.flca.mda.generator.templates";
	
	// these 4 constants are used in Template definitions
	String CLASSNAME = "CLASSNAME";
	String PACKAGE = "PACKAGE";
	String APP_NAME = "App-name";
	String APP_PACKAGE = "App-package";

	// the base package will be substitued by the app-package
	String BASE_PACKAGE = "Base-package";
	
	// this const indicated what subsFrom vallues will be substituted automatically by the codegenerator
	String AUTO_SUBSTRITUTES = CLASSNAME + ";" + PACKAGE + ";" + APP_NAME + ";" + APP_PACKAGE;
	
	String JUNIT_TEST = "junit-test";
	// set this in 
	String OVERWRITE_WITHOUT_MERGING = "OVERWRITE_WITHOUT_MERGING";
	
	//--- constants that deal with SubsValue(s) and other prefs
	String PREFERENCES_VALUES_FNAME = "easymda.ini";
	String PREFERENCES_TEMPLATES_SECTION = "use.cartridges";
	String PREFERENCES_SUBSVALS_SECTION = "subsvalues";
	
	//--- constants for subsvalues that are set by the Generator into the DataStore and can be read by TypeUtils
	String DATASTORE_CARTRIDGE_DIR = "datastore.cartridge.dir";
	String DATASTORE_MODEL_DIR = "datastore.model.dir";
	
	// Do NOT change in any way existing files
	String SUBSVAL_SKIP_ALL_EXISTING = "SkipAllExistingFiles";
	// Generate all files to the TEMP dir
	String SUBSVAL_GENERATE_ALL_TMPDIR = "GenerateAllToTmpDir";
	// The 'Users' folder where additional resource like assets for mobile can be retrieved.
	String SUBSVAL_USER_SOURCE_DIRS = "UserSourceDirs";
	
	String INI_GLOBAL_SETTINGS = "global.settings";
//	String INI_USE_FLCA_CONTEXT = "use.flca.context";
	
	//constants for subsdirs
	String PLUGIN_CONFIG = "config";
	String PLUGIN_CARTRIDGES = "cartridges";
	String PLUGIN_API_LIB = "api-lib";
	
	//used to indicate that a file doen't have to be generated
	String NOT_APPLICABLE = "_NotApplicable_";
	
	String SKIP_METHODS[] = {
		"equals",
		"getClass",
		"toString",
		"hashCode",
		"isProxyClass",
		"notify",
		"notifyAll",
		"wait",
		"newProxyInstance",
		"getProxyClass",
		"getInvocationHandler",
		"forName",
		"isAssignableFrom",
		"getSuperclass",
		"isInstance",
		"getModifiers",
		"isInterface",
		"isArray",
		"isPrimitive",
		"getComponentType",
		"getName",
		"asSubclass",
		"cast",
		"desiredAssertionStatus",
		"getAnnotation",
		"getAnnotations",
		"getCanonicalName",
		"getClassLoader",
		"getClasses",
		"getConstructor",
		"getConstructors",
		"getDeclaredAnnotations",
		"getDeclaredClasses",
		"getDeclaredConstructor",
		"getDeclaredConstructors",
		"getDeclaredField",
		"getDeclaredFields",
		"getDeclaredMethod",
		"getDeclaredMethods",
		"getDeclaringClass",
		"getEnclosingClass",
		"getEnclosingConstructor",
		"getEnclosingMethod",
		"getEnumConstants",
		"getField",
		"getFieldsExc",
		"getGenericInterfaces",
		"getGenericSuperclass",
		"getInterfaces",
		"getMethod",
		"getMethods",
		"getPackage",
		"getProtectionDomain",
		"getResource",
		"getResourceAsStream",
		"getSigners",
		"getSimpleName",
		"getTypeParameters",
		"isAnnotation",
		"isAnnotationPresent",
		"isAnonymousClass",
		"isEnum",
		"isLocalClass",
		"isMemberClass",
		"isSynthetic",
		"newInstance",
	};

}
