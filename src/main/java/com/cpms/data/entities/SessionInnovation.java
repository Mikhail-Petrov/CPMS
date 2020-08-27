package com.cpms.data.entities;

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

import com.cpms.data.AbstractDomainObject;
import com.cpms.exceptions.DataAccessException;

@Entity
@SuppressWarnings("serial")
@Table(name = "SessionInnovation")
public class SessionInnovation extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "sessionid", nullable = true)
	private VotingSession session;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "sessionInn", orphanRemoval = true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE,
        CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH})
	private Set<Voting> votes;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "innovationid", nullable = true)
	private Task innovation;
	
	public SessionInnovation() {}
	
	public SessionInnovation(VotingSession session, Task innovation) {
		setSession(session);
		setInnovation(innovation);
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
		return SessionInnovation.class;
	}

	@Override
	public String getPresentationName() {
		return "";
	}

	@SuppressWarnings("unchecked")
	@Override
	public SessionInnovation localize(Locale locale) {
		SessionInnovation returnValue = new SessionInnovation();
		returnValue.setId(getId());
		returnValue.setSession(null);
		return returnValue;
	}

	public Voting addVote(Voting vote) {
		if (vote == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.votes == null) {
			this.getVotes();
		}
		this.votes.add(vote);
		vote.setSessionInn(this);
		return vote;
	}
	
	public void removeVote(Voting vote) {
		if (vote == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.equals(vote.getSessionInn())) {
			removeEntityFromManagedCollection(vote, votes);
			vote.setSessionInn(null);
		}
	}
	
	public void clearVotes() {
		if (this.votes == null) {
			this.getVotes();
		}
		votes.clear();
	}
	
	public Set<Voting> getVotes() {
		if (votes == null) {
			votes = new LinkedHashSet<>() ;
		}
		return new LinkedHashSet<>(votes);
	}

	public void setVotes(Set<Voting> votes) {
		if (votes == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.votes == null) {
			this.votes = votes;
		} else {
			throw new DataAccessException("Cannot insert, Hibernate will lose track",
					null);
		}
	}

	public Task getInnovation() {
		return innovation;
	}

	public void setInnovation(Task innovation) {
		this.innovation = innovation;
	}
}
