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
@Table(name = "Category_Termvariant", uniqueConstraints =
		@UniqueConstraint(columnNames = {"category", "variant"}, name = "categoryVariantUnique"))
public class Category_Termvariant extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "category", nullable = false)
	private Category category;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "variant", nullable = false)
	@NotNull
	private TermVariant variant;
	
	public Category_Termvariant(Category category, TermVariant variant) {
		this.setCategory(category);
		this.setVariant(variant);
	}
	
	public Category_Termvariant() {}

	public void setId(long id) {
		this.id = id;
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public Class<?> getEntityClass() {
		return Category_Termvariant.class;
	}

	@Override
	public String getPresentationName() {
		return getVariant().getPresentationName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Category_Termvariant localize(Locale locale) {
		Category_Termvariant returnValue = new Category_Termvariant();
		returnValue.setId(getId());
		returnValue.setCategory(getCategory());
		returnValue.setVariant(getVariant());
		return returnValue;
	}

	public TermVariant getVariant() {
		return variant;
	}

	public void setVariant(TermVariant variant) {
		this.variant = variant;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
	
}
