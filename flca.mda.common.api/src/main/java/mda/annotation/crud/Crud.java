package mda.annotation.crud;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(TYPE)
@Retention(RUNTIME)

/**
 * Class annotation to be used in model classes that implement IEntity to indicate that you want to generated crud operations in what service class.
 * Optionally you can also define exactly what crud operation should be generated.
 */
public @interface Crud 
{
	/**
	 * The Service interface where the crud operation should be generated
	 * @return
	 */
	public Class<?> service();
	
	public CrudOperation[] operations() default {CrudOperation.ALL};
	
	public boolean usePaging() default true;
	
}
