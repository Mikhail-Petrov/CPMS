package com.cpms.data.entities;

import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.cpms.data.AbstractDomainObject;
import com.cpms.data.validation.BilingualValidation;
import com.cpms.exceptions.DataAccessException;

/**
 * Entity class for skill level.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Entity
@SuppressWarnings("serial")
@Table(name = "SKILLLEVEL", uniqueConstraints =
	@UniqueConstraint(columnNames = {"LEVEL", "SKILL"}, name = "SkillLevelUnique"))
@BilingualValidation(fieldOne="about", fieldTwo="about_RU", 
	nullable = true, minlength = 0, maxlength = 1000)
public class SkillLevel extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "SKILL", nullable = false)
	private Skill skill;
	
	@Column(name = "LEVEL", nullable = false)
	@Min(1)
	@Max(100)
	private int level;
	
	@Column(name = "ABOUT", nullable = true, length = 1000)
	private String about;
	
	@Column(name = "ABOUT_RU", nullable = true, length = 1000)
	private String about_RU;
	
	public SkillLevel() {}
	
	public SkillLevel(String about) {
		this.about = about;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public void setSkill(Skill skill) {
		this.skill = skill;
	}

	public void setLevel(int level) {
		if (skill != null && skill.getMaxLevel() < level) {
			throw new DataAccessException("Attempt to insert level larger than"
					+ " skill's max level.", null);
		}
		this.level = level;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	@Override
	public long getId() {
		return id;
	}

	public Skill getSkill() {
		return skill;
	}

	public int getLevel() {
		return level;
	}

	public String getAbout() {
		return about;
	}

	public String getAbout_RU() {
		return about_RU;
	}

	public void setAbout_RU(String about_RU) {
		this.about_RU = about_RU;
	}

	@Override
	public Class<?> getEntityClass() {
		return SkillLevel.class;
	}

	@Override
	public String getPresentationName() {
		return about;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SkillLevel localize(Locale locale) {
		SkillLevel returnValue = new SkillLevel();
		returnValue.setId(getId());
		returnValue.setLevel(getLevel());
		returnValue.setSkill(null);
		returnValue.setAbout(
				localizeBilingualField(getAbout(), getAbout_RU(), locale));
		return returnValue;
	}
	
}
