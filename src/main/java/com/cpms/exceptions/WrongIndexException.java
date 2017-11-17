package com.cpms.exceptions;

/**
 * Thrown when pagination utility was supplied with wrong indexes.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class WrongIndexException extends WebException {

	public WrongIndexException(int total, int index, String page,
			Exception cause) {
		super("Attempt to access item in the list with wrong index. ",
				"Total items: " + total + ", index: " + index + ".", 
				cause, page);
	}
	
	public WrongIndexException(int total, int index, String page) {
		super("Attempt to access item in the list with wrong index. ",
				"Total items: " + total + ", index: " + index + ".", page);
	}

}
