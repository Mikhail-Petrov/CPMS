package com.cpms.exceptions;

/**
 * Thrown when user has attempted to access wrong page.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class WrongPageException extends WebException {

	public WrongPageException(String explanation, Exception cause, String path) {
		super("The page that you have tried to access does not exist.", 
				explanation, cause, path);
	}
	
	public WrongPageException(String explanation, String path) {
		super("The page that you have tried to access does not exist.", 
				explanation, path);
	}

}
