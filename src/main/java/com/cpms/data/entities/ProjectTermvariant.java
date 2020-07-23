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
@Table(name = "ProjectTermvariant", uniqueConstraints =
		@UniqueConstraint(columnNames = {"taskid", "termvariantid"}, name = "taskVariantUnique"))
public class ProjectTermvariant extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "taskid", nullable = false)
	private Task task;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "termvariantid", nullable = false)
	@NotNull
	private TermVariant variant;
	
	public ProjectTermvariant(Task project, TermVariant variant) {
		this.setTask(project);
		this.setVariant(variant);
	}
	
	public ProjectTermvariant() {}

	public void setId(long id) {
		this.id = id;
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public Class<?> getEntityClass() {
		return ProjectTermvariant.class;
	}

	@Override
	public String getPresentationName() {
		return getVariant().getPresentationName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProjectTermvariant localize(Locale locale) {
		ProjectTermvariant returnValue = new ProjectTermvariant();
		returnValue.setId(getId());
		returnValue.setTask(getTask());
		returnValue.setVariant(getVariant());
		return returnValue;
	}

	public TermVariant getVariant() {
		return variant;
	}

	public void setVariant(TermVariant variant) {
		this.variant = variant;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task trend) {
		this.task = trend;
	}
	
}
