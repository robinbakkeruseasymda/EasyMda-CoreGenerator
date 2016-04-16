package mda.annotation.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({METHOD, FIELD})
@Retention(RUNTIME)

public @interface DateRange 
{
	public int minYear();
	public int minMonth() default 1; 
	public int minDay() default 1;

	public int maxYear();
	public int maxMonth() default 12; 
	public int maxDay() default 31;
}
