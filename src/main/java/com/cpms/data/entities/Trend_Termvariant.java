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

@SuppressWarnings("serial")
@Entity
@Table(name = "Trend_Termvariant", uniqueConstraints =
		@UniqueConstraint(columnNames = {"trend", "variant"}, name = "trendVariantUnique"))
public class Trend_Termvariant extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "trend", nullable = false)
	private Trend trend;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "variant", nullable = false)
	@NotNull
	private TermVariant variant;
	
	public Trend_Termvariant(Trend category, TermVariant variant) {
		this.setTrend(category);
		this.setVariant(variant);
	}
	
	public Trend_Termvariant() {}

	public void setId(long id) {
		this.id = id;
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public Class<?> getEntityClass() {
		return Trend_Termvariant.class;
	}

	@Override
	public String getPresentationName() {
		return getVariant().getPresentationName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Trend_Termvariant localize(Locale locale) {
		Trend_Termvariant returnValue = new Trend_Termvariant();
		returnValue.setId(getId());
		returnValue.setTrend(getTrend());
		returnValue.setVariant(getVariant());
		return returnValue;
	}

	public TermVariant getVariant() {
		return variant;
	}

	public void setVariant(TermVariant variant) {
		this.variant = variant;
	}

	public Trend getTrend() {
		return trend;
	}

	public void setTrend(Trend trend) {
		this.trend = trend;
	}
	
}
