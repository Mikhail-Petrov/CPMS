package com.cpms.exceptions;

import org.springframework.context.MessageSource;

import com.cpms.web.UserSessionData;

/**
 * Thrown when session data seems to be lost.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class SessionExpiredException extends WebException {

	public SessionExpiredException(Exception cause, String path, MessageSource messageSource) {
		super(UserSessionData.localizeText("exception.SessionExpired", messageSource), UserSessionData.localizeText("exception.SessionExpired.explanation", messageSource), cause, path);
	}
	
	public SessionExpiredException(String path, MessageSource messageSource) {
		super(UserSessionData.localizeText("exception.SessionExpired", messageSource), UserSessionData.localizeText("exception.SessionExpired.explanation", messageSource), path);
	}

}
