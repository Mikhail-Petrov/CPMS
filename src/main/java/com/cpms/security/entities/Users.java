package com.cpms.security.entities;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.cpms.data.AbstractDomainObject;
import com.cpms.data.entities.Language;
import com.cpms.data.entities.Message;
import com.cpms.data.entities.MessageCenter;
import com.cpms.data.entities.TaskCenter;
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
@Table(name = "USERS")
@SuppressWarnings("serial")
public class Users extends AbstractDomainObject implements Comparable<Users> {

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

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "owner", orphanRemoval = true)
	@Cascade({CascadeType.DELETE, CascadeType.DETACH})
	private Set<Message> messages;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "uid", orphanRemoval = true)
	@Cascade({CascadeType.DELETE, CascadeType.DETACH})
	private Set<MessageCenter> inMessages;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "uid")
	@Cascade({CascadeType.DELETE, CascadeType.DETACH})
	private Set<TaskCenter> tasks;
	
	public boolean isHashed() {
		return hashed;
	}
	
	public void setHashed(boolean hashed) {
		this.hashed = hashed;
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

	public String getRole() {
		if (roles == null || roles.isEmpty())
			return null;
		return roles.get(0).getRolename();
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
		for (Role oldRole : roles)
			if (oldRole.getRolename().equals(role.getRolename()))
				return;
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
		return checkRole(type.toRoleName()) || checkRole(type.name());
	}
	public boolean checkRole(String roleName) {
		for (Role role : this.getRoles())
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
	public Users localize(Locale locale) {
		return this;
	}

	public Set<Message> getMessages() {
		if (messages == null) {
			messages = new LinkedHashSet<Message>();
		}
		return new LinkedHashSet<Message>(messages);
	}

	public void setMessages(Set<Message> messages) {
		this.messages = messages;
		this.messages.forEach(x -> x.setOwner(this));
	}

	public Set<MessageCenter> getInMessages() {
		if (inMessages == null) {
			inMessages = new LinkedHashSet<MessageCenter>();
		}
		return new LinkedHashSet<MessageCenter>(inMessages);
	}

	public void setInMessages(Set<MessageCenter> inMessages) {
		this.inMessages = inMessages;
		this.inMessages.forEach(x -> x.setUser(this));
	}

	public Set<TaskCenter> getTasks() {
		if (tasks == null) {
			tasks = new LinkedHashSet<TaskCenter>();
		}
		return new LinkedHashSet<TaskCenter>(tasks);
	}

	public void setTasks(Set<TaskCenter> tasks) {
		this.tasks = tasks;
		this.tasks.forEach(x -> x.setUser(this));
	}

	@Override
	public int compareTo(Users object) {
		return this.getPresentationName().toLowerCase().compareTo(object.getPresentationName().toLowerCase());
	}

}
