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
import com.cpms.security.entities.Users;

/**
 * Entity class for competencies.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "MESSAGECENTER")
public class MessageCenter extends AbstractDomainObject implements Comparable<MessageCenter> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "UID", nullable = false)
	private Users uid;
	
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
	
	public MessageCenter(Users uid) {
		this.uid = uid;
		this.copy = false;
		this.hidden = false;
		this.setRed(false);
	}
	
	public MessageCenter(Message message) {
		this.message = message;
		this.copy = false;
		this.hidden = false;
	}
	

	public Users getUser() {
		return uid;
	}

	public void setUser(Users uid) {
		this.uid = uid;
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

	@Override
	public int compareTo(MessageCenter mes) {
		if (getMessage() == null || getMessage().getSendedTime() == null) return -1;
		if (mes == null || mes.getMessage() == null || mes.getMessage().getSendedTime() == null) return 1;
		return -getMessage().getSendedTime().compareTo(mes.getMessage().getSendedTime());
	}
	
}
