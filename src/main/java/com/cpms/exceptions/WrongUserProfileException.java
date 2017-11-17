package com.cpms.exceptions;

/**
 * Thrown when a resident user creation was attempted but his competency profile
 * was not specified.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class WrongUserProfileException extends WebException {

	public WrongUserProfileException(String explanation, String path) {
		super("You have tried to create a resident user with wrong profile.",
				explanation, path);
	}
	
	public WrongUserProfileException(String explanation, Exception cause, String path) {
		super("You have tried to create a resident user with wrong profile.",
				explanation, cause, path);
	}

}
