package com.cpms.data.entities;

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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
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
@Table(name = "Term")
public class Term extends AbstractDomainObject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@Column(name = "preferabletext", nullable = true, length = 1000)
	@Field
	private String pref;
	
	@Column(name = "stemtext", nullable = false, length = 1000)
	private String stem;
	
	@Column(name = "category", nullable = true, length = 100)
	private String category;
	
	@Column(name = "ISinnovation", nullable = false)
	private boolean inn;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "term", orphanRemoval = true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE,
        CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH})
	private Set<TermVariant> variants;
	
	public Term() {
		setInn(false);
		setPref("");
	}
	

	public boolean isInn() {
		return inn;
	}

	public void setInn(boolean inn) {
		this.inn = inn;
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
		return Term.class;
	}

	@Override
	public String getPresentationName() {
		return getPref();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Term localize(Locale locale) {
		Term returnValue = new Term();
		returnValue.setId(getId());
		returnValue.setStem(getStem());
		returnValue.setPref(getPref());
		returnValue.setInn(isInn());
		returnValue.setCategory(getCategory());
		return returnValue;
	}


	public String getPref() {
		return pref;
	}


	public void setPref(String pref) {
		this.pref = pref;
	}


	public String getStem() {
		return stem;
	}


	public void setStem(String stem) {
		this.stem = stem;
	}
	
	public TermVariant addVariant(String text) {
		return addVariant(new TermVariant(text));
	}

	public TermVariant addVariant(TermVariant variant) {
		if (variant == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.variants == null) {
			this.getVariants();
		}
		if (!this.variants.stream().anyMatch(
				x -> 
				x.getText().equals(variant.getText()))
				) {
			if (getPref().isEmpty() || getPref().length() > variant.getText().length())
				setPref(variant.getText());
			this.variants.add(variant);
			variant.setTerm(this);
		} 
		return variant;
	}
	
	public void removeVariant(TermVariant variant) {
		if (variant == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.equals(variant.getTerm())) {
			removeEntityFromManagedCollection(variant, variants);
			variant.setTerm(null);
		}
	}
	
	public void clearVariants() {
		if (this.variants == null) {
			this.getVariants();
		}
		variants.clear();
	}
	
	public Set<TermVariant> getVariants() {
		if (variants == null) {
			variants = new LinkedHashSet<TermVariant>() ;
		}
		return new LinkedHashSet<TermVariant>(variants);
	}

	public void setVariants(Set<TermVariant> variants) {
		if (variants == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.variants == null) {
			this.variants = variants;
		} else {
			throw new DataAccessException("Cannot insert, Hibernate will lose track",
					null);
		}
	}


	public String getCategory() {
		if (category == null) return "";
		return category;
	}


	public void setCategory(String category) {
		this.category = category;
	}
}
