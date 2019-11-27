package com.cpms.exceptions;

import org.springframework.context.MessageSource;

import com.cpms.web.UserSessionData;

/**
 * Thrown when a resident user creation was attempted but his competency profile
 * was not specified.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class WrongUserProfileException extends WebException {

	public WrongUserProfileException(String explanation, String path, MessageSource messageSource) {
		super(UserSessionData.localizeText("exception.WrongUserProfile", messageSource), explanation, path);
	}
	
	public WrongUserProfileException(String explanation, Exception cause, String path, MessageSource messageSource) {
		super(UserSessionData.localizeText("exception.WrongUserProfile", messageSource), explanation, cause, path);
	}

}
