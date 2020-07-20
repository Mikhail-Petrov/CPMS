package com.cpms.data.entities;

import java.util.HashSet;
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
import javax.persistence.Table;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.search.annotations.Field;

import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.AbstractDomainObject;

/**
 * Entity class for categories.
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "Website")
public class Website extends AbstractDomainObject implements Comparable<Website>{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@Column(name = "Name", nullable = true, length = 500)
	@Field
	private String name;
	
	@Column(name = "URL", nullable = true, length = 500)
	@Field
	private String url;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Parent", nullable = true)
	@Cascade({CascadeType.DETACH})
	private Website parent;
	
	public Website() {}
	
	public Website(Website cat) {
		setName(cat.getName());
		setId(cat.getId());
		if (cat.getParent() == null)
			setParent(null);
		else {
			Website parent = cat.getParent();
			setParent(new Website(parent));
			String alternative = "";
			while (parent != null) {
				alternative += "--";
				parent = parent.getParent();
			}
			setName(alternative + "~!@" + getName());
		}
	}
	
	public Website(String name, Website parent) {
		this.name = name;
		setParent(parent);
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParent(Website parent) {
		this.parent = parent;
	}

	@Override
	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Website getParent() {
		return parent;
	}

	@Override
	public Class<?> getEntityClass() {
		return Website.class;
	}

	@Override
	public String getPresentationName() {
		String res = getName();
		Website par = getParent();
		while (par != null) {
			res = "--" + res;
			par = par.getParent();
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Website localize(Locale locale) {
		Website returnValue = new Website();
		returnValue.setName(getName());
		returnValue.setUrl(getUrl());
		returnValue.setId(getId());
		returnValue.setParent(getParent());
		return returnValue;
	}
	
	private String getParentsIDs() {
		String res = "";
		Website cur = this;
		do {
			res = cur.getId() + "-" + res;
			cur = cur.getParent();
		} while (cur != null);
		return res;
	}

	@Override
	public int compareTo(Website o) {
		return this.getParentsIDs().compareTo(o.getParentsIDs());
	}
	
	public boolean equals(Website o) {
		return this.getName().equals(o.getName());
	}
	
	public Set<Website> getChildren(IDAO<Website> dao) {
		Set<Website> res = new HashSet<>();
		for (Website cat : dao.getChildren(this))
			//if (cat.getParent() != null && cat.getParent().getId() == getId())
				res.add(cat);
		return res;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
