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
import javax.validation.constraints.NotNull;

import com.cpms.data.AbstractDomainObject;
import com.cpms.security.entities.User;

/**
 * Entity class for competencies.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "TASKCENTER")
public class TaskCenter extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "USER", nullable = false)
	private User user;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "TASK", nullable = false)
	@NotNull
	private Task task;
	
	public TaskCenter() {}
	
	public TaskCenter(User user) {
		this.user = user;
	}
	
	public TaskCenter(Task task) {
		this.setTask(task);
	}
	

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public Class<?> getEntityClass() {
		return TaskCenter.class;
	}

	@Override
	public String getPresentationName() {
		return getTask().getPresentationName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public TaskCenter localize(Locale locale) {
		TaskCenter returnValue = new TaskCenter();
		returnValue.setId(getId());
		returnValue.setUser(null);
		returnValue.setTask(getTask());
		return returnValue;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}
	
}
