package mda.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({METHOD, FIELD})
@Retention(RUNTIME)

public @interface JavaDoc 
{
	/**
	 * The corresponding Javadoc.
	 * You dont need to supply the start and finish tags
	 * @return
	 */
	public String doc() default "TODO";
}
