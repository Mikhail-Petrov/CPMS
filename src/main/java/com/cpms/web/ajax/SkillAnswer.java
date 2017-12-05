package com.cpms.web.ajax;

import java.util.ArrayList;
import java.util.List;

import com.cpms.data.entities.Skill;
import com.cpms.data.entities.SkillLevel;

/**
 * AJAX answer that returns information about a certain skill.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class SkillAnswer implements IAjaxAnswer {

	private long id;
	private int maxLevel;
	private String name, about, name_ru, name_en, about_ru, about_en;
	private boolean successful = false;
	private List<SkillLevelAnswer> levels;
	private boolean draft = false;
	
	public SkillAnswer(Skill source, boolean successful) {
		id = source.getId();
		maxLevel = source.getMaxLevel();
		name = source.getPresentationName();
		name_ru = source.getName_RU();
		name_en = source.getName();
		about = source.getPresentationAbout();
		about_ru = source.getAbout_RU();
		about_en = source.getAbout();
		this.successful = successful;
		levels = new ArrayList<SkillLevelAnswer>();
		draft = source.isDraft();
		for (SkillLevel level : source.getFullSkillLevels()) {
			SkillLevelAnswer newLevel = new SkillLevelAnswer(level, true);
			levels.add(newLevel);
		}
	}
	
	public SkillAnswer() {
		id = 0;
		maxLevel = 0;
		name = "Skill not found";
		setName_ru("Умение не найдено");
		setName_en("Skill not found");
		about = "Skill not found";
		setAbout_ru("Умение не найдено");
		setAbout_en("Skill not found");
		successful = false;
		levels = new ArrayList<SkillLevelAnswer>();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
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

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public List<SkillLevelAnswer> getLevels() {
		return levels;
	}

	public void setLevels(List<SkillLevelAnswer> levels) {
		this.levels = levels;
	}

	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	/**
	 * @return the name_ru
	 */
	public String getName_ru() {
		return name_ru;
	}

	/**
	 * @param name_ru the name_ru to set
	 */
	public void setName_ru(String name_ru) {
		this.name_ru = name_ru;
	}

	/**
	 * @return the name_en
	 */
	public String getName_en() {
		return name_en;
	}

	/**
	 * @param name_en the name_en to set
	 */
	public void setName_en(String name_en) {
		this.name_en = name_en;
	}

	/**
	 * @return the about_ru
	 */
	public String getAbout_ru() {
		return about_ru;
	}

	/**
	 * @param about_ru the about_ru to set
	 */
	public void setAbout_ru(String about_ru) {
		this.about_ru = about_ru;
	}

	/**
	 * @return the about_en
	 */
	public String getAbout_en() {
		return about_en;
	}

	/**
	 * @param about_en the about_en to set
	 */
	public void setAbout_en(String about_en) {
		this.about_en = about_en;
	}
	
}
