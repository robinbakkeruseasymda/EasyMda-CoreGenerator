package flca.mda.api.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

import mda.annotation.validation.DateRange;
import mda.annotation.validation.MinMax;
import mda.annotation.validation.NotEmpty;
import mda.annotation.validation.NotNull;
import mda.annotation.validation.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidatorUtils {
	private static Logger logger = LoggerFactory.getLogger(ValidatorUtils.class);

	protected static NameUtils nu = new NameUtils();
	protected static SnippetUtils su = new SnippetUtils();

	public boolean mustValidate(Field fld) {
		Annotation annots[] = fld.getAnnotations();

		for (Annotation anno : annots) {
			Class<?> c = anno.annotationType();
			if (c.getName().startsWith("mda.annotation.validation")) {
				return true;
			}
		}

		return false;
	}

	public boolean mustValidate(Fw fw) {
		if (!fw.isSpecial()) {
			return mustValidate(fw.getField());
		}

		return false;
	}

	public boolean isRequired(Field fld) {
		Annotation annots[] = fld.getAnnotations();

		for (Annotation anno : annots) {
			String sname = getSimpleAnnotName(anno);
			if (("NotNull NotEmpty").indexOf(sname) >= 0) {
				return true;
			}
		}

		return false;
	}

	public boolean isRequired(Fw fw) {
		if (!fw.isSpecial()) {
			return isRequired(fw.getField());
		} else {
			return false;
		}
	}

	public String getValidatorLines(Fw aField) {
		StringBuffer sb = new StringBuffer();

		Annotation annots[] = aField.getField().getAnnotations();

		for (Annotation anno : annots) {
			try {
				Class<?> c = anno.annotationType();
				Method m = this.getClass().getMethod("handle" + c.getSimpleName(),
						new Class<?>[] { c, Properties.class, StringBuffer.class });
				Properties props = makePropertiesForVld(aField);
				m.invoke(null, new Object[] { anno, props, sb });
			} catch (Exception e) {
				logger.warn("no handler found for " + anno);
			}
		}

		return sb.toString();
	}

	public static void handleNotNull(NotNull anno, Properties props, StringBuffer sb) {
		sb.append(su.getSnippet("java.validators", getSimpleAnnotName(anno), props));
	}

	public static void handleNotEmpty(NotEmpty anno, Properties props, StringBuffer sb) {
		sb.append(su.getSnippet("java.validators", getSimpleAnnotName(anno), props));
	}

	public static void handleMinMax(MinMax anno, Properties props, StringBuffer sb) {
		props.put("min", "" + anno.min());
		props.put("max", "" + anno.max());
		sb.append(su.getSnippet("java.validators", getSimpleAnnotName(anno), props));
	}

	public static void handleSize(Size anno, Properties props, StringBuffer sb) {
		props.put("min", "" + anno.min());
		props.put("max", "" + anno.max());
		sb.append(su.getSnippet("java.validators", getSimpleAnnotName(anno), props));
	}

	public static void handleDateRange(DateRange anno, Properties props, StringBuffer sb) {
		props.put("min", "TODO");
		props.put("max", "TODO");
		sb.append(su.getSnippet("java.validators", getSimpleAnnotName(anno), props));
	}

	private static Properties makePropertiesForVld(Fw aField) {
		Properties result = new Properties();

		result.put("uncapName", nu.uncapName(aField));
		result.put("capname", nu.capName(aField));

		return result;
	}

	private static String getSimpleAnnotName(Annotation anno) {
		Class<?> c = anno.annotationType();
		return c.getSimpleName();
	}
}
