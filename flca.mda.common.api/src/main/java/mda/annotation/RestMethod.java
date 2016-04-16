package mda.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({METHOD, FIELD})
@Retention(RUNTIME)

public @interface RestMethod 
{
	/**
	 * indicated if a GET (default) or POST should be generated
	 */
	public boolean POST() default false;
	public boolean GET() default true;
	
}
