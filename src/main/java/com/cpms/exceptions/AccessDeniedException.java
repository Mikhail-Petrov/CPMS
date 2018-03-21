package com.cpms.exceptions;

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
	
	public AccessDeniedException(String explanation, String path) {
		super (UserSessionData.localizeText(
				"Вы пытались получить доступ к чему-то, к чему у вас нет доступа",
				"You have tried to access something you don't have access to"),
				explanation, path);
	}

}
