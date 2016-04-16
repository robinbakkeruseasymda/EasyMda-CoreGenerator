package flca.mda.codegen.helpers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mda.annotation.jpa.JoinColumn;
import mda.annotation.jpa.ManyToOne;
import mda.annotation.jpa.OneToMany;
import mda.type.IDtoType;
import mda.type.IEntityType;
import mda.type.IServiceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import flca.mda.api.util.Fw;
import flca.mda.api.util.TypeUtils;

public class AnnotationsHelper {
	private static Logger logger = LoggerFactory.getLogger(AnnotationsHelper.class);
	
	private static TypeUtils tu = new TypeUtils();

	private final static String Q = "\"";

	public static boolean isMdaAnnotation(Object aObject) {
		if (aObject.getClass().getName().startsWith("mda.annotation")) {
			return true;
		} else {
			return (aObject.toString().indexOf("mda.annotation.") >= 0);
		}
	}

	public static Annotation getAnnotation(Method aMethod, Class<?> annotionToLookFor) {
		return getAnnotation(aMethod, annotionToLookFor.getName());
	}

	public static Annotation getAnnotation(Method aMethod, String aFqnameToLookFor) {
		Annotation annots[] = aMethod.getAnnotations();
		for (Annotation a : annots) {
			Class<?> c = a.annotationType();
			if (c.getName().equals(aFqnameToLookFor)) {
				return a;
			}
		}
		return null;
	}

	public static boolean hasAnnotation(Method aMethod, Class<?> annotionToLookFor) {
		return hasAnnotation(aMethod, annotionToLookFor.getName());
	}

	public static boolean hasAnnotation(Method aMethod, String aFqnameToLookFor) {
		return getAnnotation(aMethod, aFqnameToLookFor) != null;
	}

	public static Annotation getAnnotation(Fw aField, Class<?> annotionToLookFor) {
		return getAnnotation(aField, annotionToLookFor.getName());
	}

	public static Annotation getAnnotation(Fw aField, String aFqNameToLookFor) {
		if (!aField.isSpecial()) {
			Annotation annots[] = aField.getField().getAnnotations();
			for (Annotation a : annots) {
				Class<?> c = a.annotationType();
				if (c.getName().equals(aFqNameToLookFor)) {
					return a;
				}
			}
		}
		return null;
	}

	public static boolean hasAnnotation(Fw aField, Class<?> annoType) {
		return hasAnnotation(aField, annoType.getName());
	}

	public static boolean hasAnnotation(Fw aField, String fqAnnoType) {
		return getAnnotation(aField, fqAnnoType) != null;
	}

	public static Annotation getAnnotation(Class<?> aTargetClass, Class<?> annotionToLookFor) {
		return getAnnotation(aTargetClass, annotionToLookFor.getName());
	}

	public static Annotation getAnnotation(Class<?> aTargetClass, String aFqnameToLookFor) {
		Annotation annots[] = aTargetClass.getAnnotations();
		for (Annotation a : annots) {
			Class<?> c = a.annotationType();
			if (c.getName().equals(aFqnameToLookFor)) {
				return a;
			}
		}
		return null;
	}

	public static Annotation getAnnotation(Class<?> aTargetClass, Annotation aFindAnnot) {
		Annotation annots[] = aTargetClass.getAnnotations();
		for (Annotation a : annots) {
			if (a.annotationType().equals(aFindAnnot.annotationType())) {
				return a;
			}
		}
		return null;
	}

	public static boolean hasAnnotation(Class<?> aTargetClass, Annotation annot) {
		return hasAnnotation(aTargetClass, annot);
	}

	public static boolean hasAnnotation(Class<?> aTargetClass, Class<?> annoType) {
		return hasAnnotation(aTargetClass, annoType.getName());
	}

	public static boolean hasAnnotation(Class<?> aTargetClass, String fqAnnoType) {
		return getAnnotation(aTargetClass, fqAnnoType) != null;
	}

	private static final String SKIP_METHODS = "equals getClass toString hashCode isProxyClass notify notifyAll wait newProxyInstance getProxyClass getInvocationHandler";

	public static boolean isValidMethod(Method m) {
		return (SKIP_METHODS.indexOf(m.getName())) < 0;
	}

	/**
	 * recursive method to generate the string value belonging to this annotation
	 * method
	 * 
	 * @param anno
	 * @param method
	 * @param sb
	 */
	public static String getAnnotationValue(Annotation anno, Method method) {
		String result = "";

		if (!isValidMethod(method)) {
			return result;
		}

		try {
			Object value = method.invoke(anno, new Object[] {});
			if (value != null) {
				if (value instanceof String) {
					result = getStringAnnotationValue(method, (String) value);
				} else if (value.getClass().isArray()) {
					result = getArrayAnnotationValue(method, value);
				}
			}
			return result;
		} catch (Exception e) {
			logger.error("error in getAnnotationValue " + method.getName() + "  " + e);
			return "";
		}
	}

	private static String getStringAnnotationValue(Method method, String value) {
		String result = "";
		if (value != null && value.trim().length() > 0) {
			result = method.getName() + "=" + Q + value + Q;
		}
		return result;
	}

	private static String getArrayAnnotationValue(Method method, Object value) {
		String result = "";
		if (Array.getLength(value) > 0) {
			result = method.getName() + "={";
			for (int i = 0; i < Array.getLength(value); i++) {
				Object item = Array.get(value, 0);
				result = getArrayAnnotationValue(item.toString());
			}
			result += "}";
		}

		return result;
	}

	private static final String ANNOT_NAME = "(^.*)(\\(.*)";
	private static final String ARRAY_NAME = "(.*)(\\()(.*)(=)(.*)";
	private static final String ARRAY_VALUES = "(.*)(=\\{)(.*)(\\}\\).*)";
	private static final Pattern pat1 = Pattern.compile(ANNOT_NAME);
	private static final Pattern pat2 = Pattern.compile(ARRAY_NAME);
	private static final Pattern pat3 = Pattern.compile(ARRAY_VALUES);

	/**
	 * This replace a string like
	 * [@mda.annotation.jpa.UniqueConstraint(columnNames=[c1, c2])] to:
	 * {@UniqueConstraint(columnNames={"c1",
	 *  "c2"})} we do this by replace [ -> {
	 * and ] -> } look for the text before the first: ( this gives
	 * @mda.annotation.jpa.UniqueConstraint replace this to @UniqueConstraint and
	 * import the corr annotation look for text between: ( and ={ this gives
	 * columnNames look for the text between { and }) this gives: c1, c2 replace
	 * this to: "c1", "c2" concat found and replaces values
	 */
	private static String getArrayAnnotationValue(String value) {
		String r = value.replaceAll("\\[", "{");
		r = r.replaceAll("\\]", "}");

		String annoclass = "";
		String arrayvalues = "";
		String arrayname = "";

		Matcher m1 = pat1.matcher(r);
		if (m1.matches()) {
			annoclass = m1.group(1);
		}
		Matcher m2 = pat2.matcher(r);
		if (m2.matches()) {
			arrayname = m2.group(3);
		}
		Matcher m3 = pat3.matcher(r);
		if (m3.matches()) {
			arrayvalues = m3.group(3);
		}

		String vals[] = arrayvalues.split(", ");
//		System.out.println(vals);
		String rvals = "={";
		for (String v : vals) {
			rvals += "\"" + v + "\"" + ",";
		}
		rvals += "}";
		String result = "{" + annoclass + "(" + arrayname + rvals + ")}";
		return result;
	}
	
	/**
	 * For the given field return the corresponding name of the foreign key in the mapped class
	 * Note the input field should be a @OneToMany field with a mappedBY tag !
	 * In the mapped class, there should be a corresponding @ManyToOne 
	 * Example:
	 * Troop
	 * @OneToMany(mappedBy="troop")  // or (mappedBy="troopId")
	 * Set<Soldier> soldier
	 * 
	 * Soldier
	 * @ManyToOne 
	 * @JoinColumn(name="SOLDIER_TROOP_FK")
	 * Troop troop  // or Long troopId
	 * 
	 * @param fw
	 * @return
	 */
	public static Fw getMappedByFkField(Fw fw) {
		OneToMany anno = (OneToMany) getAnnotation(fw, OneToMany.class);
		Class<?> gentype = fw.genericType();

		if (anno == null || gentype == null) {
//			System.out.println("** getMappedByForeignKey annotation or generic type is null");
			return null;
		} else {
			Fw mappedByfld = gentype != null ? tu.getFieldByName(gentype, anno.mappedBy()) : null ;
			return mappedByfld;
		}
	}

	public static boolean hasMappedByFkField(Fw fw) {
		return getMappedByFkField(fw) != null;
	}
	
	/**
	 * This method validates the given class
	 * In checks if it implements a know easymda interface. 
	 * @param aClass 
	 * @return
	 */
	public static boolean validate(Class<?> aClass, List<String> aMessages) {
		Class<?> mdainterfaces[] = new Class<?>[] {IEntityType.class, IDtoType.class, IServiceType.class};
		boolean found = false;
		for (Class<?> intf : mdainterfaces) {
			if (tu.hasInterface(aClass, intf)) {
				found = true;
				break;
			}
		}
		if (!found && !aClass.isEnum()) {
			aMessages.add("\n[INFO]: Class " + aClass.getName() + " does not implements any of the easymda interfaces and is not an enum");
			return false;
		} else {
			return true;
		}
	}

	/**
	 * This method validates the given field.
	 * In particular it validates that @OneToMany fields have a corresponding @ManyToOne 
	 * @param aField
	 * @return
	 */
	public static boolean validate(Fw aField, List<String> aMessages) {
		if (hasAnnotation(aField, OneToMany.class)) {
			return validateOneToManyField(aField, aMessages);
		} else {
			return true;
		}
	}
	
	private static boolean validateOneToManyField(Fw aField, List<String> aMessages) {
		OneToMany anno = (OneToMany) getAnnotation(aField, OneToMany.class);
		String error = "\n[ERROR]: Field " + aField.name();
		String info = "\n[INFO]: Field " + aField.name();
		
		//OneToMany should be a collection
		Class<?> gentype = aField.genericType();
		if (gentype == null) {
			aMessages.add(error + " should have a generic type");
			return false;
		}
		
		// check the mappedBy tag
		if (anno.mappedBy().isEmpty()) {
			aMessages.add(error + " does not have a mappedBy tag in the @OneToMany annotation");	
			return false;
		} 
		
		// check the mappedBy class
		Fw fw = tu.getFieldByName(gentype, anno.mappedBy());
		if (fw == null) {
			aMessages.add(error + " can not find the mappedBy field " + anno.mappedBy());
			return false;
		}
		
		// check corresponding @ManyToOne annot
		if (!hasAnnotation(fw, ManyToOne.class)) {
			aMessages.add(error + " the mappedBy field does not have @ManyToOne annotation");
			return false;
		}
		
		// check the JoinColumn annot
		if (!hasAnnotation(fw, JoinColumn.class)) {
			aMessages.add(info + " the mappedBy field does not have @JoinColumns annotation, " + fw.name() + " will be used");
			return false;
		}
		
		return true;
	}
}
