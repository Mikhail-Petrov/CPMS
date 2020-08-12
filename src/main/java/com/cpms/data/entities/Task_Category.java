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
import javax.validation.constraints.NotNull;

import com.cpms.data.AbstractDomainObject;

@SuppressWarnings("serial")
@Entity
@Table(name = "Task_Category", uniqueConstraints =
		@UniqueConstraint(columnNames = {"category_id", "task_id"}, name = "taskCategoryUnique"))
public class Task_Category extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "task_id", nullable = false)
	@NotNull
	private Task task;
	
	public Task_Category(Category category, Task task) {
		this.setCategory(category);
		this.setTask(task);
	}
	
	public Task_Category() {}

	public void setId(long id) {
		this.id = id;
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public Class<?> getEntityClass() {
		return Task_Category.class;
	}

	@Override
	public String getPresentationName() {
		return "";// getTask().getPresentationName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Task_Category localize(Locale locale) {
		Task_Category returnValue = new Task_Category();
		returnValue.setId(getId());
		returnValue.setCategory(getCategory());
		returnValue.setTask(getTask());
		return returnValue;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task variant) {
		this.task = variant;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
	
}
