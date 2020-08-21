package com.cpms.data.entities;

import java.util.Date;
import java.util.LinkedHashSet;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.springframework.format.annotation.DateTimeFormat;

import com.cpms.data.AbstractDomainObject;
import com.cpms.exceptions.DataAccessException;

/**
 * Entity class for skill.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Indexed
@Table(name = "Document")
public class Article extends AbstractDomainObject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@Column(name = "URL", nullable = false)
	@Field
	private String url;
	
	@Column(name = "TEXT", nullable = false, length = 10000)
	private String text;
	
	@Column(name = "wordcount", nullable = false)
	private int wordcount;
	
	@Column(name = "title", nullable = true, length = 10000)
	private String title;
	
	@Column(name = "mask", nullable = true, length = 1000)
	private String mask;

	@Column(name = "creationDate", nullable = true)
	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	@Temporal(TemporalType.TIMESTAMP)
	private Date creationDate;

	@Column(name = "parseDate", nullable = false)
	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	@Temporal(TemporalType.TIMESTAMP)
	private Date parseDate;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "document", orphanRemoval = true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE,
        CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH})
	private Set<DocumentCategory> cats;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "document", orphanRemoval = true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE,
        CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH})
	private Set<DocumentTrend> trends;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "websiteid", nullable = true)
	@Cascade({CascadeType.DETACH})
	private Website website;
	
	public Article() {
		setParseDate(new Date(System.currentTimeMillis()));
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
		return Article.class;
	}

	@Override
	public String getPresentationName() {
		return getUrl();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Article localize(Locale locale) {
		Article returnValue = new Article();
		returnValue.setId(getId());
		returnValue.setText(getText());
		returnValue.setMask(getMask());
		returnValue.setUrl(getUrl());
		returnValue.setParseDate(getParseDate());
		returnValue.setWordcount(getWordcount());
		return returnValue;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String title) {
		this.url = title;
	}


	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}


	public Date getParseDate() {
		return parseDate;
	}


	public void setParseDate(Date sendedTime) {
		this.parseDate = sendedTime;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public int getWordcount() {
		return wordcount;
	}

	public void setWordcount(int wordcount) {
		this.wordcount = wordcount;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMask() {
		return mask;
	}

	public void setMask(String mask) {
		this.mask = mask;
	}

	public void addCat(DocumentCategory cat) {
		if (cat == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.cats == null) {
			this.getCats();
		}
		if (!this.cats.stream().anyMatch(
				x -> 
				x.getId() == cat.getId())
				) {
			this.cats.add(cat);
			cat.setDocument(this);
		} 
	}
	
	public void removeCat(DocumentCategory cat) {
		if (cat == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.equals(cat.getDocument())) {
			removeEntityFromManagedCollection(cat, cats);
			cat.setDocument(null);
		}
	}
	
	public void clearCats() {
		if (this.cats == null) {
			this.getCats();
		}
		cats.clear();
	}
	
	public Set<DocumentCategory> getCats() {
		if (cats == null) {
			cats = new LinkedHashSet<>() ;
		}
		return new LinkedHashSet<>(cats);
	}

	public void setCats(Set<DocumentCategory> cats) {
		if (cats == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.cats == null) {
			this.cats = cats;
		} else {
			throw new DataAccessException("Cannot insert, Hibernate will lose track",
					null);
		}
	}

	public void addTrend(DocumentTrend trend) {
		if (trend == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.trends == null) {
			this.getTrends();
		}
		if (!this.trends.stream().anyMatch(
				x -> 
				x.getId() == trend.getId())
				) {
			this.trends.add(trend);
			trend.setDocument(this);
		} 
	}
	
	public void removeTrend(DocumentTrend trend) {
		if (trend == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.equals(trend.getDocument())) {
			removeEntityFromManagedCollection(trend, trends);
			trend.setDocument(null);
		}
	}
	
	public void clearTrends() {
		if (this.trends == null) {
			this.getTrends();
		}
		trends.clear();
	}
	
	public Set<DocumentTrend> getTrends() {
		if (trends == null) {
			trends = new LinkedHashSet<>() ;
		}
		return new LinkedHashSet<>(trends);
	}

	public void setTrends(Set<DocumentTrend> trends) {
		if (trends == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.trends == null) {
			this.trends = trends;
		} else {
			throw new DataAccessException("Cannot insert, Hibernate will lose track",
					null);
		}
	}

	public Website getWebsite() {
		return website;
	}

	public void setWebsite(Website website) {
		this.website = website;
	}
	
	
}
