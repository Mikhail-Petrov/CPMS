package com.cpms.data.entities;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.search.annotations.DocumentId;

import com.cpms.data.AbstractDomainObject;
import com.cpms.exceptions.DataAccessException;

/**
 * Superclass for all types of competency profiles.
 * 
 * @see Company
 * @see Person
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "PROFILE")
public abstract class Profile extends AbstractDomainObject implements Comparable<Profile> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	@DocumentId
	private long id;
	
	@Column(name = "Price", nullable = false)
	private int price;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "owner", orphanRemoval = true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE,
        CascadeType.MERGE, CascadeType.PERSIST})
	private Set<Competency> competencies;
	
	@Column(nullable = true)
	private Date createdDate;
	
	@PrePersist
	protected void onCreate() {
	    createdDate = new Date();
	}

	@Override
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	
	public int getPrice() {
		return price;
	}
	
	public void setPrice(int price) {
		this.price = price;
	}

	public void removeCompetency(Competency competency) {
		if (competency == null) {
			throw new DataAccessException("Null value.", null);
		}
		if(this.equals(competency.getOwner())) {
			removeEntityFromManagedCollection(competency, competencies);
			competency.setOwner(null);
		}
	}

	public void addCompetency(Competency competency) {
		if (competency == null || competency.getSkill() == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (competency.getLevel() > competency.getSkill().getMaxLevel()
				|| competency.getLevel() < 1) {
			throw new DataAccessException("Competency with invalid level inserted",
					null);
		}
		if (competencies == null) {
			getCompetencies();
		}
		if (competencies.stream()
				.anyMatch(x -> x.duplicates(competency))) {
			throw new DataAccessException("Duplicate competency insertion.", null);
		}
		competencies.add(competency);
		competency.setOwner(this);
	}

	public void setCompetencies(Set<Competency> competencies) {
		if (this.competencies != null) {
			throw new DataAccessException("Cannot insert, Hibernate will lose track!");
		}
		this.competencies = competencies;
		this.competencies.forEach(x -> x.setOwner(this));
	}

	public Set<Competency> getCompetencies() {
		if (competencies == null) {
			competencies = new LinkedHashSet<Competency>();
		}
		return new LinkedHashSet<Competency>(competencies);
	}

	@Override
	public Class<?> getEntityClass() {
		return Profile.class;
	}
	
	@Override
	public abstract String getPresentationName();
	
	/**
	 * It might be impossible to cast "Profile" instance to specific
	 * subclass because of proxy classes used. In that case, this method can be
	 * used to get an unproxied instance.
	 */
	public abstract Profile clone();
	
	/**
	 * It might be impossible to cast "Profile" instance to specific
	 * subclass because of proxy classes used. In that case, this method can be
	 * used to set instance's subclass related properties according to it's copy.
	 * 
	 * @param source entity whose fields should be applied to this entity.
	 * Must be of the same entity class.
	 */
	public abstract void update(Profile source);


	@Override
	public int compareTo(Profile p) {
		return getPresentationName().toLowerCase().compareTo(p.getPresentationName().toLowerCase());
	}
}
