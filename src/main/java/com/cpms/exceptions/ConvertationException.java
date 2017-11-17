package com.cpms.exceptions;

/**
 * Thrown when conversion or cast failures.
 * 
 * @see CPMSException
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class ConvertationException extends CPMSException {

	public ConvertationException(String explanation, Exception cause) {
		super("Error when converting from one type to another: " + explanation, cause);
	}
	
	public ConvertationException(String explanation) {
		super("Error when converting from one type to another: " + explanation);
	}
	
}
