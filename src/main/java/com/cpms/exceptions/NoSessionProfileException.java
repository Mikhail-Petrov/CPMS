package com.cpms.exceptions;

/**
 * Thrown when there needs to be a profile remembered within user context,
 * but there is none.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class NoSessionProfileException extends WebException {

	public NoSessionProfileException(Exception cause, String path) {
		super("You need to remember the profile first.",
				"Access without profile being remembered.", cause, path);
	}
	
	public NoSessionProfileException(String path) {
		super("You need to remember the profile first.",
				"Access without profile being remembered.", path);
	}

}
