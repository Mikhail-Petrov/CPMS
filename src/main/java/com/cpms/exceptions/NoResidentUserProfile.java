package com.cpms.exceptions;

import org.springframework.context.MessageSource;

import com.cpms.web.UserSessionData;

/**
 * Thrown in case resident's user competency profile cannot be found.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class NoResidentUserProfile extends WebException {

	public NoResidentUserProfile(String explanation, Exception cause, String path, MessageSource messageSource) {
		super(UserSessionData.localizeText("exception.NoResidentUserProfile", messageSource), explanation, cause, path);
	}
	
	public NoResidentUserProfile(String explanation, String path, MessageSource messageSource) {
		super(UserSessionData.localizeText("exception.NoResidentUserProfile", messageSource), explanation, path); 
	}

}
