package flca.mda.api.util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mda.annotation.jpa.Inheritance;
import mda.annotation.jpa.JoinTable;
import mda.annotation.jpa.ManyToMany;
import mda.annotation.jpa.OneToMany;
import mda.annotation.jpa.Table;
import mda.type.IApplicationType;
import mda.type.IDtoType;
import mda.type.IEntityType;
import mda.type.IServiceType;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import flca.mda.codegen.CodegenConstants;
import flca.mda.codegen.data.DataStore;
import flca.mda.codegen.data.ITemplate;
import flca.mda.codegen.data.SubsValue;
import flca.mda.codegen.helpers.AdditionalGeneratesHelper;
import flca.mda.codegen.helpers.AnnotationsHelper;
import flca.mda.codegen.helpers.FilenameHelper;
import flca.mda.codegen.helpers.SubClassesHelper;
import flca.mda.common.api.helpers.ImportHelper;

public class TypeUtils implements TypeConstants {

	public static final Logger logger = LoggerFactory.getLogger(TypeUtils.class);

	protected static NameUtils nu = new NameUtils();
	protected static ValidatorUtils vu = new ValidatorUtils();
	private static FilenameHelper fnh = new FilenameHelper();

	public static Map<Class<?>, Class<?>> CONCRETE_TYPES = null;
	public static Set<String> COLLECTION_TYPES = null;

	/**
	 * General utility class used in de jet files.
	 */
	public TypeUtils() {
	}

	/**
	 * This will convert the given object to an instance of the given class. We
	 * need this method because the model class was loaded a different
	 * classloader.
	 * 
	 * @param aFromObject
	 * @param toClass
	 * @return
	 */
	public Object cast(Object aFromObject, Class<?> toClass) {
		try {
			Class<?> targetClz = Class.forName(toClass.getName());
			Object result = targetClz.newInstance();
			BeanUtils.copyProperties(result, aFromObject);
			return result;
		} catch (Exception e) {
			logger.warn("error casting " + aFromObject.getClass().getName() + " : " + e);
			return null;
		}
	}

	/**
	 * Returns the primitive or class name for the given Type. Class names will
	 * be added as imports to the GenModel's ImportManager, and the imported
	 * form will be returned.
	 */
	public String getImportedConcreteType(Class<?> aClass, Class<?> aGenericType) {
		Class<?> concreteClass = getConcreteClass(aClass);

		if (!concreteClass.isPrimitive()) {
			ImportHelper.addImport(concreteClass);
		}
		if (aGenericType != null && !aGenericType.isPrimitive()) {
			ImportHelper.addImport(aGenericType);
		}

		if (isCollection(aClass)) {
			if (aGenericType != null) {
				return concreteClass.getSimpleName() + "<" + aGenericType.getSimpleName() + ">";
			} else {
				return concreteClass.getSimpleName() + "<?>";
			}
		} else {
			return aClass.getSimpleName();
		}
	}

	/**
	 * return the generic type of the given field or the actual type if this is
	 * not a generic type
	 * 
	 * @param aField
	 * @return
	 */
	protected Class<?> getGenericOrActualType(Field aField) {
		if (aField == null) {
			return null;
		}

		Type gentyp = aField.getGenericType();
		if (gentyp instanceof ParameterizedType) {
			ParameterizedType partyp = (ParameterizedType) gentyp;
			Type[] fieldArgTypes = partyp.getActualTypeArguments();
			if (fieldArgTypes.length == 1) {
				Class<?> fieldArgClass = (Class<?>) fieldArgTypes[0];
				ImportHelper.addImport(fieldArgClass);
				return fieldArgClass;
			} else {
				return null;// should not be possible
			}
		} else {
			ImportHelper.addImport(getTypeName(aField.getType()));
			return aField.getType();
		}
	}

	/**
	 * return all the supertypes except Object for the given class, this loops
	 * recursively up the hiarchy
	 * 
	 * @param aClass
	 * @return
	 */
	public Class<?>[] getAllSuperTypes(Class<?> aClass) {
		List<Class<?>> result = new ArrayList<Class<?>>();

		getAllSuperTypes(aClass, result);
		getAllInterfaces(aClass, result);

		Class<?> types[] = new Class[0];
		return result.toArray(types);
	}

	/**
	 * get all the interfaces for the given class, this loops recursively up the
	 * hiarchy
	 * 
	 * @param aClass
	 * @return
	 */
	public Class<?>[] getAllInterfaces(Class<?> aClass) {
		List<Class<?>> result = new ArrayList<Class<?>>();
		getAllInterfaces(aClass, result);
		Class<?> types[] = new Class[0];
		return result.toArray(types);
	}

	private void getAllInterfaces(Class<?> aClass, List<Class<?>> aResult) {
		Class<?> interfaces[] = aClass.getInterfaces();
		for (Class<?> intf : interfaces) {
			aResult.add(intf);
			getAllInterfaces(intf, aResult);
		}
	}

	private void getAllSuperTypes(Class<?> aClass, List<Class<?>> aResult) {
		Class<?> clz = aClass.getSuperclass();
		while (clz != null && !clz.equals(Object.class)) {
			aResult.add(clz);
			clz = clz.getSuperclass();
		}
	}

	/**
	 * returns boolean to indicate if given class implemenst or extends the
	 * aTestForClass
	 * 
	 * @param aSourceClass
	 * @param aTestForClass
	 * @return
	 */
	public boolean hasType(Class<?> aSourceClass, Class<?> aTestForClass) {
		Class<?> clazzes[] = getAllSuperTypes(aSourceClass);
		for (Class<?> clz : clazzes) {
			if (clz.getName().equals(aTestForClass.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * returns boolean to indicate if given class implements the aTestForClass
	 * 
	 * @param aSourceClass
	 * @param aTestForClass
	 * @return
	 */
	public boolean hasInterface(Class<?> aSourceClass, Class<?> aTestForClass) {
		Class<?> clazzes[] = getAllInterfaces(aSourceClass);
		for (Class<?> clz : clazzes) {
			if (clz.getName().equals(aTestForClass.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * return all Field(s) (properties) of the given class
	 * 
	 * @param aSourceClass
	 * @return
	 */
	public List<Field> getAllTheFields(Class<?> aSourceClass) {
		List<Field> result = new ArrayList<Field>();

		Field fields[] = aSourceClass.getDeclaredFields();
		for (Field field : fields) {
			result.add(field);
		}

		return result;
	}

	protected Fw makeIdFieldWrapper(Class<?> ownerClass) {
		return new IdFieldWrapper(this, ownerClass);
	}

	protected List<Fw> getSpecialFields(Class<?> aClass, FwSelectType... selectOpts) {
		return new ArrayList<Fw>();
	}

	protected boolean hasSelectOption(FwSelectType checkOption, FwSelectType... selectOpts) {
		for (FwSelectType opt : selectOpts) {
			if (opt.equals(checkOption))
				return true;
		}
		return false;
	}

	public void importIfNeeded(Fw aField) {
		importIfNeeded(aField.type(), aField.genericType());
	}

	public void importIfNeeded(Class<?> aClass, Type aGenericType) {
		if (!aClass.isPrimitive()) {
			ImportHelper.addImport(aClass);
		}
		if (aGenericType != null && !aGenericType.equals(aClass)) {
			if (aGenericType instanceof ParameterizedType) {
				ParameterizedType typeimpl = (ParameterizedType) aGenericType;
				for (Type type : typeimpl.getActualTypeArguments()) {
					ImportHelper.addImport(type.getClass());
				}
			}
		}
	}

	/**
	 * returns boolean to indicatie that this field needs to be surounded with
	 * an Option[]
	 * 
	 * @param fw
	 * @return
	 */
	public boolean needsOption(Fw fw) {
		return false;
	}

	public List<Class<?>> getAllImportedClasses() {
		return getAllImportedClasses(currentClass());
	}

	/**
	 * given the source class, return all the non-simplme classes that are being
	 * used by this class.
	 * 
	 * @param aSourceClass
	 * @return
	 */
	public List<Class<?>> getAllImportedClasses(Class<?> aSourceClass) {
		List<Class<?>> result = new ArrayList<Class<?>>();
		for (Fw fld : getAllFields(aSourceClass)) {
			if (fld.isEnum() || !fld.isSimple()) {
				result.add(fld.type());
			}
		}
		return result;
	}

	/**
	 * return the given field annotation of the given field, or null if this
	 * field is not annotated
	 * 
	 * @param aField
	 * @param aAnnotation
	 * @return
	 */
	public Annotation getAnnotation(Fw aField, Class<?> aAnnotation) {
		return AnnotationsHelper.getAnnotation(aField, aAnnotation);
	}

	/**
	 * return the given Class annotation of the given class, or null if this
	 * field is not annotated
	 * 
	 * @param aClass
	 *            Class
	 * @param aAnnotation
	 *            Class
	 * @return
	 */
	public Annotation getAnnotation(Class<?> aClass, Class<?> aAnnotation) {
		return AnnotationsHelper.getAnnotation(aClass, aAnnotation);
	}

	/**
	 * return the given Class annotation of the given class, or null if this
	 * field is not annotated
	 * 
	 * @param aClass
	 *            Class
	 * @param aAnnotation
	 *            Annotation
	 * @return
	 */
	public Annotation getAnnotation(Class<?> aClass, Annotation aAnnotation) {
		return AnnotationsHelper.getAnnotation(aClass, aAnnotation);
	}

	/**
	 * return the given method annotation of the given method, or null if this
	 * field is not annotated
	 * 
	 * @param aMethod
	 * @param aAnnotation
	 * @return
	 */
	public Annotation getAnnotation(Method aMethod, Class<?> aAnnotation) {
		return AnnotationsHelper.getAnnotation(aMethod, aAnnotation);
	}

	/**
	 * return true of the given field annotation is defined on the given field
	 * 
	 * @param aField
	 * @param aAnnotation
	 *            Class
	 * @return
	 */
	public boolean hasAnnotation(Fw aField, Class<?> aAnnotation) {
		return getAnnotation(aField, aAnnotation) != null;
	}

	/**
	 * return true of the given field annotation is defined on the given field
	 * 
	 * @param aField
	 * @param aFqNameToLookfor
	 *            fully qualified classname String
	 * @return
	 */
	public boolean hasAnnotation(Fw aField, String aFqNameToLookfor) {
		return AnnotationsHelper.getAnnotation(aField, aFqNameToLookfor) != null;
	}

	/**
	 * return true of the given Class annotation is defined on the given class
	 * 
	 * @param aTargetClass
	 * @param aAnnotation
	 * @return
	 */
	public boolean hasAnnotation(Class<?> aTargetClass, Class<?> aAnnotation) {
		return AnnotationsHelper.getAnnotation(aTargetClass, aAnnotation) != null;
	}

	/**
	 * return true of the given Class annotation is defined on the given fully
	 * qualified classname
	 * 
	 * @param aTargetClass
	 * @param aFqNameToLookfor
	 * @return
	 */
	public boolean hasAnnotation(Class<?> aTargetClass, String aFqNameToLookfor) {
		return AnnotationsHelper.getAnnotation(aTargetClass, aFqNameToLookfor) != null;
	}

	/**
	 * return true of the given method annotation is defined on the given method
	 * 
	 * @param aMethod
	 * @param aAnnotation
	 * @return
	 */
	public boolean hasAnnotation(Method aMethod, Class<?> aAnnotation) {
		return AnnotationsHelper.getAnnotation(aMethod, aAnnotation) != null;
	}

	/**
	 * return true of the given method annotation is defined on the given fully
	 * qualified classname
	 * 
	 * @param aMethod
	 * @param aFqNameToLookfor
	 * @return
	 */
	public boolean hasAnnotation(Method aMethod, String aFqNameToLookfor) {
		return AnnotationsHelper.getAnnotation(aMethod, aFqNameToLookfor) != null;
	}

	/**
	 * return true if the given class is an Enum
	 * 
	 * @param aClass
	 * @return
	 */
	public boolean isEnum(Class<?> aClass) {
		return aClass.isEnum();
	}

	/**
	 * return true if the given class is a collection (ie Set, List, etc)
	 * 
	 * @param aClass
	 * @return
	 */
	public boolean isCollection(Class<?> aClass) {
		return (getCollectionTypes().contains(aClass.getName()));
	}

	/**
	 * this returns the concrete class of given class, this may be overwritten
	 * in for example ScalaTypeUtils etc for example the
	 * 
	 * <pre>
	 * Set -> HashSet
	 * List -> ArrayList
	 * </pre>
	 * 
	 * @param aCollectionIntf
	 * @return
	 */
	public Class<?> getConcreteClass(Class<?> aClass) {
		if (getConcreteTypes().containsKey(aClass)) {
			return getConcreteTypes().get(aClass);
		} else {
			return aClass;
		}
	}

	public String getAnnotations(Class<?> aClass) {
		logger.error("getAnnotations() methods should be overwritten");
		return "getAnnotations() should be overwritten";
	}

	public String getAnnotations(Fw fw) {
		logger.error("getAnnotations() methods should be overwritten");
		return "getAnnotations() should be overwritten";
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public String getDocumentation() {
		String result = null;

		result = ""; // TODO

		return result;
	}

	/**
	 * This return the class that belong the primary key of the given class
	 * 
	 * @param aClass
	 * @return
	 */
	public Class<?> getIdType(Class<?> aClass) {
		Fw pkfld = getExplicitIdField(aClass);
		if (pkfld != null) {
			Class<?> result = pkfld.type();
			ImportHelper.addImport(result);
			return result;
		}

		return Long.class;
	}

	public Class<?> getIdType() {
		return getIdType(currentClass());
	}

	/**
	 * returns the name of the fieldtype that defines the primary key for the
	 * given class
	 * 
	 * @return
	 */
	public String getIdTypeName(Class<?> aClass) {
		return getIdType(aClass).getSimpleName();
	}

	/**
	 * returns the name of the fieldtype that defines the primary key for the
	 * current active entity
	 * 
	 * @return
	 */
	public String getIdTypeName() {
		return getIdType(currentClass()).getSimpleName();
	}

	/**
	 * returns the field that defines the primary key for the given class
	 * 
	 * @return
	 */
	public Fw getExplicitIdField(Class<?> aClass) {
		return getExplicitIdField(getFields(aClass, INC, FwSelectType.SIMPLE));
	}

	/**
	 * returns the field that defines the primary key for the given class
	 * 
	 * @return
	 */
	public Fw getExplicitIdField(List<Fw> fields) {
		for (Fw fw : fields) {
			if (!fw.isSpecial() && AnnotationsHelper.hasAnnotation(fw, mda.annotation.jpa.Id.class)) {
				return fw;
			}
		}

		return null;
	}

	/**
	 * return the fieldname of the field that defines the primary
	 * 
	 * @param aClass
	 * @return
	 */
	public String getIdName(Class<?> aClass) {
		Fw pkfld = getExplicitIdField(aClass);
		if (pkfld != null) {
			return pkfld.name();
		}

		return "id";
	}

	public boolean hasExplicitId(Class<?> aClass) {
		return getExplicitIdField(aClass) != null;
	}

	public boolean isEntity(Class<?> aClass) {
		return hasType(aClass, IEntityType.class);
	}

	public boolean isDto(Class<?> aClass) {
		return hasType(aClass, IDtoType.class);
	}

	public boolean isService(Class<?> aClass) {
		return hasType(aClass, IServiceType.class);
	}

	/**
	 * use this method if no explicit @Id field exists
	 * 
	 * @return
	 */
	public String generateIdField() {
		ImportHelper.addImport("javax.persistence.Id");
		ImportHelper.addImport("javax.persistence.GeneratedValue");
		ImportHelper.addImport("javax.persistence.GenerationType");
		String result = "\t@Id\n";
		result += "\t@GeneratedValue(strategy = GenerationType.AUTO) \n";
		result += "\tprivate Long id;\n";
		return result;
	}

	public String generateIdGetterSetter() {
		if (hasExplicitId(currentClass())) {
			return generateExplicitIdGetterSetter();
		} else {
			return generateStdIdGetterSetter();
		}
	}

	/**
	 * use this method if no an explicit @Id is provided.
	 * 
	 * @return
	 */
	private String generateExplicitIdGetterSetter() {
		Class<?> clz = currentClass();
		String explicitIdName = getIdName(clz);
		String explicitIdType = getIdTypeName(clz);
		StringBuffer sb = new StringBuffer();
		sb.append("\tpublic " + explicitIdType + " " + explicitIdGetter() + " {\n");
		sb.append("\t\treturn this." + explicitIdName + ";\n");
		sb.append("\t}\n");
		sb.append("\tpublic void set" + nu.capName(explicitIdName) + "(" + explicitIdType + " aValue) {\n");
		sb.append("\t\t this." + explicitIdName + " = aValue;\n");
		sb.append("\t}\n");

		return sb.toString();
	}

	/**
	 * use this method if no explicit @Id field exists. TODO make more
	 * 'plugable' (using ini file?)
	 * 
	 * @return
	 */
	private String generateStdIdGetterSetter() {
		StringBuffer sb = new StringBuffer();
		sb.append("\tpublic Long getId() {\n");
		sb.append("\t\t" + "return id;\n");
		sb.append("\t}\n");
		sb.append("\tpublic void setId(Long aValue) {\n");
		sb.append("\t\t" + "id = aValue;\n");
		sb.append("\t}\n\n");
		return sb.toString();
	}

	public String idGetter() {
		if (hasExplicitId(currentClass())) {
			return explicitIdGetter();
		} else {
			return "getId()";
		}
	}

	private String explicitIdGetter() {
		Class<?> clz = currentClass();
		String explicitIdName = getIdName(clz);
		return "get" + nu.capName(explicitIdName) + "()";
	}

	/**
	 * return all subclasses of the given class.
	 * 
	 * @see SubClassHelper
	 * @return
	 */
	public List<Class<?>> getSubClasses(Class<?> aClass) {
		return SubClassesHelper.getAllSubClasses(aClass);
	}

	/**
	 * return the superclass of the given class. Note it will NOT return the
	 * obvious Object.class, if the superclass is Object.class then null is
	 * returned
	 * 
	 * @return
	 */
	public Class<?> getSuperClass(Class<?> aClass) {
		Class<?> result = aClass.getSuperclass();
		if (result != Object.class) {
			return result;
		} else {
			return null;
		}
	}

	/**
	 * Does this class has a SubClass ?
	 * 
	 * @param aClass
	 * @return
	 */
	public boolean hasSubClasses(Class<?> aClass) {
		return getSubClasses(aClass) != null && getSubClasses(aClass).size() > 0;
	}

	/**
	 * returns true if the given model class has is the base class from which
	 * other classes may extend note this is an alias for hasInheritance
	 * 
	 * @return
	 */
	public boolean isBaseClass(Class<?> aClass) {
		return hasInheritance(aClass);
	}

	/**
	 * returns true if the given model extends some base class. to retrieve the
	 * class it extends from use getSuperClass(aClass)
	 * 
	 * @return
	 */
	public boolean isSubClass(Class<?> aClass) {
		return getSuperClass(aClass) != null;
	}

	/**
	 * returns true if the given model is not a base- and not a subclass
	 * 
	 * @return
	 */
	public boolean isNormalClass(Class<?> aClass) {
		return !isBaseClass(aClass) && !isSubClass(aClass);
	}

	private static final String IMPLEMENTS = " implements ";

	public String getExtendsAndImplements(Class<?> aClass, Class<?> aDefaultImplements[]) {
		if (aDefaultImplements != null) {
			String impls[] = new String[aDefaultImplements.length];
			for (int i = 0; i < aDefaultImplements.length; i++) {
				impls[i] = aDefaultImplements[i].getName();
			}
			return getExtendsAndImplements(aClass, impls);
		} else {
			return getExtendsAndImplements(aClass, new String[] {});
		}
	}

	public String getExtendsAndImplements(Class<?> aClass, String aDefaultImplements[]) {
		String result = "";

		Class<?> supercls = aClass.getSuperclass();
		if (supercls != null && !supercls.equals(Object.class)) {
			result = "extends " + supercls.getSimpleName();
			ImportHelper.addImport(supercls);
		}

		Class<?> intfs[] = aClass.getInterfaces();
		if (intfs.length > 0) {
			for (Class<?> intf : intfs) {
				if (!isCodegenInterface(intf)) {
					if (result.indexOf(IMPLEMENTS) < 0) {
						result += IMPLEMENTS;
					}
					ImportHelper.addImport(intf);
					if (!result.endsWith(IMPLEMENTS)) {
						result += ", ";
					}
					result += intf.getSimpleName();
				}
			}
		}

		if (aDefaultImplements != null && aDefaultImplements.length > 0) {
			if (result.indexOf(IMPLEMENTS) < 0) {
				result += IMPLEMENTS;
			}
			for (int i = 0; i < aDefaultImplements.length; i++) {
				String impl = aDefaultImplements[i];
				ImportHelper.addImport(impl);
				result += new NameUtils().getSimplename(impl);
				if (i < aDefaultImplements.length - 1) {
					result += ",";
				}
			}
		}
		return result;
	}

	/**
	 * This returns the Fw with the given fieldname in the given class, or null
	 * if not found
	 * 
	 * @param aClass
	 * @return
	 */
	public Fw getFieldByName(Class<?> aClass, String aFieldname) {
		for (Field fld : getAllTheFields(aClass)) {
			if (fld.getName().equals(aFieldname))
				return makeFw(fld, aClass);
		}
		return null;
	}

	/**
	 * This returns the Field with the given type in the given class, or null if
	 * not found
	 * 
	 * @param aClass
	 * @param aFieldType
	 * @return
	 */
	public Fw getFieldByType(Class<?> aClass, Class<?> aFieldType) {
		for (Field fld : getAllTheFields(aClass)) {
			if (fld.getType().equals(aFieldType))
				return makeFw(fld, aClass);
		}
		return null;
	}

	private boolean isCodegenInterface(Class<?> aIntf) {
		return aIntf.getName().startsWith("mda.");
	}

	/**
	 * Return true if this property is annotated with @JoinTable
	 * 
	 * @param aField
	 * @return
	 */
	public boolean isJoinTable(Fw fw) {
		if (hasAnnotation(fw, JoinTable.class)) {
			return true;
		} else {
			if (hasAnnotation(fw, OneToMany.class)) {
				OneToMany anno = (OneToMany) getAnnotation(fw, OneToMany.class);
				// TODO other scenario
				return (anno.mappedBy() != null && anno.mappedBy().trim().length() > 0);
			} else if (hasAnnotation(fw, ManyToMany.class)) {
				return true;
			}
			return false;
		}
	}

	public String getOneToManyType(Fw fw, Class<?> aParent) {
		return "todo";
	}

	public String[] getEnumItems(Class<?> aClass) {
		if (aClass != null && aClass.isEnum()) {
			try {
				Method m = aClass.getMethod("values", new Class<?>[] {});
				Object values[] = (Object[]) m.invoke(null, new Object[] {});
				if (values != null && values.length > 0) {
					String result[] = new String[values.length];
					for (int i = 0; i < values.length; i++) {
						result[i] = values[i].toString();
					}
					return result;
				}
			} catch (Exception e) {
				logger.error("error in getEnumItems()", e);
			}
		}
		return new String[] { "???" };
	}

	public boolean hasAbstractAnnotation() {
		Class<?> clz = currentClass();

		return (hasAnnotation(clz, mda.annotation.Abstract.class));
	}

	public String getSerialVersionUID() {
		return "" + new Date().getTime() + "L";
	}

	/**
	 * Use this method if you want to force that the given class is generated
	 * with the given template, even though this template does not apply to this
	 * class initially.
	 * 
	 * @param aClass
	 * @param aTemplate
	 * @param Object
	 *            ... optional arguments
	 */
	public void generate(Class<?> aClass, ITemplate aTemplate, Object... args) {
		AdditionalGeneratesHelper.add(aClass, aTemplate, args);
	}

	/**
	 * use this method if you want to include generated code from a jet template
	 * indicated with the snippet class. The second param is the argument that
	 * is passed to this jet template. The major difference with the
	 * <b>generate</b> method is that this is not called via an ITemplate but
	 * with class of the jet file.
	 * 
	 * @param aFqClassname
	 * @param aArgument
	 * @return
	 */
	public String include(Class<?> aSnippetClass, Object... aArguments) {
		try {
			Method method = aSnippetClass.getMethod("generate", new Class[] { Object.class });
			Object generator = aSnippetClass.newInstance();
			Object gensrcObj = method.invoke(generator, new Object[] { aArguments });
			String result = (gensrcObj != null) ? gensrcObj.toString() : "";
			return result;
		} catch (InvocationTargetException ex) {
			logger.error("error while include() code for " + aSnippetClass.getName() + " " + ex.getTargetException());
			return "**error**";
		} catch (Throwable t) {
			logger.error("error while include() code for " + aSnippetClass.getName() + " " + t);
			return "**error**";
		}
	}

	public String include(Class<?> aSnippetClass, JetArgument arg) {
		try {
			Method method = aSnippetClass.getMethod("generate", new Class[] { Object.class });
			Object generator = aSnippetClass.newInstance();
			Object gensrcObj = method.invoke(generator, new Object[] { arg });
			String result = (gensrcObj != null) ? gensrcObj.toString() : "";
			return result;
		} catch (InvocationTargetException ex) {
			logger.error("error while include() code for " + aSnippetClass.getName() + " " + ex.getTargetException());
			return "**error**";
		} catch (Throwable t) {
			logger.error("error while include() code for " + aSnippetClass.getName() + " " + t);
			return "**error**";
		}
	}

	/**
	 * return a valid classname given a Class This method also takes into
	 * account an Array class. Such a class would normally return something like
	 * "[Lcom.xxx.Aclasname;"
	 * 
	 * @param aClass
	 * @return
	 */
	public String getClassname(Class<?> aClass) {
		String result = aClass.getCanonicalName();
		if (result.endsWith("[]")) {
			result = result.substring(0, result.length() - 2);
		}
		return result;
	}

	public String getSimpleClassname(Class<?> aClass) {
		String result = getClassname(aClass);
		if (result.indexOf(".") > 0) {
			result = result.substring(result.lastIndexOf(".") + 1);
		}
		return result;
	}

	/**
	 * shortcut to get the base-package string
	 * 
	 * @see DataStore.getBasePackage()
	 * @return
	 */
	public String getBasePackage() {
		return DataStore.getInstance().getBasePackage();
	}

	/**
	 * shortcut to get the (user) supplied App-package string
	 * 
	 * @see DataStore.getBasePackage()
	 * @return
	 */
	public String getAppPackage() {
		return fnh.getOutputAppPackage();
	}

	/**
	 * replace the base-package with app-package in the given input string
	 * 
	 * @param aPackage
	 * @return
	 */
	public String substitutePackage(String aString) {
		return fnh.formatPackage(aString);
	}

	/**
	 * return the one class that implements the ApplicationBaseType interface
	 * 
	 * @return
	 */
	public IApplicationType getApplicationBaseType() {
		return ModelClassesUtils.findApplicationType();
	}

	/**
	 * returns the Class that is currenly active
	 * 
	 * @return
	 */
	public Class<?> currentClass() {
		return JetArgument.getCurrent().getElementClass();
	}

	/**
	 * returns the Table name that belong to the current active class. If the
	 * class is annotated with @Table and the name is provided, this will be
	 * returned else the classname is returned
	 * 
	 * @return
	 */
	public String getTableName() {
		return getTableName(currentClass());
	}

	/**
	 * returns the Table name that belong to given class.
	 * 
	 * @see getTableName()
	 * @param aClass
	 * @return
	 */
	public String getTableName(Class<?> aClass) {
		String result = aClass.getSimpleName();
		if (hasAnnotation(aClass, Table.class)) {
			Table anno = (Table) getAnnotation(aClass, Table.class);
			if (anno.name() != null && !anno.name().isEmpty())
				result = anno.name();
		}
		return result;
	}

	/**
	 * This methods validates the given class, to check if alle annotations are
	 * set correctly
	 * 
	 * @param aClass
	 * @return
	 */
	public boolean validate(Class<?> aClass, List<String> aMessages) {

		boolean status = AnnotationsHelper.validate(aClass, aMessages);

		for (Fw fw : getAllFields(aClass)) {
			status = status && AnnotationsHelper.validate(fw, aMessages);
		}
		return status;
	}

	/**
	 * return all model entity classes
	 * 
	 * @return
	 */
	public List<Class<?>> getAllModelEntities() {
		return ModelClassesUtils.findModelClassesWithInterface(IEntityType.class);
	}

	/**
	 * return all model service classes
	 * 
	 * @return
	 */
	public List<Class<?>> getAllModelServices() {
		return ModelClassesUtils.findModelClassesWithInterface(IServiceType.class);
	}

	/**
	 * return all model service classes
	 * 
	 * @return
	 */
	public List<Class<?>> getAllModelEnums() {
		return ModelClassesUtils.findModelEnums();
	}

	/**
	 * This a NestedObjects object that contains a list of all nested objects
	 * including the class-paths and corresponding field names.
	 * 
	 * @param aClass
	 * @return NestedObjects
	 */
	public NestedObjects getNestedObjects(Class<?> aClass) {
		return new NestedObjects(aClass);
	}

	/**
	 * This returns true if the first class, is nested (somewhere) in the second
	 * class. for example: In Tsta you have: Set<Tstc> cs; In TstC you have:
	 * Tsta a; Within Tstc you can check field a if it Tstc is nested with Tsta
	 * like this: boolean nested = tu.isNestedIn(cc, fw.getType());
	 * 
	 * @param testclass
	 *            the class to be tested if it is inside
	 * @param fromclass
	 *            the root class that is scanned
	 * @return true if nested
	 */
	public boolean isNestedIn(Class<?> testclass, Class<?> rootclass) {
		boolean result = false;
		NestedObjects nestedobjs = getNestedObjects(rootclass);
		for (NestedObject nested : nestedobjs.getNestedObjects()) {
			if (nested.getNestedType().equals(testclass)) {
				result = true;
				break;
			}
		}
		return result;
	}

	public Map<Class<?>, Class<?>> getConcreteTypes() {
		return CONCRETE_TYPES;
	}

	public Set<String> getCollectionTypes() {
		return COLLECTION_TYPES;
	}

	/**
	 * retrieve the filled-in value from SubsValue from the DataStore
	 * 
	 * @param aName
	 * @return
	 */
	public String getSubsvalueFromDataStore(String aName) {
		SubsValue subsval = DataStore.getInstance().getSubsValue(aName);
		if (subsval != null) {
			return subsval.getValue();
		} else {
			logger.error("No SubsValue found for : " + aName);
			return "?" + aName + "?";
		}
	}

	/**
	 * return the current cartrdiges folder. While running the plugin this is
	 * <eclipse-dir>/flca.easymda.generator_xxx/cartridges While junit testing
	 * this is the value returned by the TestTemplatesData interface method:
	 * getPluginDir(), eg <easymda>/flca.mda.generator/cartridges
	 * 
	 * @return
	 */
	public File getCartridgeDir() {
		return new File(DataStore.getInstance().getValue(CodegenConstants.DATASTORE_CARTRIDGE_DIR));
	}

	/**
	 * return the current cartrdiges folder. While running the plugin this is
	 * root dir of the selected class or package While junit testing this is the
	 * value returned by the TestTemplatesData interface method: getModelDir(),
	 * eg <easymda>/flca.mda.test.model.webapp
	 * 
	 * @return
	 */
	public File getModelDir() {
		return new File(DataStore.getInstance().getValue(CodegenConstants.DATASTORE_MODEL_DIR));
	}

	/**
	 * returns true if the given model class has the @Inheritance annotation
	 * 
	 * @return
	 */
	public boolean hasInheritance(Class<?> aClass) {
		return hasAnnotation(aClass, Inheritance.class);
	}

	/**
	 * Return all the fieldwrappers that correspond with the FwSelectType's This
	 * method starts empty and only the Fw's that correspond are returned
	 * 
	 * @see getFieldsExc
	 * @param aClass
	 * @param aSelectOptions
	 * @return
	 */
	public List<Fw> getFieldsInc(final Class<?> aClass, final FwSelectType... aSelectOptions) {
		return getFields(aClass, GetFieldsModus.INCLUDE, aSelectOptions);
	}

	/**
	 * Return all the fieldwrappers that correspond with the FwSelectType's This
	 * method starts with all the available Fw's and substract the Fw's that
	 * correspond with the selectoptions
	 * 
	 * @see getFieldsInc
	 * @param aClass
	 * @param aSelectOptions
	 * @return
	 */
	public List<Fw> getFieldsExc(final Class<?> aClass, final FwSelectType... aSelectOptions) {
		return getFields(aClass, GetFieldsModus.EXCLUDE, aSelectOptions);
	}

	/**
	 * This method combines getFieldsInc and getFieldsExc The result is
	 * equivalent to the result of: List<Fw> fields = getFieldsInc(aClass,
	 * aIncludeSelectOptions) return filterOut(aClass, fields,
	 * aExcludeSelectOptions);
	 * 
	 * @param aClass
	 * @param aIncludeSelectOptions
	 * @param aExcludeSelectOptions
	 * @return
	 */
	public List<Fw> getFields(final Class<?> aClass, final List<FwSelectType> aIncludeSelectOptions,
			final List<FwSelectType> aExcludeSelectOptions) {

		FwSelectType[] incArr = aIncludeSelectOptions.toArray(new FwSelectType[aIncludeSelectOptions.size()]);
		FwSelectType[] excArr = aExcludeSelectOptions.toArray(new FwSelectType[aExcludeSelectOptions.size()]);

		List<Fw> fields = getFieldsInc(aClass, incArr);
		return filterOut(aClass, fields, excArr);
	}

	/**
	 * 
	 * @param aClass
	 * @param aModus
	 * @param aSelectOptions
	 * @return
	 */
	public List<Fw> getFields(final Class<?> aClass, final GetFieldsModus aModus, final FwSelectType... aSelectOptions) {
		if (GetFieldsModus.INCLUDE.equals(aModus)) {
			return getFieldsInclude(aClass, aSelectOptions);
		} else {
			return getFieldsExclude(aClass, aSelectOptions);
		}
	}

	public List<Fw> getAllFields(final Class<?> aClass) {
		List<Fw> result = new ArrayList<>();
		result.addAll(getSpecialFields(aClass));
		for (Field fld : getAllTheFields(aClass)) {
			result.add(makeFw(fld, aClass));
		}

		removeDoubleIdField(result);
		return sortAllFields(result);
	}

	private void removeDoubleIdField(List<Fw> result) {
		// remove the double id field if ness.
		Fw explicitId = getExplicitIdField(result);
		if (explicitId != null) {
			Fw specialId = getSpecialIdField(result);
			if (specialId != null) {
				result.remove(specialId);
			}
		}
	}

	private List<Fw> sortAllFields(List<Fw> fields) {
		Collections.sort(fields, new FwComparator());
		return fields;
	}

	private Fw getSpecialIdField(List<Fw> fields) {
		Fw result = null;
		for (Fw fw : fields) {
			if (fw.isSpecial() && fw.isId()) {
				result = fw;
				break;
			}
		}
		return result;
	}

	/**
	 * Filter out all the Fw's that correspond with the given select options
	 * 
	 * @return
	 */
	public List<Fw> filterOut(Class<?> aClass, List<Fw> fields, final FwSelectType... aSelectOptions) {
		List<Fw> result = new ArrayList<>();
		for (Fw fw : fields) {
			boolean keep = true;
			for (FwSelectType selctype : aSelectOptions) {
				if (includeField(aClass, fw, selctype)) {
					keep = false;
					break;
				}
			}

			if (keep) {
				result.add(fw);
			}
		}
		return result;
	}

	protected Fw makeFw(final Field fld, Class<?> ownerClass) {
		return new Fw(this, fld, ownerClass);
	}

	protected Fw makeFw(final SpecialField fld, Class<?> ownerClass) {
		return new Fw(this, fld, ownerClass);
	}

	protected List<Fw> getSpecialFields(Class<?> aClass) {
		List<Fw> result = new ArrayList<Fw>();
		result.add(makeIdFieldWrapper(aClass));
		return result;
	}

	private List<Fw> getFieldsInclude(final Class<?> aClass, final FwSelectType... aSelectOptions) {
		List<Fw> result = new ArrayList<>();
		List<Fw> allfields = getAllFields(aClass);

		for (Fw fieldWrapper : allfields) {
			for (FwSelectType selcopt : aSelectOptions) {
				if (includeField(aClass, fieldWrapper, selcopt)) {
					result.add(fieldWrapper);
					break;
				}
			}
		}
		return result;
	}

	private List<Fw> getFieldsExclude(final Class<?> aClass, final FwSelectType... aSelectOptions) {
		List<Fw> result = getAllFields(aClass);
		List<Fw> delFields = new ArrayList<>();
		if (hasSelectOptSuper(aSelectOptions) && getSuperClass(aClass) != null) {
			result.addAll(getAllFields(getSuperClass(aClass)));
		}
		for (Fw fieldWrapper : result) {
			for (FwSelectType selcopt : aSelectOptions) {
				if (includeField(aClass, fieldWrapper, selcopt)) {
					delFields.add(fieldWrapper);
				}
			}
		}

		for (Fw delfld : delFields) {
			result.remove(delfld);
		}
		return result;
	}

	private boolean hasSelectOptSuper(final FwSelectType... aSelectOptions) {
		for (FwSelectType opt : aSelectOptions) {
			if (FwSelectType.SUPER.equals(opt)) {
				return true;
			}
		}
		return false;
	}

	private boolean includeField(final Class<?> aClass, final Fw fw, final FwSelectType aFieldType) {
		if (aFieldType.equals(FwSelectType.ALL)) {
			return true;
		} else if (aFieldType.equals(FwSelectType.ALL)) {
			return false;
		} else if (aFieldType.equals(FwSelectType.ONETOONE)) {
			return fw.isOneToOneField();
		} else if (aFieldType.equals(FwSelectType.ONETOMANY)) {
			return fw.isOneToManyField();
		} else if (aFieldType.equals(FwSelectType.MANYTOONE)) {
			return fw.isManyToOneField();
		} else if (aFieldType.equals(FwSelectType.MANYTOMANY)) {
			return fw.isManyToManyField();
		} else if (aFieldType.equals(FwSelectType.RELATIONS)) {
			return fw.isOneToOneField() || fw.isOneToManyField() || fw.isManyToOneField() || fw.isManyToManyField();
		} else if (aFieldType.equals(FwSelectType.SIMPLE)) {
			return fw.isSimple();
		} else if (aFieldType.equals(FwSelectType.ENUM)) {
			return fw.isEnum();
		} else if (aFieldType.equals(FwSelectType.VAL)) {
			return fw.isVal();
		} else if (aFieldType.equals(FwSelectType.VAR)) {
			return !fw.isSpecial() && fw.isVar();
		} else if (aFieldType.equals(FwSelectType.SPECIAL)) {
			return fw.isSpecial();
		} else if (aFieldType.equals(FwSelectType.OFD)) {
			return fw.isSpecial() && FwSelectType.OFD.equals(fw.getSpecialfield().getSpecialFieldType());
		} else if (aFieldType.equals(FwSelectType.DISCRIMINATOR)) {
			return fw.isSpecial() && FwSelectType.DISCRIMINATOR.equals(fw.getSpecialfield().getSpecialFieldType());
		} else if (aFieldType.equals(FwSelectType.ID)) {
			if (hasExplicitId(aClass)) {
				return !fw.isSpecial() && fw.isId();
			} else {
				return fw.isSpecial() && FwSelectType.ID.equals(fw.getSpecialfield().getSpecialFieldType());
			}
		} else {
			return false;
		}
	}

	/**
	 * Returns the primitive or class name for the given Type. Class names will
	 * be added as imports to the GenModel's ImportManager, and the imported
	 * form will be returned.
	 */
	public String getTypeName(Class<?> aClass) {
		if (!aClass.isPrimitive()) {
			ImportHelper.addImport(aClass);
		}

		if (isCollection(aClass)) {
			return aClass.getSimpleName() + "<?" + ">";
		} else {
			return aClass.getSimpleName();
		}
	}

	/**
	 * Returns the primitive or class name for the given Type. Class names will
	 * be added as imports to the GenModel's ImportManager, and the imported
	 * form will be returned.
	 */
	public String getTypeName(Class<?> aClass, Class<?> aGenericType) {
		if (!aClass.isPrimitive()) {
			ImportHelper.addImport(aClass);
		}
		if (aGenericType != null && !aGenericType.isPrimitive()) {
			ImportHelper.addImport(aGenericType);
		}

		if (isCollection(aClass)) {
			if (aGenericType != null) {
				return aClass.getSimpleName() + "<" + aGenericType.getSimpleName() + ">";
			} else {
				return aClass.getSimpleName() + "<?>";
			}
		} else {
			String result = aClass.getSimpleName();
			return result;
		}
	}

	public boolean isSimpleField(Class<?> aClass) {
		if (aClass == null) {
			return true;
		} else if (isCollection(aClass)) {
			return false;
		} else {
			return aClass.isPrimitive() || aClass.getName().startsWith("java") || aClass.isEnum();
		}
	}

	/**
	 * Return true if this field us marked with @Val
	 * 
	 * @param fld
	 * @return boolean
	 */
	public boolean isValType(Fw flw) {
		return false;
	}

	/**
	 * Return true if this field us marked with @Var
	 * 
	 * @param fld
	 * @return boolean
	 */
	public boolean isVarType(Fw flw) {
		return false;
	}

	public boolean isBooleanType(Class<?> type) {
		return "boolean".equals(type.getSimpleName().toLowerCase());
	}

	public boolean isNumericType(Class<?> aClass) {
		if (isSimpleField(aClass)) {
			String typename = aClass.getSimpleName();
			return NUMERIC_FIELDS.indexOf(typename.toLowerCase()) >= 0;
		} else {
			return false;
		}
	}

	public boolean isStringType(Class<?> type) {
		return "String".equals(type.getSimpleName());
	}

	/**
	 * return true if the given class is int Integer long Long short or Short
	 * 
	 * @param type
	 * @return
	 */
	public boolean isIntType(Class<?> type) {
		if (isSimpleField(type)) {
			String typename = type.getName().toLowerCase();
			if (typename.startsWith("java.lang")) {
				typename = typename.substring(10);
			}
			return (INT_FIELDS.indexOf(typename) >= 0);
		} else {
			return false;
		}
	}

	/**
	 * return true if the given class is double Double float Floao or BigDecimal
	 * 
	 * @param type
	 * @return
	 */
	public boolean isDecimalType(Class<?> type) {
		if (isSimpleField(type)) {
			String typename = type.getName().toLowerCase();
			if (typename.startsWith("java.lang") || typename.startsWith("java.math")) {
				typename = typename.substring(10);
			}
			return (DEC_FIELDS.indexOf(typename) >= 0);
		} else {
			return false;
		}
	}

	/**
	 * return true if the given class is double Double float Floao or BigDecimal
	 * 
	 * @param type
	 * @return
	 */
	public boolean isDateType(Class<?> type) {
		if (isSimpleField(type)) {
			String typename = type.getName();
			return (DATE_FIELDS.indexOf(typename) >= 0);
		} else {
			return false;
		}
	}

	public boolean isTimestampType(Class<?> type) {
		if (type == null) {
			return false;
		} else {
			return "Timestamp".equals(type.getSimpleName());
		}
	}

	public boolean isTimeType(Class<?> type) {
		if (type == null) {
			return false;
		} else {
			return "Time".equals(type.getSimpleName());
		}
	}

	public boolean isIntegerType(Class<?> type) {
		if (type == null) {
			return false;
		} else {
			return "Integer".equals(type.getSimpleName());
		}
	}

	public boolean is_intType(Class<?> type) {
		if (type == null) {
			return false;
		} else {
			return "int".equals(type.getSimpleName());
		}
	}

	public boolean isDoubleType(Class<?> type) {
		if (type == null) {
			return false;
		} else {
			return "Double".equals(type.getSimpleName());
		}
	}

	public boolean is_doubleType(Class<?> type) {
		if (type == null) {
			return false;
		} else {
			return "double".equals(type.getSimpleName());
		}
	}

	public boolean isLongType(Class<?> type) {
		if (type == null) {
			return false;
		} else {
			return "Long".equals(type.getSimpleName());
		}
	}

	public boolean is_longType(Class<?> type) {
		if (type == null) {
			return false;
		} else {
			return "long".equals(type.getSimpleName());
		}
	}

	// public boolean isBlob(Type type) {
	// return "Byte[]".equals(type.getName());
	// }

	// public boolean isCurrency(Property property) {
	// return "Currency".equals(property.getType().getName());
	// }
	//
	// public boolean isPercentage(Property property) {
	// return "Percentage".equals(property.getType().getName());
	// }
	//
	// public boolean isText(Property property) {
	// return "Text".equals(property.getType().getName());
	// }
	//
	// public boolean isImage(Property property) {
	// return "Image".equals(property.getType().getName());
	// }
	//
	// public boolean isLink(Property property) {
	// return "Link".equals(property.getType().getName());
	// }
	//
	// public boolean isDecimal(Property property) {
	// return "Decimal".equals(property.getType().getName());
	// }

	public String getDefaultValue(Class<?> aClass) {
		String result = DataStore.getInstance().getSectionValue("java.default.value", aClass.getSimpleName());
		if (result != null && result.trim().length() == 0) {
			result = "null";
		}
		return result;
	}

	public boolean hasDefaultValue(Class<?> aClass) {
		String dv = getDefaultValue(aClass);
		return dv != null && dv.length() > 0;
	}

	public String format(List<Fw> fields, String formatString, String separator) {
		List<String> formattedFields = new ArrayList<String>();
		for (Fw fw : fields) {
			formattedFields.add(fw.format(formatString));
		}
		return nu.join(formattedFields, separator);
	}

	// ------- static ----------------------------------------
	static {
		CONCRETE_TYPES = new HashMap<Class<?>, Class<?>>();
		CONCRETE_TYPES.put(Set.class, HashSet.class);
		CONCRETE_TYPES.put(List.class, ArrayList.class);
		CONCRETE_TYPES.put(Map.class, HashMap.class);
		CONCRETE_TYPES.put(Collection.class, HashSet.class);

		COLLECTION_TYPES = new HashSet<String>();
		COLLECTION_TYPES.add(Set.class.getName());
		COLLECTION_TYPES.add(List.class.getName());
		COLLECTION_TYPES.add(HashSet.class.getName());
		COLLECTION_TYPES.add(ArrayList.class.getName());
		COLLECTION_TYPES.add(Collection.class.getName());
	}

	public class FwComparator implements Comparator<Fw> {
		@Override
		public int compare(Fw o1, Fw o2) {
			if (o1.isId()) {
				return -1;
			} else if (o2.isId()) {
				return 1;
			} else if (o1.isSpecial()) {
				return -1;
			} else if (o2.isSpecial()) {
				return 1;
			} else {
				return 0;
			}
		}
	}
}
