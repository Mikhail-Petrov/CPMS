package com.cpms.exceptions;

/**
 * Thrown in case of unavoidable numeric overflow.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class NumericalOverflowException extends CPMSException {

	public NumericalOverflowException(String explanation) {
		super(explanation);
	}
	
	public NumericalOverflowException(String explanation, Exception cause) {
		super(explanation, cause);
	}

}
