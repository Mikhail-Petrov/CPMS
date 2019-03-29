package com.cpms.web.ajax;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.cpms.data.entities.Message;
import com.cpms.data.entities.MessageCenter;

/**
 * AJAX answer that returns information about a certain skill.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class MessagesAnswer implements IAjaxAnswer {

	private long id;
	private String parentId;
	private String title, text;
	private List<Long> recepients;
	private boolean successful = false;
	
	public MessagesAnswer(Message source, boolean successful) {
		id = source.getId();
		title = source.getPresentationName();
		text = source.getText();
		if (source.getParent() == null)
			parentId = null;
		else
			parentId = String.format("%d", source.getParent().getId());
		this.successful = successful;
		Set<MessageCenter> centers = source.getRecipients();
		getRecepients();
		for (MessageCenter center : centers)
			recepients.add(center.getUser().getId());
	}
	
	public MessagesAnswer() {
		id = 0;
		title = "Motivation not found";
		text = "Motivation not found";
		successful = false;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	/**
	 * @return the parentId
	 */
	public String getParentId() {
		return parentId;
	}

	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<Long> getRecepients() {
		if (recepients == null) recepients = new ArrayList<>();
		return recepients;
	}

	public void setRecepients(List<Long> recepients) {
		this.recepients = recepients;
	}
	
}
