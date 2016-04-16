package mda.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({METHOD, FIELD})
@Retention(RUNTIME)

public @interface AnnotationLiteral 
{
	/**
	 * The string is used as is.
	 * Note the annotion should contain a fully qualified classname, but the generator will output the Simplename and import this annotation
	 * @return
	 */
	public String annotation() ;
}
