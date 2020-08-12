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
@Table(name = "Task_Trend", uniqueConstraints =
		@UniqueConstraint(columnNames = {"trend_id", "task_id"}, name = "taskTrendUnique"))
public class Task_Trend extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "trend_id", nullable = false)
	private Trend trend;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "task_id", nullable = false)
	@NotNull
	private Task task;
	
	public Task_Trend(Trend category, Task task) {
		this.setTrend(category);
		this.setTask(task);
	}
	
	public Task_Trend() {}

	public void setId(long id) {
		this.id = id;
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public Class<?> getEntityClass() {
		return Task_Trend.class;
	}

	@Override
	public String getPresentationName() {
		return "";//getTask().getPresentationName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Task_Trend localize(Locale locale) {
		Task_Trend returnValue = new Task_Trend();
		returnValue.setId(getId());
		returnValue.setTrend(getTrend());
		returnValue.setTask(getTask());
		return returnValue;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task variant) {
		this.task = variant;
	}

	public Trend getTrend() {
		return trend;
	}

	public void setTrend(Trend trend) {
		this.trend = trend;
	}
	
}
