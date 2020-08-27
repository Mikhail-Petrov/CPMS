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

import com.cpms.data.AbstractDomainObject;

@Entity
@SuppressWarnings("serial")
@Table(name = "VotingSessionUser")
public class VotingSessionUser extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "votingsessionid", nullable = true)
	private VotingSession session;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "expertid", nullable = true)
	private Profile expert;
	
	public VotingSessionUser() {}
	
	public VotingSessionUser(VotingSession session, Profile expert) {
		setSession(session);
		setExpert(expert);
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public void setSession(VotingSession term) {
		this.session = term;
	}

	@Override
	public long getId() {
		return id;
	}

	public VotingSession getSession() {
		return session;
	}

	@Override
	public Class<?> getEntityClass() {
		return VotingSessionUser.class;
	}

	@Override
	public String getPresentationName() {
		return "";
	}

	@SuppressWarnings("unchecked")
	@Override
	public VotingSessionUser localize(Locale locale) {
		VotingSessionUser returnValue = new VotingSessionUser();
		returnValue.setId(getId());
		returnValue.setSession(null);
		return returnValue;
	}

	public Profile getExpert() {
		return expert;
	}

	public void setExpert(Profile expert) {
		this.expert = expert;
	}
}
