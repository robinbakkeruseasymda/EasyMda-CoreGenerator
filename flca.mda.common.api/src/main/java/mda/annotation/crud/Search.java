package mda.annotation.crud;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(TYPE)
@Retention(RUNTIME)

/**
 * this annotation indicates that the target file can be copied as is.
 */
public @interface Search 
{
	/**
	 * The Service interface where the crud operation should be generated
	 * @return
	 */
	public Class<?> service();
	
	public Class<?> searchArguments();
	
	public boolean usePaging() default true;
	
}
