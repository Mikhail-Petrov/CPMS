package com.cpms.data.entities;

import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

import com.cpms.data.AbstractDomainObject;

/**
 * Entity class for experts' votes.
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "Voting")
public class Voting extends AbstractDomainObject{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "expertid", nullable = true)
	private Profile expert;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "sessioninnovationid", nullable = true)
	private SessionInnovation sessionInn;

	@Column(name = "votingdate", nullable = true)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Temporal(TemporalType.DATE)
	private Date votingDate;
	
	@Column(name = "spending", nullable = true)
	private Integer spending;
	
	public Voting() {}
	
	public Voting(SessionInnovation sessionInn, Profile expert, int spending) {
		setSessionInn(sessionInn);
		setExpert(expert);
		setSpending(spending);
		setVotingDate(new Date(System.currentTimeMillis()));
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
		return Voting.class;
	}

	@Override
	public String getPresentationName() {
		return "";
	}

	@SuppressWarnings("unchecked")
	@Override
	public Voting localize(Locale locale) {
		Voting returnValue = new Voting();
		return returnValue;
	}

	public Profile getExpert() {
		return expert;
	}

	public void setExpert(Profile expert) {
		this.expert = expert;
	}

	public SessionInnovation getSessionInn() {
		return sessionInn;
	}

	public void setSessionInn(SessionInnovation sessionInn) {
		this.sessionInn = sessionInn;
	}

	public Date getVotingDate() {
		return votingDate;
	}

	public void setVotingDate(Date votingDate) {
		this.votingDate = votingDate;
	}

	public Integer getSpending() {
		if (spending == null) return 0;
		return spending;
	}

	public void setSpending(Integer spending) {
		this.spending = spending;
	}
}
