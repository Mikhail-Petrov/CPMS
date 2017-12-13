package com.cpms.security.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.cpms.data.AbstractDomainObject;
import com.cpms.exceptions.DataAccessException;
import com.cpms.security.RoleTypes;
import com.cpms.security.SecurityUser;

/**
 * Entity representing user.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Entity
@Table(name = "USER")
@SuppressWarnings("serial")
public class User extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	protected long id;
	
	@Column(name = "username", nullable = false, unique = true, length = 100)
	protected String username;
	
	@Column(name = "PASSWORD", nullable = false, length = 60)
	protected String password;
	
	@Column(name = "profileId", nullable = true)
	protected Long profileId; //TODO think of a safer way
	
	@Transient
	protected boolean hashed = false;
	
	@OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "owner")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE,
        CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH})
	protected List<Role> roles;
	
	public boolean isHashed() {
		return hashed;
	}
	
	public Long getProfileId() {
		return profileId;
	}

	public void setProfileId(Long profileId) {
		this.profileId = profileId;
	}

	public void hashPassword() {
		if (!hashed) {
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			password = encoder.encode(password);
			hashed = true;
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
		hashed = false;
	}

	public void setId(long id) {
		this.id = id;
	}

	public List<Role> getRoles() {
		if (roles == null) {
			roles = new ArrayList<Role>();
		}
		return new ArrayList<Role>(roles);
	}
	
	public void setRoles(List<Role> roles) {
		if (roles == null) {
			throw new DataAccessException("Null value.", null);
		}
		this.roles = roles;
		this.roles.forEach(x -> x.setOwner(this));
	}
	
	public void addRole(Role role) {
		if (role == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (roles == null) {
			this.getRoles();
		}
		roles.add(role);
		role.setOwner(this);
	}
	
	public void removeRole(Role role) {
		if (role == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (roles == null) {
			this.getRoles();
		}
		for (Role userRole : new ArrayList<Role>(roles))
			if (userRole.getRolename().equals((role.getRolename()))) {
				roles.remove(userRole);
				userRole.setOwner(null);
			}
	}
	
	public boolean checkRole(RoleTypes type) {
		return checkRole(type.toString());
	}
	public boolean checkRole(String roleName) {
		for (Role role : this.roles)
			if (role.getRolename().equals(roleName))
				return true;
		return false;
	}

	@Override
	public Class<?> getEntityClass() {
		return SecurityUser.class;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getPresentationName() {
		return username;
	}

	@SuppressWarnings("unchecked")
	@Override
	public User localize(Locale locale) {
		return this;
	}

}
