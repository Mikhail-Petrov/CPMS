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
 * Entity class for task requirement.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "REQUIREMENT", uniqueConstraints =
		@UniqueConstraint(columnNames = {"TASK", "SKILL"}, name = "TaskSkillUnique"))
public class TaskRequirement extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@Column(name = "LEVEL", nullable = false)
	@Min(1)
	@Max(100)
	private int level;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "task", nullable = false)
	private Task task;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "SKILL", nullable = false)
	@NotNull
	private Skill skill;
	
	public TaskRequirement(Skill skill, int level) {
		this.level = level;
		this.skill = skill;
	}
	
	public TaskRequirement() {}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Skill getSkill() {
		return skill;
	}

	public void setSkill(Skill skill) {
		this.skill = skill;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public Class<?> getEntityClass() {
		return TaskRequirement.class;
	}

	@Override
	public long getId() {
		return id;
	}
	
	/**
	 * Alternative equality function for competency types. Uses skills and owners
	 * to compare.
	 * 
	 * @param requirement to compare with
	 * @return true if those requirements duplicate each other (their skills and
	 * tasks are the same), otherwise false.
	 */
	public boolean duplicates(TaskRequirement requirement) {
		if (requirement == null) {
			return false;
		}
		if (requirement.getTask() == null || requirement.getSkill() == null 
				|| this.getTask() == null || this.getSkill() == null){
			return false;
		} else {
			return requirement.getTask().equals(this.getTask())
				&& requirement.getSkill().equals(this.getSkill());
		}
	}

	@Override
	public String getPresentationName() {
		return getSkill().getPresentationName() + " - " + getLevel();
	}

	@SuppressWarnings("unchecked")
	@Override
	public TaskRequirement localize(Locale locale) {
		TaskRequirement returnValue = new TaskRequirement();
		returnValue.setId(getId());
		returnValue.setSkill(getSkill());
		returnValue.setTask(null);
		returnValue.setLevel(getLevel());
		return returnValue;
	}

}
