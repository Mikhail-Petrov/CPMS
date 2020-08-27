package com.cpms.data.entities;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.springframework.format.annotation.DateTimeFormat;

import com.cpms.data.AbstractDomainObject;
import com.cpms.data.DomainObject;
import com.cpms.exceptions.DataAccessException;

/**
 * Entity class for voting session.
 */
@SuppressWarnings("serial")
@Entity
@Indexed
@Table(name = "VotingSession")
public class VotingSession extends AbstractDomainObject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;

	@Column(name = "startdate", nullable = true)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Temporal(TemporalType.DATE)
	private Date startDate;

	@Column(name = "enddate", nullable = true)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Temporal(TemporalType.DATE)
	private Date endDate;
	
	@Column(name = "budget", nullable = true)
	private Integer budget;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "session", orphanRemoval = true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE,
        CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH})
	private Set<SessionInnovation> innovations;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "session", orphanRemoval = true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE,
        CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH})
	private Set<VotingSessionUser> users;
	
	public VotingSession() {
	}
	
	public void update(VotingSession session) {
		setStartDate(session.getStartDate());
		setEndDate(session.getEndDate());
		setBudget(session.getBudget());
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
		return VotingSession.class;
	}

	@Override
	public String getPresentationName() {
		return "";
	}

	public SessionInnovation addInnovation(SessionInnovation innovation) {
		if (innovation == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.innovations == null) {
			this.getInnovations();
		}
		this.innovations.add(innovation);
		innovation.setSession(this);
		return innovation;
	}
	
	public void removeInnovation(SessionInnovation innovation) {
		if (innovation == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.equals(innovation.getSession())) {
			removeEntityFromManagedCollection(innovation, innovations);
			innovation.setSession(null);
		}
	}
	
	public void clearInnovations() {
		if (this.innovations == null) {
			this.getInnovations();
		}
		innovations.clear();
	}
	
	public Set<SessionInnovation> getInnovations() {
		if (innovations == null) {
			innovations = new LinkedHashSet<>() ;
		}
		return new LinkedHashSet<>(innovations);
	}

	public void setInnovations(Set<SessionInnovation> innovations) {
		if (innovations == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.innovations == null) {
			this.innovations = innovations;
		} else {
			throw new DataAccessException("Cannot insert, Hibernate will lose track",
					null);
		}
	}

	public VotingSessionUser addUser(VotingSessionUser user) {
		if (user == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.users == null) {
			this.getUsers();
		}
		this.users.add(user);
		user.setSession(this);
		return user;
	}
	
	public void removeUser(VotingSessionUser user) {
		if (user == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.equals(user.getSession())) {
			removeEntityFromManagedCollection(user, users);
			user.setSession(null);
		}
	}
	
	public void clearUsers() {
		if (this.users == null) {
			this.getUsers();
		}
		users.clear();
	}
	
	public Set<VotingSessionUser> getUsers() {
		if (users == null) {
			users = new LinkedHashSet<>() ;
		}
		return new LinkedHashSet<>(users);
	}

	public void setUsers(Set<VotingSessionUser> users) {
		if (users == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.users == null) {
			this.users = users;
		} else {
			throw new DataAccessException("Cannot insert, Hibernate will lose track",
					null);
		}
	}

	@Override
	public <T extends DomainObject> T localize(Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public int getBudget() {
		if (budget == null) return 0;
		return budget;
	}

	public void setBudget(int budget) {
		this.budget = budget;
	}
}
