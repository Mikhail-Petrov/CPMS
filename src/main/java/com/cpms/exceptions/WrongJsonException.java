package com.cpms.exceptions;

import com.cpms.web.UserSessionData;

/**
 * Thrown when JSON parsing utility found unknown JSON format.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class WrongJsonException extends CPMSException {

	public WrongJsonException(String json, Exception cause) {
		super(UserSessionData.localizeText("Получен неверный JSON:\n", "Wrong JSON recieved:\n") + json, cause);
	}

	public WrongJsonException(String json) {
		super(UserSessionData.localizeText("Получен неверный JSON:\n", "Wrong JSON recieved:\n") + json);
	}
	
}
