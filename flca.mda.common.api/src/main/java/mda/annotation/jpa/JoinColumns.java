package mda.annotation.jpa;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines mapping for the composite foreign keys. This annotation 
 * groups {@link JoinColumn} annotations for the same relationship.
 *
 * <p> When the <code>JoinColumns</code> annotation is used, 
 * both the {@link JoinColumn#name name} and the {@link 
 * JoinColumn#referencedColumnName referencedColumnName} elements 
 * must be specified in each such {@link JoinColumn} annotation.
 *
 * <pre>
 *
 *    Example:
 *    &#064;ManyToOne
 *    &#064;JoinColumns({
 *        &#064;JoinColumn(name="ADDR_ID", referencedColumnName="ID"),
 *        &#064;JoinColumn(name="ADDR_ZIP", referencedColumnName="ZIP")
 *    })
 *    public Address getAddress() { return address; }
 * </pre>
 *
 * @since Java Persistence 1.0
 */
@Target({METHOD, FIELD}) 
@Retention(RUNTIME)

public @interface JoinColumns {
    JoinColumn[] value();
}
