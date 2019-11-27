package com.cpms.exceptions;

import org.springframework.context.MessageSource;

import com.cpms.web.UserSessionData;

/**
 * Thrown when there needs to be a profile remembered within user context,
 * but there is none.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class NoSessionProfileException extends WebException {

	public NoSessionProfileException(Exception cause, String path, MessageSource messageSource) {
		super(UserSessionData.localizeText("exception.NoSessionProfile", messageSource), UserSessionData.localizeText(
				"exception.NoSessionProfile.explanation", messageSource), cause, path);
	}
	
	public NoSessionProfileException(String path, MessageSource messageSource) {
		super(UserSessionData.localizeText("exception.NoSessionProfile", messageSource), UserSessionData.localizeText(
				"exception.NoSessionProfile.explanation", messageSource), path);
	}

}
