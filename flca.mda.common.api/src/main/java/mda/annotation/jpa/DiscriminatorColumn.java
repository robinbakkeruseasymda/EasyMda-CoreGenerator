package mda.annotation.jpa;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({TYPE})
@Retention(RUNTIME)

/**
 * Optional annotation that is only applicable for InheritanceType.SINGLE_TABLE
 *
 * <pre>
 *
 *   Example:
 *
 *   &#064;Entity
 *   &#064;Inheritance(strategy=SINGLE_TABLE)
 *   &#064;DiscriminatorColumn(name="kind")
 *   public class Customer { ... }
 *
 *   &#064;Entity
 *   &#064;DiscriminatorValue(name="kind")
 *   public class ValuedCustomer extends Customer { ... }
 * </pre>
 *
 * @since Java Persistence 1.0
 */
public @interface DiscriminatorColumn {
	String name() default "discriminator";
	String columnName() default "DISCRIMINATOR"; 
	DiscriminatorType discriminatorType() default DiscriminatorType.STRING;
}
