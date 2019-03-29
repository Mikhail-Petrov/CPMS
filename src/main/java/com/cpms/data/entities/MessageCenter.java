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
import javax.validation.constraints.NotNull;

import com.cpms.data.AbstractDomainObject;
import com.cpms.security.entities.User;

/**
 * Entity class for competencies.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "MESSAGECENTER")
public class MessageCenter extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "USER", nullable = false)
	private User user;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "MESSAGE", nullable = false)
	@NotNull
	private Message message;
	
	@Column(name = "copy", nullable = false)
	private boolean copy = false;
	
	@Column(name = "hidden", nullable = false)
	private boolean hidden = false;
	
	@Column(name = "red", nullable = false)
	private boolean red = false;
	
	public MessageCenter() {}
	
	public MessageCenter(User user) {
		this.user = user;
		this.copy = false;
		this.hidden = false;
		this.setRed(false);
	}
	
	public MessageCenter(Message message) {
		this.message = message;
		this.copy = false;
		this.hidden = false;
	}
	

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	@Override
	public long getId() {
		return id;
	}

	public Message getMessage() {
		return message;
	}

	@Override
	public Class<?> getEntityClass() {
		return MessageCenter.class;
	}

	@Override
	public String getPresentationName() {
		return getMessage().getPresentationName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public MessageCenter localize(Locale locale) {
		MessageCenter returnValue = new MessageCenter();
		returnValue.setId(getId());
		returnValue.setUser(null);
		returnValue.setMessage(getMessage());
		returnValue.setCopy(isCopy());
		returnValue.setHidden(isHidden());
		return returnValue;
	}


	public boolean isCopy() {
		return copy;
	}


	public void setCopy(boolean copy) {
		this.copy = copy;
	}


	public boolean isHidden() {
		return hidden;
	}


	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isRed() {
		return red;
	}

	public void setRed(boolean red) {
		this.red = red;
	}
	
}
