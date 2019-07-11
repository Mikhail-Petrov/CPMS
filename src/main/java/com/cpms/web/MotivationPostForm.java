package com.cpms.web;

/**
 * Alternative skill form for posting skills, used in skill tree editor
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class MotivationPostForm {

	private long id;
	
	private String name;
	
	private String description;
	
	private int cost;
	
	private int budget;
	
	private int benefit;
	
	private String parent;
	
	private boolean isGroup;
	
	private String code;
	
	private String local;
	
	public MotivationPostForm() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public int getCost() {
		return cost;
	}

	public void setCost(int maxLevel) {
		this.cost = maxLevel;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public boolean isGroup() {
		return isGroup;
	}

	public void setIsGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}

	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}

	public int getBudget() {
		return budget;
	}

	public void setBudget(int budget) {
		this.budget = budget;
	}

	public int getBenefit() {
		return benefit;
	}

	public void setBenefit(int benefit) {
		this.benefit = benefit;
	}
	
}
