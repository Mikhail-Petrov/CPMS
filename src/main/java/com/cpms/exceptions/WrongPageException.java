package com.cpms.exceptions;

import org.springframework.context.MessageSource;

import com.cpms.web.UserSessionData;

/**
 * Thrown when user has attempted to access wrong page.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class WrongPageException extends WebException {

	public WrongPageException(String explanation, Exception cause, String path, MessageSource messageSource) {
		super(UserSessionData.localizeText("exception.WrongPage", messageSource), explanation, cause, path);
	}
	
	public WrongPageException(String explanation, String path, MessageSource messageSource) {
		super(UserSessionData.localizeText("exception.WrongPage", messageSource), explanation, path);
	}

}
