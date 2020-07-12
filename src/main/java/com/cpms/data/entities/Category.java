package com.cpms.data.entities;

import java.util.HashSet;
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
import com.cpms.exceptions.DataAccessException;

/**
 * Entity class for categories.
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "Category")
public class Category extends AbstractDomainObject implements Comparable<Category>{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@Column(name = "Name", nullable = true, length = 500)
	@Field
	private String name;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Parent", nullable = true)
	@Cascade({CascadeType.DETACH})
	private Category parent;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "category", orphanRemoval = true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE,
        CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH})
	private Set<Category_Termvariant> variants;
	
	public Category() {}
	
	public Category(String name, Category parent) {
		this.name = name;
		setParent(parent);
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParent(Category parent) {
		this.parent = parent;
	}

	@Override
	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Category getParent() {
		return parent;
	}

	@Override
	public Class<?> getEntityClass() {
		return Category.class;
	}

	@Override
	public String getPresentationName() {
		String res = getName();
		Category par = getParent();
		while (par != null) {
			res = "--" + res;
			par = par.getParent();
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Category localize(Locale locale) {
		Category returnValue = new Category();
		returnValue.setName(getName());
		returnValue.setId(getId());
		returnValue.setParent(getParent());
		returnValue.setVariants(getVariants());
		return returnValue;
	}
	
	private String getParentsIDs() {
		String res = "";
		Category cur = this;
		do {
			res = cur.getId() + "-" + res;
			cur = cur.getParent();
		} while (cur != null);
		return res;
	}

	@Override
	public int compareTo(Category o) {
		return this.getParentsIDs().compareTo(o.getParentsIDs());
	}
	
	public boolean equals(Category o) {
		return this.getName().equals(o.getName());
	}
	
	public Set<Category> getChildren(List<Category> all) {
		Set<Category> res = new HashSet<>();
		for (Category cat : all)
			if (cat.getParent() != null && cat.getParent().getId() == getId())
				res.add(cat);
		return res;
	}

	public void addVariant(Category_Termvariant variant) {
		if (variant == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.variants == null) {
			this.getVariants();
		}
		if (!this.variants.stream().anyMatch(
				x -> 
				x.getId() == variant.getId())
				) {
			this.variants.add(variant);
			variant.setCategory(this);
		} 
	}
	
	public void removeVariant(Category_Termvariant variant) {
		if (variant == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.equals(variant.getCategory())) {
			removeEntityFromManagedCollection(variant, variants);
			variant.setCategory(null);
		}
	}
	
	public void clearVariants() {
		if (this.variants == null) {
			this.getVariants();
		}
		variants.clear();
	}
	
	public Set<Category_Termvariant> getVariants() {
		if (variants == null) {
			variants = new LinkedHashSet<Category_Termvariant>() ;
		}
		return new LinkedHashSet<Category_Termvariant>(variants);
	}

	public void setVariants(Set<Category_Termvariant> variants) {
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
}
