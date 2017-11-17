package com.cpms.data.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows {@link BilingualValidation} to be applied multiple times to
 * the same entity.
 * 
 * @see BilingualValidation
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface RepeatableBilingualValidation {
	BilingualValidation[] value();
}
