package mda.annotation.gui;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(TYPE) 
@Retention(RUNTIME)

/**
 * Use this annotatiion to override the default behavior with respect to generate corresponding gui forms.
 * For Entities, the default behaviour is true
 * For Dto's the default behaviour is false
 * With this annotation the default behavior can be altered.
 */
public @interface Form 
{
	/**
	* indicate if a corresponding MainForm and DetailForm should be generated.
	 */
	public boolean generate();
}
