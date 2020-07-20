package com.cpms.data.entities;

import java.util.HashSet;
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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.search.annotations.Field;

import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.AbstractDomainObject;
import com.cpms.exceptions.DataAccessException;

/**
 * Entity class for categories.
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "Trend")
public class Trend extends AbstractDomainObject implements Comparable<Trend>{
	
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
	private Trend parent;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "trend", orphanRemoval = true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE,
        CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH})
	private Set<Trend_Termvariant> variants;
	
	public Trend() {}

	public Trend(Trend cat) {
		setName(cat.getName());
		setId(cat.getId());
		if (cat.getParent() == null)
			setParent(null);
		else {
			Trend parent = cat.getParent();
			setParent(new Trend(parent));
			String alternative = "";
			while (parent != null) {
				alternative += "--";
				parent = parent.getParent();
			}
			setName(alternative + "~!@" + getName());
		}
	}
	
	public Trend(String name, Trend parent) {
		this.name = name;
		setParent(parent);
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParent(Trend parent) {
		this.parent = parent;
	}

	@Override
	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Trend getParent() {
		return parent;
	}

	@Override
	public Class<?> getEntityClass() {
		return Trend.class;
	}

	@Override
	public String getPresentationName() {
		String res = getName();
		Trend par = getParent();
		while (par != null) {
			res = "--" + res;
			par = par.getParent();
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Trend localize(Locale locale) {
		Trend returnValue = new Trend();
		returnValue.setName(getName());
		returnValue.setId(getId());
		returnValue.setParent(getParent());
		returnValue.setVariants(getVariants());
		return returnValue;
	}
	
	private String getParentsIDs() {
		String res = "";
		Trend cur = this;
		do {
			res = cur.getId() + "-" + res;
			cur = cur.getParent();
		} while (cur != null);
		return res;
	}

	@Override
	public int compareTo(Trend o) {
		return this.getParentsIDs().compareTo(o.getParentsIDs());
	}
	
	public boolean equals(Trend o) {
		return this.getName().equals(o.getName());
	}
	
	public Set<Trend> getChildren(IDAO<Trend> dao) {
		Set<Trend> res = new HashSet<>();
		for (Trend cat : dao.getChildren(this))
			//if (cat.getParent() != null && cat.getParent().getId() == getId())
				res.add(cat);
		return res;
	}

	public void addVariant(Trend_Termvariant variant) {
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
			variant.setTrend(this);
		} 
	}
	
	public void removeVariant(Trend_Termvariant variant) {
		if (variant == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.equals(variant.getTrend())) {
			removeEntityFromManagedCollection(variant, variants);
			variant.setTrend(null);
		}
	}
	
	public void clearVariants() {
		if (this.variants == null) {
			this.getVariants();
		}
		variants.clear();
	}
	
	public Set<Trend_Termvariant> getVariants() {
		if (variants == null) {
			variants = new LinkedHashSet<Trend_Termvariant>() ;
		}
		return new LinkedHashSet<Trend_Termvariant>(variants);
	}

	public void setVariants(Set<Trend_Termvariant> variants) {
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
