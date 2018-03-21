package com.cpms.exceptions;

import com.cpms.web.UserSessionData;

/**
 * Thrown when user has attempted to access wrong page.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class WrongPageException extends WebException {

	public WrongPageException(String explanation, Exception cause, String path) {
		super(UserSessionData.localizeText(
				"Страница, к который вы пытаетесь получить доступ, не существует.",
				"The page that you have tried to access does not exist."), 
				explanation, cause, path);
	}
	
	public WrongPageException(String explanation, String path) {
		super(UserSessionData.localizeText(
				"Страница, к который вы пытаетесь получить доступ, не существует.",
				"The page that you have tried to access does not exist."), 
				explanation, path);
	}

}
