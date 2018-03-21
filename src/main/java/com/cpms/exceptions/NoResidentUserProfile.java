package com.cpms.exceptions;

import com.cpms.web.UserSessionData;

/**
 * Thrown in case resident's user competency profile cannot be found.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class NoResidentUserProfile extends WebException {

	public NoResidentUserProfile(String explanation, Exception cause, String path) {
		super(UserSessionData.localizeText(
				"У вашего профиля пользователя нет связанного профиля компетенции."
				+ " Если вы считаете, что это ошибка, обратитесь к администратору.",
				"Your user profile does not have a competency profile associated."
				+ " Please contact administrator if you think this is a mistake."),
				explanation, cause, path);
	}
	
	public NoResidentUserProfile(String explanation, String path) {
		super(UserSessionData.localizeText(
				"У вашего профиля пользователя нет связанного профиля компетенции."
				+ " Если вы считаете, что это ошибка, обратитесь к администратору.",
				"Your user profile does not have a competency profile associated."
				+ " Please contact administrator if you think this is a mistake."),
				explanation, path);
	}

}
