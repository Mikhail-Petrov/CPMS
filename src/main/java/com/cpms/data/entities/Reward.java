package com.cpms.data.entities;

import java.util.Locale;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.search.annotations.Field;

import com.cpms.data.AbstractDomainObject;

/**
 * Entity class for rewards.
 * 
 * @author Petrov Mikhail
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "REWARDS")
public class Reward extends AbstractDomainObject implements Comparable<Reward>{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@Column(name = "Description", nullable = true, length = 1000)
	private String description;
	
	@Column(name = "NAME", nullable = true, length = 100)
	@Field
	private String name;
	
	@Column(name = "Experts", nullable = true, length = 1000)
	private String experts;
	
	@Column(name = "Motivations", nullable = true, length = 1000)
	private String motivations;
	
	public Reward() {
		description = "";
		name = "";
		setExperts("");
	}
	
	public Reward(String description, String name, String experts, String motivations) {
		this.description = description;
		this.name = name;
		this.setExperts(experts);
		this.setMotivations(motivations);
	}
	
	public Reward(Reward source) {
		this(source.getDescription(), source.getName(), source.getExperts(), source.getMotivations());
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public Class<?> getEntityClass() {
		return Reward.class;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getPresentationName() {
		return name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Reward localize(Locale locale) {
		return new Reward(description, name, experts, motivations);
	}

	@Override
	public int compareTo(Reward o) {
		return this.getPresentationName().compareTo(o.getPresentationName());
	}

	public String getExperts() {
		return experts;
	}

	public void setExperts(String experts) {
		this.experts = experts;
	}

	public String getMotivations() {
		return motivations;
	}

	public void setMotivations(String motivations) {
		this.motivations = motivations;
	}
	
}
