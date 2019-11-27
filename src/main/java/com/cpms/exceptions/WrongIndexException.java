package com.cpms.exceptions;

import org.springframework.context.MessageSource;

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
			Exception cause, MessageSource messageSource) {
		super(UserSessionData.localizeText("exception.WrongIndex", messageSource),
				String.format(UserSessionData.localizeText("count.total.index", messageSource), total, index), 
				cause, page);
	}
	
	public WrongIndexException(int total, int index, String page, MessageSource messageSource) {
		super(UserSessionData.localizeText("exception.WrongIndex", messageSource),
				String.format(UserSessionData.localizeText("count.total.index", messageSource), total, index), page);
	}

}
