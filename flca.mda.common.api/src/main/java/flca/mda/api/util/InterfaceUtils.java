package flca.mda.api.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import mda.annotation.JavaDoc;
import mda.annotation.RestMethod;
import mda.annotation.RestService;
import mda.annotation.crud.CrudOperation;
import mda.annotation.crud.Search;
import mda.type.IDtoType;
import mda.type.IEntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import flca.mda.codegen.CodegenConstants;
import flca.mda.codegen.data.DataStore;
import flca.mda.codegen.helpers.AnnotationsHelper;
import flca.mda.codegen.helpers.StrUtil;
import flca.mda.common.api.helpers.ImportHelper;

public class InterfaceUtils {
	protected static Logger logger = LoggerFactory.getLogger(InterfaceUtils.class);

	protected TemplateUtils tplu = new TemplateUtils();
	protected TypeUtils tu = new TypeUtils();

	// map betwee operation-name and corresponding service class
	protected static Map<String, Class<?>> sOperationMap = null; 
	
	// this is map between service interface and all its crud methods.
	protected Map<Class<?>, List<CrudMethod>> crudsMap = null;
	
	/**
	 * returns all relevant Method's in a not null List. Methods like toString()
	 * will be skipped
	 * 
	 * @param aClass
	 * @return
	 */
	public List<Method> getMethods(Class<?> aClass) {
		List<Method> result = new ArrayList<Method>();

		for (Method m : aClass.getMethods()) {
			if (!skipMethod(m)) {
				result.add(m);
			}
		}

		return result;
	}
	
	/**
	 * returns all relevant Method's of the current active class in a not null List. Methods like toString()
	 * will be skipped
	 * 
	 * @param aClass
	 * @return
	 */
	public List<Method> getMethods() {
		Class<?> currIntf = JetArgument.getCurrent().getElementClass();
		return getMethods(currIntf);
	}

	/**
	 * skip all methods obtained from object and all dummy placeholder methods
	 * for crud operations
	 * 
	 * @param aMethod
	 * @return
	 */
	protected boolean skipMethod(Method aMethod) {
		return sSkipMethods.contains(aMethod.getName());
	}

	public String getDocumentation(Method aMethod) {
		String result = null;

		JavaDoc javadocAnno = (JavaDoc) AnnotationsHelper.getAnnotation(aMethod, JavaDoc.class);
		if (javadocAnno != null) {
			return javadocAnno.doc();
		}
		return result;
	}

	/**
	 * return the num
	 * @param aMethod
	 * @return
	 */
	public int getParameterCount(Method aMethod) {
		Class<?> params[] = aMethod.getParameterTypes();
		return params.length;
	}

	/**
	 * Returns all argument names as a formatted String
	 * 
	 * <pre>
	 * void doIt(String aaa, int bbb) results in "String aaa, int bbb"
	 * </pre>
	 * 
	 * @param aMethod
	 * @return
	 */
	public String getParameters(Method aMethod) {
		StringBuffer sb = new StringBuffer();

		String src = getSourceCode();
		List<ParameterData> paramData = (src == null) ? getParametersViaReflection(aMethod)
				                                        : getParametersFromSourcecode(aMethod, src);

		int n = 0;
		for (ParameterData parameterData : paramData) {
			sb.append(parameterData.datatype + " " + parameterData.name);
			if (n++ < paramData.size()-1) sb.append(",  ");
		}
		
		return sb.toString();
	}

	public String getSourceCode() {
		String result = JetArgument.getCurrent().getSourceCode();
		return result;
	}

	/**
	 * Returns all argument names as a formatted String
	 * Note this is similar to getParameterNames
	 * <pre>
	 * void doIt(String aaa, int bbb) results in "[aaa, bbb]"
	 * </pre>
	 * 
	 * @param aMethod
	 * @return
	 */
	public List<String> getArguments(Method aMethod) {
		return getParameterNames(aMethod);
	}

	public List<ParameterData> getParametersFromSourcecode(Method aMethod, String aSourceCode) {
		if (aSourceCode != null) {
			int n = aSourceCode.indexOf(aMethod.getName());
			if (n >= 0) {
				String s1 = aSourceCode.substring(n);
				String s2 = s1.substring(s1.indexOf("("));
				String s3 = s2.substring(1, s2.indexOf(")"));
				
				return parseParametersString(s3);
			} else {
				return null; // we should never come here
			}
		} else {
			return new ArrayList<ParameterData>();
		}
	}

	public List<ParameterData> getParametersViaReflection(Method aMethod) {
		List<ParameterData> result = new ArrayList<ParameterData>();
		Class<?> params[] = aMethod.getParameterTypes();
		for (int i = 0; i < params.length; i++) {
			result.add(new ParameterData("arg" + i, params[i].getName()));
		}
		return result;
	}

	
	public List<ParameterData> parseParametersString(String aArgsString) {
		List<ParameterData> result = new ArrayList<ParameterData>();

		if (aArgsString != null && aArgsString.trim().length() > 0) {
			String items[] = aArgsString.split(",");
			if (items != null) {
				for (int i = 0; i < items.length; i++) {
					String tokens[] = items[i].trim().split(" ");
					if (tokens.length == 2) {
						result.add(new ParameterData(tokens[1], tokens[0]));
					} else {
						logger.error("this should not be possible in getArgumentsFromSourcecode " + aArgsString);
					}
				}
			}
		}

		return result;
	}

	public String getReturn(Method aMethod) {
		Class<?> rettyp = aMethod.getReturnType();
		Type t = aMethod.getGenericReturnType();
		if (t instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) t;
			Object obj = pt.getActualTypeArguments()[0];
			if (obj instanceof Class) {
				ImportHelper.addImport((Class<?>) pt.getRawType());
				ImportHelper.addImport((Class<?>) obj);
				return rettyp.getSimpleName() + "<" + ((Class<?>) obj).getSimpleName() + ">";
			} else {
				return "???";
			}
		} else {
			if (rettyp.equals(Void.class)) {
				return "void";
			} else {
				ImportHelper.addImport(rettyp);
				return rettyp.getSimpleName();
			}
		}
	}

	/**
	 * return true if this method is void
	 * @param aMethod
	 * @return
	 */
	public boolean isVoid(Method aMethod) {
		return void.class.equals(aMethod.getReturnType());
	}

	/**
	 * return the 0-based parameter's nameof the given method. This name will be retrieved from the sourcecode if possible 
	 * @param aMethod
	 * @param aIndex
	 * @return
	 */
	public String getParameterName(Method aMethod, int aIndex) {
		String src = getSourceCode();
		List<ParameterData> paramData = (src == null) ? getParametersViaReflection(aMethod)
				                                        : getParametersFromSourcecode(aMethod, src);
		if (paramData != null && paramData.size() >= aIndex) {
			return paramData.get(aIndex).name;
		} else {
			return "???";
		}
	}

	/**
	 * return all the names of the parameters (without datatype) in a string list 
	 * ex: [id, name]
	 * Note this is similar to getArguments
	 * @param aMethod
	 * @return
	 */
	public List<String> getParameterNames(Method aMethod) {
		String src = getSourceCode();
		List<ParameterData> paramData = (src == null) ? getParametersViaReflection(aMethod)
				                                        : getParametersFromSourcecode(aMethod, src);
		List<String> result = new ArrayList<>();
		for (ParameterData parameterData : paramData) {
			result.add(parameterData.name);
		}
		return result;
	}

	
	/**
	 * returns the class that corresponds with the aIndex's method argument
	 * 
	 * @param aMethod
	 * @param aIndex
	 * @return
	 */
	public Class<?> getParameterType(Method aMethod, int aIndex) {
		Class<?> params[] = aMethod.getParameterTypes();
		// Type types[] = aMethod.getGenericParameterTypes();
		if (params != null) {
			if (aIndex < params.length) {
				return params[aIndex];
			}
		}
		return null;
	}

	/**
	 * return the generic type that corresponds with the aIndex's method argument
	 * for example myMethod(List<Test>): getParameterType(myMethod, 0) -> List
	 * getGenericParameterType(myMethod, 0) -> Test
	 * 
	 * @param aMethod
	 * @param aIndex
	 * @return
	 */
	public Class<?> getGenericParameterType(Method aMethod, int aIndex) {
		Type types[] = aMethod.getGenericParameterTypes();
		if (types != null) {
			if (aIndex < types.length) {
				Type type = types[aIndex];
				if (type instanceof ParameterizedType) {
					Type actTypes[] = ((ParameterizedType) type).getActualTypeArguments();
					if (actTypes.length == 1) {
						return (Class<?>) actTypes[0];
					}
				}
			}
		}
		return null;
	}

	/**
	 * returns the corresponding concrete type given tbe input type. for example
	 * List -> ArrayList, Map -> HashMap etc
	 * 
	 * @param aParmType
	 * @return
	 */
	public Class<?> getConcreteType(Class<?> aParmType) {
		Class<?> result = aParmType;

		if (result.equals(List.class)) {
			return ArrayList.class;
		} else if (result.equals(Collection.class)) {
			return ArrayList.class;
		} else if (result.equals(Map.class)) {
			return HashMap.class;
		}

		return result;
	}

	public Class<?>[] getExceptions(Method aMethod) {
		return aMethod.getExceptionTypes();
	}

	/**
	 * return the Exception as a formatted String or "". Example
	 * "throws DaoExcepion"
	 * 
	 * @param aMethod
	 * @return
	 */
	public String getThrows(Method aMethod) {
		String result = "";

		Class<?> exceptions[] = aMethod.getExceptionTypes();
		if (exceptions.length > 0) {
			result = "throws ";
			for (Class<?> ex : exceptions) {
				result += " " + ex.getSimpleName();
				ImportHelper.addImport(ex);
			}
		}

		return result;
	}

	public boolean hasThrowsException(Method aMethod) {
		return getThrows(aMethod).length() > 1;
	}

	public Annotation[] getAnnotations(Method aMethod) {
		return aMethod.getAnnotations();
	}

	public Annotation getAnnotation(Method aMethod, Class<?> aAnnotation) {
		Annotation[] annots = getAnnotations(aMethod);
		if (annots != null) {
			for (Annotation anno : annots) {
				if (anno.annotationType().equals(aAnnotation)) {
					return anno;
				}
			}
		}
		return null;
	}

	public boolean moreParameters(Method aMethod, int aIndex) {
		int cnt = getParameterCount(aMethod);
		return (aIndex < cnt - 1);
	}

	public String getServiceName(String aOperation) {
		String m = getServiceMethod(aOperation);

		if (m != null) {
			Class<?> clz = getOperationMap().get(aOperation);
			if (clz != null) {
				String pck = StrUtil.getPackage(clz.getName());
				ImportHelper.addImport(pck + ".*");
				return clz.getSimpleName();
			}
		}

		return "// could not find service with method " + aOperation + ", did you forget the Crud annotation?";
	}

	public Class<?> getServiceClass(Class<?> aEntity) {
		// TODO try to make this more generic
		String findoper = "retrieve" + aEntity.getSimpleName();
		return getServiceClass(findoper);
	}

	public Class<?> getServiceClass(String aOperation) {
		String m = getServiceMethod(aOperation);

		if (m != null) {
			Class<?> clz = getOperationMap().get(aOperation);
			if (clz != null) {
				ImportHelper.addImport(clz);
				return clz;
			}
		}

		return null;
	}

	private String getServiceMethod(String aOperation) {
		for (String m : getOperationMap().keySet()) {
			if (aOperation.equals(m)) {
				return m;
			}
		}

		return null;
	}

	public String getServiceFactName(String aOperation) {
		String servicename = getServiceName(aOperation);
		if (servicename != null) {
			return servicename + "Fact"; // TODO try to make this more generic
		}

		return null;
	}

	public String getServiceName(Class<?> aEntity) {
		// TODO try to make this more generic
		String findoper = "retrieve" + aEntity.getSimpleName();
		return getServiceName(findoper);
	}

	public String getServiceFactName(Class<?> aEntity) {
		String findoper = "retrieve" + aEntity.getSimpleName();
		return getServiceFactName(findoper);
	}

	protected static Collection<String> sSkipMethods;
	static {
		sSkipMethods = new HashSet<String>();
		for (String s : CodegenConstants.SKIP_METHODS) {
			sSkipMethods.add(s);
		}
	}

	public Map<String, Class<?>> getOperationMap() {
		if (sOperationMap == null) {
			sOperationMap = new HashMap<String, Class<?>>();

			fillOperationMap();
		}

		return sOperationMap;
	}

	protected void fillOperationMap() {
		for (Class<?> clz : DataStore.getInstance().getModelClasses()) {
			if (clz.isInterface()) {
				for (Method m : clz.getMethods()) {
					if (!skipMethod(m.getName())) {
						sOperationMap.put(m.getName(), clz);
					}
				}
			}
		}
	}

	protected boolean skipMethod(String aMethodname) {
		for (String skip : CodegenConstants.SKIP_METHODS) {
			if (skip.equals(aMethodname)) {
				return true;
			}
		}
		return false;
	}

	public static class ParameterData {
		public String name;
		public String datatype;

		public ParameterData(String name, String datatype) {
			super();
			this.name = name;
			this.datatype = datatype;
		}
	}

	public static void refresh() {
		sOperationMap = null;
	}
	
	//--- crud operattions
	
	public List<CrudMethod> getAllCrudMethods() {
		Class<?> currClz = JetArgument.getCurrent().getElementClass();
		return getAllCrudMethods(currClz);
	}

	public List<CrudMethod> getAllCrudMethods(Class<?> aClass) {
		if (getCrudsMap().containsKey(aClass)) {
			return getCrudsMap().get(aClass);
		} else {
			return new ArrayList<CrudMethod>();
		}
	}

	private Map<Class<?>, List<CrudMethod>> getCrudsMap() {
		if (crudsMap == null) {
			crudsMap = new HashMap<Class<?>, List<CrudMethod>>();

			fillCrudsMap();
		}

		return crudsMap;
	}

	private void fillCrudsMap() {
		for (Class<?> entityClz : DataStore.getInstance().getModelClasses()) {
			if (tu.hasType(entityClz, IEntityType.class) || tu.hasType(entityClz, IDtoType.class)) {
				if (hasCrudOperation(entityClz)) {
					addCrudAnnots(entityClz);
				}
//				if (tu.hasSearchOperation(entityClz)) {
//					addSearchAnnot(entityClz);
//				}
			}
		}
	}

	public List<Class<?>> getAllCrudEntities() {
		List<Class<?>> result = new ArrayList<Class<?>>();

		for (CrudMethod cm : getAllCrudMethods()) {
			Class<?> ent = cm.getEntity();
			if (!result.contains(ent)) {
				result.add(ent);
			}
		}

		return result;
	}

	
	
	private void addCrudAnnots(Class<?> entityClz) {
		RestService crudanno = (RestService) tu.getAnnotation(entityClz, RestService.class);

		if (crudanno != null) {
			List<CrudMethod> list = null;
			if (crudsMap.containsKey(entityClz)) {
				list = crudsMap.get(entityClz);
			} else {
				list = new ArrayList<CrudMethod>();
				crudsMap.put(entityClz, list);
			}

			for (CrudOperation crudoper : getCrudOperations(crudanno)) {
				CrudMethod cm = new CrudMethod(entityClz, crudoper, crudanno);
				if (!exists(list, cm) && !cm.getCrudOper().equals(CrudOperation.SEARCH)) {
					list.add(cm);
				}
			}
		}
	}

	private boolean exists(List<CrudMethod> aList, CrudMethod aCrudMethod) {
		for (CrudMethod cm : aList) {
			if (cm.equals(aCrudMethod)) {
				return true;
			}
		}

		return false;
	}

//	private void addSearchAnnot(Class<?> entityClz) {
//		Search searchanno = tu.getSearchAnnotion(entityClz);
//		Class<?> serviceClz = searchanno.service();
//
//		if (serviceClz != null) {
//			List<CrudMethod> list = null;
//			if (crudsMap.containsKey(serviceClz)) {
//				list = crudsMap.get(serviceClz);
//			} else {
//				list = new ArrayList<CrudMethod>();
//				crudsMap.put(serviceClz, list);
//			}
//
//			CrudMethod cm = new CrudMethod(entityClz, searchanno);
//			list.add(cm);
//		}
//	}
	
	// -- crud methods
	/**
	 * Does this method has any (or all) crud operation. NOT included is the
	 * Search crud
	 * 
	 * @param aMethod
	 * @return
	 */
	public boolean hasCrudOperation(Class<?> aClass) {
		return !getCrudOperations(aClass).isEmpty();
	}

	public boolean hasSearchOperation(Class<?> aClass) {
		return getSearchAnnotion(aClass) != null;
	}

	/**
	 * Does this method has this particular crud operation
	 * 
	 * @param aMethod
	 * @return
	 */
	public boolean hasCrudOperation(Class<?> aClass, CrudOperation aOperation) {
		if (hasCrudOperation(aClass)) {
			List<CrudOperation> crudops = getCrudOperations(aClass);
			for (CrudOperation op : crudops) {
				if (op.name().equals(aOperation.name())) {
					return true;
				}
			}
		}
		return false;
	}

	public List<CrudOperation> getCrudOperations(Class<?> aClass) {
		List<CrudOperation> result = new ArrayList<>(); 
		RestService restsrv = (RestService) tu.getAnnotation(aClass, RestService.class);
		if (restsrv != null) {
			for (CrudOperation crudop : restsrv.operations()) {
				if (crudop.equals(CrudOperation.ALL)) {
					result.add(CrudOperation.FIND_ALL);
					result.add(CrudOperation.RETRIEVE);
					result.add(CrudOperation.SAVE);
					result.add(CrudOperation.REMOVE);
				} else {
					result.add(crudop);
				}
			}
		}
		return result;
	}

	public Search getSearchAnnotion(Class<?> aClass) {
		Annotation[] annots = aClass.getAnnotations();

		for (Annotation anno : annots) {
			if (anno.annotationType().equals(Search.class)) {
				return (Search) anno;
			}
		}
		return null;
	}

	public List<CrudOperation> getCrudOperations(RestService crudanno) {
		List<CrudOperation> result = new ArrayList<CrudOperation>();

		if (crudanno != null) {
			CrudOperation ops[] = crudanno.operations();

			// if ALL was used, then return all
			for (CrudOperation crudoper : ops) {
				if (CrudOperation.ALL.equals(crudoper)) {
					result.add(CrudOperation.FIND_ALL);
					result.add(CrudOperation.SAVE);
					result.add(CrudOperation.REMOVE);
					result.add(CrudOperation.RETRIEVE);
					result.add(CrudOperation.SEARCH);
				}
			}
		}

		return result;
	}
	
	
	/**
	 * returns true is this method should generate a rest GET method (default !) 
	 * @param aMethod
	 */
	public boolean isRestGetMethod(Method aMethod) {
		RestMethod anno = (RestMethod) getAnnotation(aMethod, RestMethod.class);
		return anno == null || anno.GET(); 
	}

	/**
	 * returns true is this method should generate a rest GET method (default !) 
	 * @param aMethod
	 */
	public boolean isRestPostMethod(Method aMethod) {
		RestMethod anno = (RestMethod) getAnnotation(aMethod, RestMethod.class);
		return anno != null && anno.POST(); 
	}
}
