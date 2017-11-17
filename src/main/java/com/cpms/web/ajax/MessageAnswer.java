package com.cpms.web.ajax;

import org.codehaus.jackson.map.annotate.JsonView;

//import com.fasterxml.jackson.annotation.JsonView;

/**
 * AJAX answer that returns a single message.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class MessageAnswer implements IAjaxAnswer {
	
	@JsonView
	private String message;
	
	public MessageAnswer() {}
	
	public MessageAnswer(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
