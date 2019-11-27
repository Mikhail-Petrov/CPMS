package com.cpms.exceptions;

import org.springframework.context.MessageSource;

import com.cpms.web.UserSessionData;

/**
 * Exception thrown in java code in case system's user tries to access something
 * (an entity, for instance) it doesn't have access to.
 * 
 * @see WebException
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class AccessDeniedException extends WebException {
    
	public AccessDeniedException(String explanation, String path, MessageSource messageSource) {
		super (UserSessionData.localizeText("exception.AccessDenied", messageSource), explanation, path);
	}

}
