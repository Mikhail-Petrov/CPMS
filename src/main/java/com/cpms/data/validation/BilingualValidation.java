package com.cpms.data.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Constraint for validating fields that can be input on two languages.
 * 
 * @see BilingualValidator
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Documented
@Constraint(validatedBy = BilingualValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RepeatableBilingualValidation.class)
public @interface BilingualValidation {

	/**
	 * Name of the first field.
	 */
	String fieldOne();
	
	/**
	 * Name of the second field.
	 */
	String fieldTwo();
	
	/**
	 * If it is allowed for one of the fields to be empty or null.
	 */
	boolean nullable() default false;
	
	/**
	 * Minimal text field length.
	 */
	int minlength();
	
	/**
	 * Maximal text field length.
	 */
	int maxlength();
	
	String message() default "{com.cpms.data.validation.BilingualValidation.message}";
	
	Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
	
}
