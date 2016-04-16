package mda.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(TYPE) 
@Retention(RUNTIME)

/**
 * Annotation to indicate that you dont want to generate code for this class.
 * 
 * @author robin
 *
 */
public @interface DontGenerateCode 
{

}
