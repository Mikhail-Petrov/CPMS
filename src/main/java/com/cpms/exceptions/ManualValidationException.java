package com.cpms.exceptions;

/**
 * Throw when manual validation of entities fields finds an error.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class ManualValidationException extends WebException {

	public ManualValidationException(String message, String explanation, Exception cause, String path) {
		super(message, explanation, cause, path);
	}
	
	public ManualValidationException(String message, String explanation, String path) {
		super(message, explanation, path);
	}

}
