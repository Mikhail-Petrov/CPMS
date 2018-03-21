package com.cpms.exceptions;

import com.cpms.web.UserSessionData;

/**
 * Thrown when pagination utility was supplied with wrong indexes.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class WrongIndexException extends WebException {

	public WrongIndexException(int total, int index, String page,
			Exception cause) {
		super(UserSessionData.localizeText("Попытка доступа к объекту списка с неверным индексом",
				"Attempt to access item in the list with wrong index. "),
				UserSessionData.localizeText("Всего объектов: " + total + ", индекс: " + index + ".",
						"Total items: " + total + ", index: " + index + "."), 
				cause, page);
	}
	
	public WrongIndexException(int total, int index, String page) {
		super(UserSessionData.localizeText("Попытка доступа к объекту списка с неверным индексом",
				"Attempt to access item in the list with wrong index. "),
				UserSessionData.localizeText("Всего объектов: " + total + ", индекс: " + index + ".",
						"Total items: " + total + ", index: " + index + "."),  page);
	}

}
