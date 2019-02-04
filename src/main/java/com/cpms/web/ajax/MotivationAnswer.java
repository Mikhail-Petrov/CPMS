package com.cpms.web.ajax;

import java.util.ArrayList;
import java.util.List;

import com.cpms.data.entities.Motivation;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.SkillLevel;
import com.cpms.web.UserSessionData;

/**
 * AJAX answer that returns information about a certain skill.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class MotivationAnswer implements IAjaxAnswer {

	private long id;
	private String parentId;
	private int cost;
	private String name, description, code;
	private boolean successful = false, isGroup;
	
	public MotivationAnswer(Motivation source, boolean successful) {
		id = source.getId();
		cost = source.getCost();
		name = source.getPresentationName();
		description = source.getDescription();
		code = source.getCode();
		isGroup = source.getIsGroup();
		if (source.getParent() == null)
			parentId = null;
		else
			parentId = String.format("%d", source.getParent().getId());
		this.successful = successful;
	}
	
	public MotivationAnswer() {
		id = 0;
		cost = 0;
		name = "Motivation not found";
		description = "Motivation not found";
		code = "";
		successful = false;
		isGroup = false;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public boolean getIsGroup() {
		return isGroup;
	}

	public void setGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}
	
}
