package com.cpms.data.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.search.annotations.Field;

import com.cpms.data.AbstractDomainObject;
import com.cpms.web.controllers.Skills;

/**
 * Entity class for motivations.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "MOTIVATION")
public class Motivation extends AbstractDomainObject implements Comparable<Motivation>{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@Column(name = "Description", nullable = true, length = 1000)
	private String description;
	
	@Column(name = "NAME", nullable = true, length = 100)
	@Field
	private String name;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "PARENT", nullable = true)
	@Cascade({CascadeType.DETACH})
	private Motivation parent;
	
	@Column(name = "Code", nullable = true, length = 255)
	private String code;
	
	@Column(name = "Cost", nullable = true)
	private int cost;
	
	@Column(name = "isGroup", nullable = false)
	private boolean isGroup = false;
	
	@Column(name = "LocalCompany", nullable = true, length = 1000)
	private String local;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "parent")
	@Cascade({CascadeType.DETACH})
	private Set<Motivation> children;
	
	public Motivation() {
		description = "";
		name = "";
		code = "";
		local = "";
	}
	
	public Motivation(String description, String name, Motivation parent, String code, int cost, boolean isGroup, String local) {
		this.description = description;
		this.name = name;
		this.setParent(parent);
		this.code = code;
		this.cost = cost;
		this.isGroup = isGroup;
		this.local = local;
	}
	
	public Motivation(Motivation source) {
		this(source.getDescription(), source.getName(), source.getParent(), source.getCode(), source.getCost(), source.getIsGroup(), source.getLocal());
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
		return Motivation.class;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
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
	public Motivation localize(Locale locale) {
		return new Motivation(description, name, getParent(), code, cost, isGroup, local);
	}

	public boolean getIsGroup() {
		return isGroup;
	}

	public void setIsGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}

	public Motivation getParent() {
		return parent;
	}

	public void setParent(Motivation parent) {
		this.parent = parent;
	}

	public Set<Motivation> getChildren() {
		if (children == null) {
			children = new LinkedHashSet<Motivation>() ;
		}
		return new LinkedHashSet<Motivation>(children);
	}

	public void setChildren(Set<Motivation> children) {
		this.children = children;
	}
	
	public List<Motivation> getChildrenSorted() {
		List<Motivation> result = new ArrayList<Motivation>();
		for (Motivation motivation : getChildren())
			result.add(motivation);
		Collections.sort(result);
		return result;
	}

	@Override
	public int compareTo(Motivation o) {
		return this.getPresentationName().toLowerCase().compareTo(o.getPresentationName().toLowerCase());
	}

	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}
	
}
