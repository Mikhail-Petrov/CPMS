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
@Table(name = "DocumentCategory", uniqueConstraints =
		@UniqueConstraint(columnNames = {"categoryid", "documentid"}, name = "categoryDocumentUnique"))
public class DocumentCategory extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "categoryid", nullable = false)
	private Category category;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "documentid", nullable = false)
	@NotNull
	private Article document;
	
	@Column(name = "manual", nullable = true)
	private Integer manual;
	
	public DocumentCategory(Category category, Article document) {
		this.setCategory(category);
		this.setDocument(document);
	}
	
	public DocumentCategory() {}

	public void setId(long id) {
		this.id = id;
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public Class<?> getEntityClass() {
		return DocumentCategory.class;
	}

	@Override
	public String getPresentationName() {
		return getDocument().getPresentationName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public DocumentCategory localize(Locale locale) {
		DocumentCategory returnValue = new DocumentCategory();
		returnValue.setId(getId());
		returnValue.setCategory(getCategory());
		returnValue.setDocument(getDocument());
		return returnValue;
	}

	public Article getDocument() {
		return document;
	}

	public void setDocument(Article document) {
		this.document = document;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Integer getManual() {
		return manual;
	}

	public void setManual(Integer manual) {
		this.manual = manual;
	}
	
}
