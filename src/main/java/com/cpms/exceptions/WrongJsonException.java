package com.cpms.exceptions;

import org.springframework.context.MessageSource;

import com.cpms.web.UserSessionData;

/**
 * Thrown when JSON parsing utility found unknown JSON format.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class WrongJsonException extends CPMSException {

	public WrongJsonException(String json, Exception cause, MessageSource messageSource) {
		super(UserSessionData.localizeText("exception.WrongJson", messageSource) + "\n" + json, cause);
	}

	public WrongJsonException(String json, MessageSource messageSource) {
		super(UserSessionData.localizeText("exception.WrongJson", messageSource) + "\n" + json);
	}
	
}
