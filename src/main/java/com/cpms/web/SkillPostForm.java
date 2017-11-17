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
	
	private String name_RU;
	
	private String about_RU;
	
	private int maxLevel;
	
	private long parent;

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

	public long getParent() {
		return parent;
	}

	public void setParent(long parent) {
		this.parent = parent;
	}

	public List<SkillLevel> getLevels() {
		return levels;
	}

	public void setLevels(List<SkillLevel> levels) {
		this.levels = levels;
	}

	public String getName_RU() {
		return name_RU;
	}

	public void setName_RU(String name_RU) {
		this.name_RU = name_RU;
	}

	public String getAbout_RU() {
		return about_RU;
	}

	public void setAbout_RU(String about_RU) {
		this.about_RU = about_RU;
	}
	
}
