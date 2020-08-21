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

/**
 * Entity class for competencies.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "DocumentTrend", uniqueConstraints =
		@UniqueConstraint(columnNames = {"trendid", "documentid"}, name = "trendDocumentUnique"))
public class DocumentTrend extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "trendid", nullable = false)
	private Trend category;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "documentid", nullable = false)
	@NotNull
	private Article document;
	
	@Column(name = "manual", nullable = true)
	private Integer manual;
	
	public DocumentTrend(Trend trend, Article document) {
		this.setTrend(trend);
		this.setDocument(document);
	}
	
	public DocumentTrend() {}

	public void setId(long id) {
		this.id = id;
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public Class<?> getEntityClass() {
		return DocumentTrend.class;
	}

	@Override
	public String getPresentationName() {
		return getDocument().getPresentationName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public DocumentTrend localize(Locale locale) {
		DocumentTrend returnValue = new DocumentTrend();
		returnValue.setId(getId());
		returnValue.setTrend(getTrend());
		returnValue.setDocument(getDocument());
		return returnValue;
	}

	public Article getDocument() {
		return document;
	}

	public void setDocument(Article document) {
		this.document = document;
	}

	public Trend getTrend() {
		return category;
	}

	public void setTrend(Trend trend) {
		this.category = trend;
	}

	public Integer getManual() {
		return manual;
	}

	public void setManual(Integer manual) {
		this.manual = manual;
	}
	
}
