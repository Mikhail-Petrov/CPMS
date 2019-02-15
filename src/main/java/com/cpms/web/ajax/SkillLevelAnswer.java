package com.cpms.web.ajax;

import com.cpms.data.entities.SkillLevel;

/**
 * AJAX answer that returns information about a certain skill level.
 * 
 * @author Mikhail Petrov
 * @since 1.0
 */
public class SkillLevelAnswer implements IAjaxAnswer {

	private long id;
	private String about, about_ru, about_en;
	private int level;
	private boolean successful = false;
	
	public SkillLevelAnswer(SkillLevel source, boolean successful) {
		id = source.getId();
		about = source.getPresentationAbout();
		about_en = source.getAbout();
		level = source.getLevel();
		this.successful = successful;
	}
	
	public SkillLevelAnswer() {
		id = 0;
		about = "Skill Level not found";
		setAbout_en("Skill Level not found");
		level = 0;
		successful = false;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}
	
}
