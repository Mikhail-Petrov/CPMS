package com.cpms.data.entities;

import java.util.Date;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import com.cpms.data.AbstractDomainObject;
import com.cpms.data.EvidenceType;
import com.cpms.data.validation.BilingualValidation;

/**
 * Entity class for evidence.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "EVIDENCE")
@BilingualValidation(fieldOne="description", fieldTwo="description_RU",
	nullable = true, minlength = 0, maxlength = 1000)
public class Evidence extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	private Competency competency;
	
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
	
	public Evidence() {}
	
	public Evidence(Competency competency, EvidenceType type, 
			Date acquiredDate, Date expirationDate, String description) {
		this.competency = competency;
		this.type = type;
		this.acquiredDate = acquiredDate;
		this.expirationDate = expirationDate;
		this.description = description;
	}
	
	public String getDescription_RU() {
		return description_RU;
	}

	public void setDescription_RU(String description_RU) {
		this.description_RU = description_RU;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setCompetency(Competency competency) {
		this.competency = competency;
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

	public Competency getCompetency() {
		return competency;
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

	@SuppressWarnings("unchecked")
	@Override
	public Evidence localize(Locale locale) {
		Evidence returnValue = new Evidence();
		returnValue.setId(getId());
		returnValue.setAcquiredDate(getAcquiredDate());
		returnValue.setExpirationDate(getExpirationDate());
		returnValue.setCompetency(null);
		returnValue.setType(getType());
		returnValue.setDescription(
				localizeBilingualField(getDescription(), getDescription_RU(), locale));
		return returnValue;
	}
	
}
