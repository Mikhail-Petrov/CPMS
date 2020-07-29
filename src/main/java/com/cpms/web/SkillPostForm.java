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
	
	private String name, name_ru, name_en;
	
	private String about, about_ru, about_en;
	
	private int maxLevel;
	
	private String parent;
	
	private String type, pages, dattr;

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

	public String getPages() {
		return pages;
	}

	public void setPages(String pages) {
		this.pages = pages;
	}

	public String getAbout_ru() {
		return about_ru;
	}

	public void setAbout_ru(String about_ru) {
		this.about_ru = about_ru;
	}

	public String getAbout_en() {
		return about_en;
	}

	public void setAbout_en(String about_en) {
		this.about_en = about_en;
	}

	public String getName_en() {
		return name_en;
	}

	public void setName_en(String name_en) {
		this.name_en = name_en;
	}

	public String getName_ru() {
		return name_ru;
	}

	public void setName_ru(String name_ru) {
		this.name_ru = name_ru;
	}

	public String getDattr() {
		return dattr;
	}

	public void setDattr(String dattr) {
		this.dattr = dattr;
	}
	
}
