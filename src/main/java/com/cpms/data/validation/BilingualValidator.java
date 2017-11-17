package com.cpms.data.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang.WordUtils;

import com.cpms.data.AbstractDomainObject;

/**
 * Validator that handles validation of fields that allow being
 * input on two languages. Uses metaprogramming, thus sensitive.
 * 
 * @see BilingualValidation
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class BilingualValidator implements 
				ConstraintValidator<BilingualValidation, AbstractDomainObject> {
	
	private static final String NULLSTR = null;
	
	private BilingualValidation annotation;

	@Override
	public void initialize(BilingualValidation constraintAnnotation) {
		annotation = constraintAnnotation;
	}

	@Override
	public boolean isValid(AbstractDomainObject value, 
			ConstraintValidatorContext context) {
		String field1, field2;
		try {
			field1 = getField(value, annotation.fieldOne());
			field2 = getField(value, annotation.fieldTwo());
		} catch (Exception e) {
			writeError(context, "Validation has failed due to internal reasons");
			return false;
		}
		boolean firstCorrect = checkField(field1, context),
				secondCorrect = checkField(field2, context);
		try {
			setFieldIfIncorrect(value, annotation.fieldOne(), firstCorrect);
			setFieldIfIncorrect(value, annotation.fieldTwo(), secondCorrect);
		} catch (Exception e) {
			writeError(context, "Validation has failed due to internal reasons");
			return false;
		}
		return annotation.nullable() ? (firstCorrect || secondCorrect) : 
			(firstCorrect && secondCorrect);
	}
	
	private void setFieldIfIncorrect(AbstractDomainObject object,
			String fieldName, boolean correct) throws Exception {
		if (!correct) {
			object
					.getClass()
					.getDeclaredMethod("set" + WordUtils.capitalize(fieldName), 
							String.class)
					.invoke(object, NULLSTR);
		}
	}
	
	private boolean checkField(String field, 
			ConstraintValidatorContext context) {
		if (field == null) {
			if (!annotation.nullable()) {
				writeError(context, "Empty fields are not allowed");
				return false;
			} else {
				return true;
			}
		}
		if (field.length() < annotation.minlength() || 
				field.length() > annotation.maxlength()) {
			writeError(context, "Wrong length: should be more than " +
					annotation.minlength() + " but less than " +
					annotation.maxlength() + " symbols in length");
			return false;
		} else {
			return true;
		}
	}
	
	private String getField(AbstractDomainObject object, String fieldName) 
														throws Exception {
		return ((String)object
				.getClass()
				.getDeclaredMethod("get" + WordUtils.capitalize(fieldName))
				.invoke(object));
	}
	
	private void writeError(ConstraintValidatorContext context, String error) {
		context.buildConstraintViolationWithTemplate(error)
        		.addPropertyNode(annotation.fieldOne())
        		.addConstraintViolation();
	}

}
