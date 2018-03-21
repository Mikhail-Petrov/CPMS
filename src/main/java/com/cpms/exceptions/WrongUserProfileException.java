package com.cpms.exceptions;

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

	public WrongUserProfileException(String explanation, String path) {
		super(UserSessionData.localizeText(
				"Вы попытались создать пользователя-резидента с неверным профилем.",
				"You have tried to create a resident user with wrong profile."),
				explanation, path);
	}
	
	public WrongUserProfileException(String explanation, Exception cause, String path) {
		super(UserSessionData.localizeText(
				"Вы попытались создать пользователя-резидента с неверным профилем.",
				"You have tried to create a resident user with wrong profile."),
				explanation, cause, path);
	}

}
