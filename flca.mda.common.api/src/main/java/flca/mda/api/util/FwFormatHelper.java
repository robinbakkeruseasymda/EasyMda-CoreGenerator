package flca.mda.api.util;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FwFormatHelper {

	public static String fieldFormatFunction(final Fw fw, final String src) {
		final Pattern pattern = Pattern.compile("%f<(.*)>");
		final Matcher matcher = pattern.matcher(src);
		matcher.find();
		final int n = matcher.start();
		final int m = matcher.end();
		final String funcname = src.substring(n + 3, m - 1);
		return src.substring(0, n) + getFieldFormatFunctionResult(fw, funcname) + src.substring(m);
	}

	private static String getFieldFormatFunctionResult(final Fw fw, String funcName) {
		try {
			int at = funcName.indexOf("(");
			if (at > 0) {
				funcName = funcName.substring(0, at);
			}
			final Method m = fw.getClass().getMethod(funcName, new Class[] {});
			return m.invoke(fw).toString();
		} catch (Exception ex) {
			System.out.println("error in getFieldFormatFunctionResult " + ex);
			return "?funcName?";
		}
	}

}
