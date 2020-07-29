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
	
	@Column(name = "linkMask", nullable = true, length = 100)
	@Field
	private String linkMask;
	
	@Column(name = "articleMask", nullable = true, length = 100)
	@Field
	private String articleMask;
	
	@Column(name = "dateMask", nullable = true, length = 50)
	@Field
	private String dateMask;
	
	@Column(name = "dateFormat", nullable = true, length = 50)
	@Field
	private String dateFormat;
	
	@Column(name = "dateAttribute", nullable = true, length = 50)
	@Field
	private String dateAttribute;
	
	@Column(name = "pageFormat", nullable = true, length = 50)
	@Field
	private String pageFormat;
	
	@Column(name = "show", nullable = true)
	@Field
	private Integer show;
	
	@Column(name = "extract", nullable = true)
	@Field
	private Integer extract;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Parent", nullable = true)
	@Cascade({CascadeType.DETACH})
	private Website parent;
	
	public Website() {}
	
	public void getMissingFields() {
		if (getParent() == null) return;
		if (getLinkMask() == null || getLinkMask().isEmpty())
			setLinkMask(parent.getLinkMask());
		if (getArticleMask() == null || getArticleMask().isEmpty())
			setArticleMask(parent.getArticleMask());
		if (getDateMask() == null || getDateMask().isEmpty())
			setDateMask(parent.getDateMask());
		if (getDateFormat() == null || getDateFormat().isEmpty())
			setDateFormat(parent.getDateFormat());
		if (getDateAttribute() == null || getDateAttribute().isEmpty())
			setDateAttribute(parent.getDateAttribute());
		if (getPageFormat() == null || getPageFormat().isEmpty())
			setPageFormat(parent.getPageFormat());
	}
	public Website(Website site) {
		setName(site.getName());
		setId(site.getId());
		if (site.getParent() == null)
			setParent(null);
		else {
			Website parent = site.getParent();
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
		returnValue.setArticleMask(getArticleMask());
		returnValue.setLinkMask(getLinkMask());
		returnValue.setDateAttribute(getDateAttribute());
		returnValue.setDateFormat(getDateFormat());
		returnValue.setDateMask(getDateMask());
		returnValue.setPageFormat(getPageFormat());
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

	public String getPageFormat() {
		return pageFormat;
	}

	public void setPageFormat(String pageFormat) {
		this.pageFormat = pageFormat;
	}

	public String getDateAttribute() {
		return dateAttribute;
	}

	public void setDateAttribute(String dateAttribute) {
		this.dateAttribute = dateAttribute;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getDateMask() {
		return dateMask;
	}

	public void setDateMask(String dateMask) {
		this.dateMask = dateMask;
	}

	public String getArticleMask() {
		return articleMask;
	}

	public void setArticleMask(String articleMask) {
		this.articleMask = articleMask;
	}

	public String getLinkMask() {
		return linkMask;
	}

	public void setLinkMask(String linkMask) {
		this.linkMask = linkMask;
	}

	public Integer getShow() {
		if (show == null) return 0;
		return show;
	}

	public void setShow(Integer show) {
		this.show = show;
	}

	public Integer getExtract() {
		if (extract == null) return 0;
		return extract;
	}

	public void setExtract(Integer extract) {
		this.extract = extract;
	}
}
