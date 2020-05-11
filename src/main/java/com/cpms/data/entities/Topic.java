package com.cpms.data.entities;

import java.util.Locale;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import com.cpms.data.AbstractDomainObject;


@Entity
@SuppressWarnings("serial")
@Table(name = "Topic")
public class Topic extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@Column(name = "name", nullable = true, length = 1000)
	private String name;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "domain")
	@Cascade({CascadeType.DETACH})
	private Set<Keyword> keywords;
	
	public Topic() {}
	
	public void setId(long id) {
		this.id = id;
	}

	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public Class<?> getEntityClass() {
		return Topic.class;
	}

	@Override
	public String getPresentationName() {
		return getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Topic localize(Locale locale) {
		return new Topic();
	}

	public String getName() {
		return name;
	}

	public void setName(String code) {
		this.name = code;
	}

	public Set<Keyword> getKeywords() {
		return keywords;
	}

	public void setKeywords(Set<Keyword> keywords) {
		this.keywords = keywords;
	}
}
