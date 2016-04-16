package mda.annotation.jpa;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Optional annotation that is only applicable for InheritanceType.SINGLE_TABLE
 *
 * <pre>
 *
 *   Example:
 *
 *   &#064;Entity
 *   &#064;Inheritance(strategy=SINGLE_TABLE)
 *   &#064;DiscriminatorColumn(columnDefinition=)
 *   public class Customer { ... }
 *
 *   &#064;Entity
 *   *   &#064;DiscriminatorColumn("VALUED")
 *   public class ValuedCustomer extends Customer { ... }
 * </pre>
 *
 * @since Java Persistence 1.0
 */

@Target({TYPE})
@Retention(RUNTIME)

public @interface DiscriminatorValue {

	String value();
}
