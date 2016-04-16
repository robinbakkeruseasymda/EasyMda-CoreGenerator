package mda.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import mda.annotation.crud.CrudOperation;

@Target(TYPE) 
@Retention(RUNTIME)

/**
 * This annotation is used by the generator for several aspects related with the json/rest external service.
 * It can be used for both an IService and IEntity, but some fields are not relevant. See the field below  
 */
public @interface RestService 
{
	/**
	 * This field is valid for both IService and IEntity. It is used for the url prefix:
	 * example localhost:8000/srv/ServiceName
	 * @return
	 */
	public String contextName() default "srv";
	
	/**
	 * By default for an IService a json/rest external service will be generated, hence this annotation is not neeeded.
	 * For an IEntity however, by default the Dao external json/rest will NOT be generated, to create one you have 
	 * annotate an IEntity with this one!
	 * @return
	 */
	public boolean generateService() default true;
	
	/**
	 * with this field you can define what crud operation should be generated or not.
	 * Only applicable for IEntity types
	 * @return
	 */
	public CrudOperation[] operations() default {CrudOperation.ALL};
}
