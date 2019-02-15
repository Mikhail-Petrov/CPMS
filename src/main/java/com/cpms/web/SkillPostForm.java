package com.cpms.web;

import java.util.ArrayList;
import java.util.List;

import com.cpms.data.entities.SkillLevel;

/**
 * Alternative skill form for posting skills, used in skill tree editor
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class SkillPostForm {

	private long id;
	
	private String name;
	
	private String about;
	
	private int maxLevel;
	
	private String parent;
	
	private String type;

	private List<SkillLevel> levels;
	
	public SkillPostForm() {
		levels = new ArrayList<SkillLevel>();
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

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public List<SkillLevel> getLevels() {
		return levels;
	}

	public void setLevels(List<SkillLevel> levels) {
		this.levels = levels;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
