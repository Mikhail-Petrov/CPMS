package com.cpms.exceptions;

/**
 * Thrown when JSON parsing utility found unknown JSON format.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class WrongJsonException extends CPMSException {

	public WrongJsonException(String json, Exception cause) {
		super("Wrong JSON recieved:\n" + json, cause);
	}

	public WrongJsonException(String json) {
		super("Wrong JSON recieved:\n" + json);
	}
	
}
