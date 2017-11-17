package com.cpms.exceptions;

/**
 * Thrown when session data seems to be lost.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class SessionExpiredException extends WebException {

	public SessionExpiredException(Exception cause, String path) {
		super("It seems that you were away for too long and your session has expired!",
				"Session expiration.", cause, path);
	}
	
	public SessionExpiredException(String path) {
		super("It seems that you were away for too long and your session has expired!",
				"Session expiration.", path);
	}

}
