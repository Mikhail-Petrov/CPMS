package com.cpms.data.entities;

import java.util.Date;
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
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.format.annotation.DateTimeFormat;

import com.cpms.data.AbstractDomainObject;
import com.cpms.exceptions.DataAccessException;

/**
 * Superclass for all types of competency profiles.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "PROFILE")
public class Profile extends AbstractDomainObject implements Comparable<Profile> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@Column(name = "Price", nullable = false)
	private int price;
	
	@Column(name = "Name", nullable = false)
	private String name;
	
	@Column(name = "Position", nullable = true)
	private String position;
	
	@Column(name = "ProofLevel", nullable = true)
	private String prooflevel;
	
	@Column(name = "ExpertLevel", nullable = true)
	private String level;
	
	@Column(name = "Availability", nullable = true)
	private String availability;

	@Column(name = "Start", nullable = true)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Temporal(TemporalType.DATE)
	private Date startDate;

	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "LocalCompany", nullable = true)
	@Cascade({CascadeType.DETACH})
	private Language local;
	
	private String about;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "owner", orphanRemoval = true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE,
        CascadeType.MERGE, CascadeType.PERSIST})
	private Set<Competency> competencies;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "owner", orphanRemoval = true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE,
        CascadeType.MERGE, CascadeType.PERSIST})
    private Set<Proofreading> proofs;
	
	@Column(nullable = true)
	private Date createdDate;
	
	public Profile() {}
	
	public Profile(String name, String position, String prooflevel,
			String level, String availability, Language local) {
		setName(name);
		setPosition(position);
		setProoflevel(prooflevel);
		setLevel(level);
		setAvailability(availability);
		setLocal(local);
	}
	
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
	public String getPresentationName() {
		return getName();
	}
	
	/**
	 * It might be impossible to cast "Profile" instance to specific
	 * subclass because of proxy classes used. In that case, this method can be
	 * used to get an unproxied instance.
	 */
	public Profile clone() {
		Profile clone = new Profile();
		clone.setId(getId());
		clone.setName(getName());
		clone.setPosition(getPosition());
		clone.setProoflevel(getProoflevel());
		clone.setLevel(getLevel());
		clone.setAvailability(getAvailability());
		clone.setLocal(getLocal());
		clone.setStartDate(getStartDate());
		return clone;
	}
	
	/**
	 * It might be impossible to cast "Profile" instance to specific
	 * subclass because of proxy classes used. In that case, this method can be
	 * used to set instance's subclass related properties according to it's copy.
	 * 
	 * @param source entity whose fields should be applied to this entity.
	 * Must be of the same entity class.
	 */
	public void update(Profile source) {
		setName(source.getName());
		setPosition(source.getPosition());
		setProoflevel(source.getProoflevel());
		setLevel(source.getLevel());
		setAvailability(source.getAvailability());
		setLocal(source.getLocal());
		setStartDate(source.getStartDate());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Profile localize(Locale locale) {
		Profile returnValue = (Profile)this.clone();
		getCompetencies()
			.forEach(x -> returnValue.addCompetency(x.localize(locale)));
		return returnValue;
	}


	@Override
	public int compareTo(Profile p) {
		return getPresentationName().toLowerCase().compareTo(p.getPresentationName().toLowerCase());
	}

	public Set<Proofreading> getProofs() {
		if (proofs == null)
			setProofs(new HashSet<>());
		return proofs;
	}

	public void setProofs(Set<Proofreading> proofs) {
		this.proofs = proofs;
	}
	
	public void addProof(Proofreading newProof) {
		if (newProof == null) return;
		if (getProofs().stream().anyMatch(
				x -> x.getTo().equals(newProof.getTo()) && x.getFrom().equals(newProof.getFrom())))
			return;
		
		getProofs().add(newProof);
		newProof.setOwner(this);
	}
	
	public void addProofsFromText(List<Language> langs, String text) {
		if (text == null) return;
		Set<Proofreading> oldProofs = getProofs();
		setProofs(null); getProofs();
		for (String proof : text.split(";")) {
			if (proof.isEmpty()) continue;
			Language from = null, to = null;
			String[] codes = proof.split(" -- ");
			if (codes.length < 2) continue;
			for (Language lang : langs)
				if (lang.getCode().equals(codes[0]))
					from = lang;
				else if (lang.getCode().equals(codes[1]))
					to = lang;
			if (from != null && to != null) {
				Proofreading newProof = new Proofreading(from, to);
				for (Proofreading oldProof : oldProofs)
					if (oldProof.getFrom().equals(from) && oldProof.getTo().equals(to)) {
						newProof = oldProof;
						break;
					}
				addProof(newProof);
			}
		}
	}
	
	public String getTextFromProofs() {
		String res = "";
		for (Proofreading proof : getProofs()) {
			res += proof.getFrom().getId() + " -- " + proof.getTo().getId() + ";";
		}
		return res;
	}
	
	public void removeProof(Proofreading newProof) {
		if (newProof == null) {
			throw new DataAccessException("Null value.", null);
		}
		if(this.equals(newProof.getOwner())) {
			removeEntityFromManagedCollection(newProof, getProofs());
			newProof.setOwner(null);
		}
	}

	public String getAvailability() {
		return availability;
	}

	public void setAvailability(String availability) {
		this.availability = availability;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getProoflevel() {
		return prooflevel;
	}

	public void setProoflevel(String prooflevel) {
		this.prooflevel = prooflevel;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}
	
	public String getPresentationProofs() {
		String res = "";
		for (Proofreading proof : getProofs())
			res += (res.isEmpty() ? "" : ", ") + proof.getPresentationName();
		return res;
	}

	public Language getLocal() {
		return local;
	}

	public void setLocal(Language local) {
		this.local = local;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
}
