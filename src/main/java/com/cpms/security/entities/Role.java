package com.cpms.security.entities;

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

import com.cpms.data.DomainObject;

/**
 * Entity representing user's role.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "ROLE")
public class Role implements DomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@Column(name = "ROLENAME", nullable = false, length = 50)
	private String rolename;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "OWNER", nullable = false)
	private User owner;
	
	public String getRolename() {
		return rolename;
	}

	public void setRolename(String rolename) {
		this.rolename = rolename;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public Class<?> getEntityClass() {
		return Role.class;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public boolean equals(Object arg0) {
		return (DomainObject.class.isAssignableFrom(arg0.getClass())
				&& ((DomainObject)arg0).getEntityClass().equals(this.getEntityClass())
				&& ((DomainObject)arg0).getId() == this.getId());
	}

	@Override
	public String getPresentationName() {
		return rolename;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Role localize(Locale locale) {
		return this;
	}
}
