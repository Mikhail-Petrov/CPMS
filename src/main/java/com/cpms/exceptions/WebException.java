package com.cpms.exceptions;

/**
 * Subclass for {@link CPMSException}. Allows for exception's message to be
 * demonstrated on web application's error page. Uses "message" constructor
 * parameter to set this message. Also can record web path for logging purposes.
 * 
 * @see CPMSException
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public abstract class WebException extends CPMSException {

	private final String path, message;
	
	protected WebException(String message, String explanation, Exception cause,
			String path) {
		super(explanation, cause);
		this.path = path;
		this.message = message;
	}
	
	protected WebException(String message, String explanation, String path) {
		super(explanation);
		this.path = path;
		this.message = message;
	}

	public String getPath() {
		return path;
	}

	public String getPublicMessage() {
		return message;
	}
	
}
