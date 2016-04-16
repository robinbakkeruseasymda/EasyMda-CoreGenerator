package mda.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(TYPE) 
@Retention(RUNTIME)

public @interface Gui 
{
	/**
	 * This annotation can be used on entity and dto classes to tell the generator what gui classes should be 
	 * generated (or not). If this annotation is not added, then NO gui classes are created at all!
	 * If this annot is availble, then however all field are true by default.
	 * field to indicate if you want to generate (or not) this corresponding gui class.
	 * 
	 */
	public boolean generateGrid() default true;
	public boolean generateForm() default true;
	
}
