package com.cpms.exceptions;

import com.cpms.web.UserSessionData;

/**
 * Thrown when session data seems to be lost.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class SessionExpiredException extends WebException {

	public SessionExpiredException(Exception cause, String path) {
		super(UserSessionData.localizeText(
				"Кажется, вы слишком долго отсутствовали, и ваша сессия истекла!", 
				"It seems that you were away for too long and your session has expired!"),
				"Session expiration.", cause, path);
	}
	
	public SessionExpiredException(String path) {
		super(UserSessionData.localizeText(
				"Кажется, вы слишком долго отсутствовали, и ваша сессия истекла!", 
				"It seems that you were away for too long and your session has expired!"),
				"Session expiration.", path);
	}

}
