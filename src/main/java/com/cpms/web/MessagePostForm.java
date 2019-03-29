package com.cpms.web;

/**
 * Alternative skill form for posting skills, used in skill tree editor
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class MessagePostForm {

	private long id;
	
	private String title;
	
	private String text;
	
	private String parent;
	
	public MessagePostForm() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
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
	
}
