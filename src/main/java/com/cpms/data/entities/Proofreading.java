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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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
@Table(name = "Proofreadings", uniqueConstraints =
		@UniqueConstraint(columnNames = {"OWNER", "LangFrom", "LangTo"}, name = "OwnerLangsUnique"))
public class Proofreading extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "owner", nullable = false)
	private Profile owner;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "LangFrom", nullable = false)
	@NotNull
	private Language from;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "LangTo", nullable = false)
	@NotNull
	private Language to;
	
	public Proofreading() {}
	
	public Proofreading(Proofreading proof) {
		setId(proof.getId());
		setOwner(proof.getOwner());
		setFrom(proof.getFrom());
		setTo(proof.getTo());
	}
	
	public Proofreading(Language from, Language to) {
		setFrom(from);
		setTo(to);
	}

	public Profile getOwner() {
		return owner;
	}

	public void setOwner(Profile owner) {
		this.owner = owner;
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
		return Proofreading.class;
	}

	@Override
	public String getPresentationName() {
		return String.format("%s-->%s", from.getPresentationName(), to.getPresentationName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Proofreading localize(Locale locale) {
		return new Proofreading(this);
	}

	public Language getFrom() {
		return from;
	}

	public void setFrom(Language from) {
		this.from = from;
	}

	public Language getTo() {
		return to;
	}

	public void setTo(Language to) {
		this.to = to;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!o.getClass().equals(Proofreading.class)) return false;
		Proofreading that = (Proofreading) o;
		return (getOwner().equals(that.getOwner()) && getFrom().equals(that.getFrom()) && getTo().equals(that.getTo()));
	}
	
}
