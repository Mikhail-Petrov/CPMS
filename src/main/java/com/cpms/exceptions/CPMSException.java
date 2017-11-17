package com.cpms.exceptions;

/**
 * Common template class for exceptions within CPMS.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public abstract class CPMSException extends RuntimeException {
	
	protected CPMSException(String explanation, Exception cause) {
		super(explanation, cause);
	}
	
	protected CPMSException(String explanation) {
		super(explanation);
	}
	
}
