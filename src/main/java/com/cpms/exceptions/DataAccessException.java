package com.cpms.exceptions;

/**
 * Common DAO exception.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class DataAccessException extends CPMSException {
	
	public DataAccessException(String explanation, Exception cause) {
		super(explanation, cause);
	}
	
	public DataAccessException(String explanation) {
		super(explanation);
	}
	
}
