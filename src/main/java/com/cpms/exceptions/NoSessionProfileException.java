package com.cpms.exceptions;

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

	public NoSessionProfileException(Exception cause, String path) {
		super(UserSessionData.localizeText("Сперва нужно запомнить профиль", 
				"You need to remember the profile first."), UserSessionData.localizeText(
				"Доступ без учета профиля.", "Access without profile being remembered."), cause, path);
	}
	
	public NoSessionProfileException(String path) {
		super(UserSessionData.localizeText("Сперва нужно запомнить профиль", 
				"You need to remember the profile first."), UserSessionData.localizeText(
				"Доступ без учета профиля.", "Access without profile being remembered."), path);
	}

}
