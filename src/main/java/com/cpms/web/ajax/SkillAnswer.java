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
	private String name, about;
	private boolean successful = false;
	private List<SkillLevel> levels;
	private boolean draft = false;
	
	public SkillAnswer(Skill source, boolean successful) {
		id = source.getId();
		maxLevel = source.getMaxLevel();
		name = source.getPresentationName();
		about = source.getAbout();
		this.successful = successful;
		levels = new ArrayList<SkillLevel>();
		draft = source.isDraft();
		for (SkillLevel level : source.getFullSkillLevels()) {
			SkillLevel newLevel = new SkillLevel();
			newLevel.setId(level.getId());
			newLevel.setAbout(level.getAbout());
			newLevel.setAbout_RU(level.getAbout_RU());
			newLevel.setLevel(level.getLevel());
			levels.add(newLevel);
		}
	}
	
	public SkillAnswer() {
		id = 0;
		maxLevel = 0;
		name = "Skill not found";
		about = "Skill not found";
		successful = false;
		levels = new ArrayList<SkillLevel>();
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

	public List<SkillLevel> getLevels() {
		return levels;
	}

	public void setLevels(List<SkillLevel> levels) {
		this.levels = levels;
	}

	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
	}
	
}
