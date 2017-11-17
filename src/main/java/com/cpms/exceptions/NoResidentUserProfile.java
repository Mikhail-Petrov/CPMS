package com.cpms.exceptions;

/**
 * Thrown in case resident's user competency profile cannot be found.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class NoResidentUserProfile extends WebException {

	public NoResidentUserProfile(String explanation, Exception cause, String path) {
		super("Your user profile does not have a competency profile associated."
				+ " Please contact administrator if you think this is a mistake.",
				explanation, cause, path);
	}
	
	public NoResidentUserProfile(String explanation, String path) {
		super("Your user profile does not have a competency profile associated."
				+ " Please contact administrator if you think this is a mistake.",
				explanation, path);
	}

}
