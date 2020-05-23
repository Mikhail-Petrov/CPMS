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

	@Column(name = "creationDate", nullable = true)
	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	@Temporal(TemporalType.TIMESTAMP)
	private Date creationDate;

	@Column(name = "parseDate", nullable = false)
	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	@Temporal(TemporalType.TIMESTAMP)
	private Date parseDate;
	
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
	
	
}