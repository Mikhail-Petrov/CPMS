package com.cpms.data.applications;

import java.util.Date;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import org.apache.commons.lang.NotImplementedException;

import com.cpms.data.AbstractDomainObject;
import com.cpms.data.DomainObject;
import com.cpms.data.EvidenceType;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Evidence;
import com.cpms.data.entities.Profile;
import com.cpms.data.validation.BilingualValidation;

/**
 * Entity class for resident's applications to create new evidence within
 * system. Note that all dependencies are manually managed.
 * 
 * @see Evidence
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "EVIDENCEAPPLICATION")
@BilingualValidation(fieldOne="description", fieldTwo="description_RU",
	nullable = true, minlength = 0, maxlength = 1000)
public class EvidenceApplication extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@Column(name = "COMPETENCYID")
	@NotNull
	private long competencyId;
	
	@Column(name = "OWNERID")
	@NotNull
	private long ownerId;
	
	@Column(name = "Type", nullable = false)
	@Enumerated(EnumType.STRING)
	@NotNull
	private EvidenceType type;
	
	@Column(name = "AcquiredDate", nullable = false)
	@Temporal(TemporalType.DATE)
	@NotNull
	@Past
	private Date acquiredDate;
	
	@Column(name = "ExpirationDate", nullable = true)
	@Temporal(TemporalType.DATE)
	private Date expirationDate;
	
	@Column(name = "Description", nullable = true, length = 1000)
	private String description;
	
	@Column(name = "Description_RU", nullable = true, length = 1000)
	private String description_RU;
	
	@Transient
	private Profile owner;
	
	@Transient
	private CompetencyApplication competency;
	
	public EvidenceApplication(CompetencyApplication competency, 
			EvidenceType type, Date acquiredDate, 
			Date expirationDate, String description) {
		this.competencyId = competency.getId();
		this.type = type;
		this.acquiredDate = acquiredDate;
		this.expirationDate = expirationDate;
		this.description = description;
	}
	
	public EvidenceApplication(long competencyId, EvidenceType type, 
			Date acquiredDate, Date expirationDate, String description) {
		this.competencyId = competencyId;
		this.type = type;
		this.acquiredDate = acquiredDate;
		this.expirationDate = expirationDate;
		this.description = description;
	}
	
	public EvidenceApplication() {}
	
	public String getDescription_RU() {
		return description_RU;
	}

	public void setDescription_RU(String description_RU) {
		this.description_RU = description_RU;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(long ownerId) {
		this.ownerId = ownerId;
	}

	public void setCompetencyId(long competencyId) {
		this.competencyId = competencyId;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setCompetencyId(Competency competency) {
		this.competencyId = competency.getId();
	}

	public void setType(EvidenceType type) {
		this.type = type;
	}

	public void setAcquiredDate(Date acquiredDate) {
		this.acquiredDate = acquiredDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getCompetencyId() {
		return competencyId;
	}

	public EvidenceType getType() {
		return type;
	}

	public Date getAcquiredDate() {
		return acquiredDate;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public String getDescription() {
		return description;
	}

	public Profile getOwner() {
		return owner;
	}

	public void setOwner(Profile owner) {
		this.owner = owner;
	}

	public CompetencyApplication getCompetency() {
		return competency;
	}
	
	public void setCompetency(CompetencyApplication competency) {
		this.competency = competency;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public Class<?> getEntityClass() {
		return Evidence.class;
	}

	@Override
	public String getPresentationName() {
		return description;
	}

	@Override
	public <T extends DomainObject> T localize(Locale locale) {
		throw new NotImplementedException();
	}
	
}
