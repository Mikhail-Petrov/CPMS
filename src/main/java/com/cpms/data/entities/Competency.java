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
import javax.validation.constraints.NotNull;

import com.cpms.data.AbstractDomainObject;

/**
 * Entity class for competencies.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "COMPETENCY", uniqueConstraints =
		@UniqueConstraint(columnNames = {"OWNER", "SKILL"}, name = "OwnerSkillUnique"))
public class Competency extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@Column(name = "LEVEL", nullable = false)
	@Max(100)
	@Min(1)
	private int level;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "owner", nullable = false)
	private Profile owner;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "SKILL", nullable = false)
	@NotNull
	private Skill skill;
	
	private String skillIDs;
	
	public Competency(Skill skill, int level) {
		this.level = level;
		this.skill = skill;
	}
	
	public Competency() {}
	

	public Profile getOwner() {
		return owner;
	}

	public void setOwner(Profile owner) {
		this.owner = owner;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setSkill(Skill skill) {
		this.skill = skill;
	}

	@Override
	public long getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public Skill getSkill() {
		return skill;
	}
	
	/**
	 * Alternative equality function for competency types. Uses skills and owners
	 * to compare.
	 * 
	 * @param competency competency to compare with
	 * @return true if those competencies duplicate each other (their skills and
	 * owners are the same), otherwise false.
	 */
	public boolean duplicates(Competency competency) {
		if (competency == null) {
			return false;
		}
		if (competency.getOwner() == null || competency.getSkill() == null 
				|| this.getClass() == null || this.getSkill() == null){
			return false;
		} else {
			return competency.getOwner().equals(this.getOwner())
				&& competency.getSkill().equals(this.getSkill());
		}
	}

	@Override
	public Class<?> getEntityClass() {
		return Competency.class;
	}

	@Override
	public String getPresentationName() {
		return getSkill().getPresentationName() + " - " + getLevel();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Competency localize(Locale locale) {
		Competency returnValue = new Competency();
		returnValue.setId(getId());
		returnValue.setLevel(getLevel());
		returnValue.setOwner(null);
		returnValue.setSkill(getSkill());
		return returnValue;
	}

	public String getSkillIDs() {
		return skillIDs;
	}

	public void setSkillIDs(String skillIDs) {
		this.skillIDs = skillIDs;
	}
	
}
