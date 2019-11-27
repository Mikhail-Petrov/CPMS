package com.cpms.exceptions;

import org.springframework.context.MessageSource;

import com.cpms.web.UserSessionData;

/**
 * Thrown when conversion or cast failures.
 * 
 * @see CPMSException
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class ConvertationException extends CPMSException {

	public ConvertationException(String explanation, Exception cause, MessageSource messageSource) {
		super(UserSessionData.localizeText("exception.Convertation", messageSource) + explanation, cause);
	}
	
	public ConvertationException(String explanation, MessageSource messageSource) { 
		super(UserSessionData.localizeText("exception.Convertation", messageSource) + explanation);
	}
	
}
