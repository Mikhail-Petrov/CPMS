package com.cpms.exceptions;

/**
 * Exception thrown in java code in case system's user tries to access something
 * (an entity, for instance) it doesn't have access to.
 * 
 * @see WebException
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class AccessDeniedException extends WebException {
	
	public AccessDeniedException(String explanation, String path) {
		super ("You have tried to access something you don't have access to",
				explanation, path);
	}

}
