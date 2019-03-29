package com.cpms.data.entities;

import java.util.ArrayList;
import java.util.Date;
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.solr.analysis.LowerCaseFilterFactory;
import org.apache.solr.analysis.WhitespaceTokenizerFactory;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.springframework.format.annotation.DateTimeFormat;

import com.cpms.data.AbstractDomainObject;
import com.cpms.exceptions.DataAccessException;
import com.cpms.security.entities.User;
import com.cpms.web.UserSessionData;

/**
 * Entity class for skill.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Indexed
@Table(name = "MESSAGE")
public class Message extends AbstractDomainObject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@Column(name = "TITLE", nullable = true)
	@Field
	private String title;
	
	@Column(name = "TEXT", nullable = true, length = 1000)
	private String text;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "PARENT", nullable = true)
	@Cascade({CascadeType.DETACH})
	private Message parent;

	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "OWNER", nullable = true)
	@Cascade({CascadeType.DETACH})
	private User owner;
	
	@Column(name = "TYPE", nullable = true, length = 100)
	private String type;
	
	//TODO see if you can fetch this lazily with hibernate
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "parent")
	@Cascade({CascadeType.DETACH})
	private Set<Message> children;
	
	@OneToMany(fetch = FetchType.EAGER,	mappedBy = "message", orphanRemoval = true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE,
        CascadeType.MERGE, CascadeType.PERSIST})
	private Set<MessageCenter> recipients;

	@Column(name = "SENDED", nullable = true)
	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	@Temporal(TemporalType.TIMESTAMP)
	private Date sendedTime;
	
	public Message() {
		setSendedTime(new Date(System.currentTimeMillis()));
		setType("1");
	}
	

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Set<Message> getChildren() {
		if (children == null) {
			children = new LinkedHashSet<Message>() ;
		}
		return new LinkedHashSet<Message>(children);
	}
	
	public List<Message> getChildrenSorted() {
		List<Message> result = new ArrayList<Message>();
		for (Message message : getChildren())
			result.add(message);
		return result;
	}

	public void setChildren(Set<Message> children) {
		this.children = children;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setParent(Message parent) {
		if (this.parent != null && this.parent.children != null) {
			this.parent.children.remove(this);
		}
		this.parent = parent;
		if (this.parent != null && this.parent.getChildren() != null 
				&& !this.parent.children.contains(this)) {
			this.parent.children.add(this);
		}
	}
	
	public void detachChildren() {
		if (children != null) {
			getChildren().forEach(x -> x.setParent(null));
			children.clear();
		}
	}

	@Override
	public long getId() {
		return id;
	}

	public Message getParent() {
		return parent;
	}

	@Override
	public Class<?> getEntityClass() {
		return Message.class;
	}

	@Override
	public String getPresentationName() {
		return UserSessionData.localizeText(getTitle());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Message localize(Locale locale) {
		Message returnValue = new Message();
		returnValue.setId(getId());
		returnValue.setChildren(getChildren());
		returnValue.setParent(getParent());
		returnValue.setText(getText());
		returnValue.setTitle(getTitle());
		returnValue.setOwner(getOwner());
		returnValue.setType(getType());
		returnValue.setSendedTime(getSendedTime());
		return returnValue;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}

	public void removeRecipient(MessageCenter recipient) {
		if (recipient == null) {
			throw new DataAccessException("Null value.", null);
		}
		if(this.equals(recipient.getMessage())) {
			removeEntityFromManagedCollection(recipient, recipients);
			recipient.setMessage(null);
		}
	}
	
	public void removeRecepient(User user) {
		for (MessageCenter recepient : getRecipients())
			if (recepient.getUser().equals(user))
				removeRecipient(recepient);
	}

	public void addRecipient(MessageCenter recipient) {
		if (recipient == null || recipient.getUser() == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (recipients == null) {
			getRecipients();
		}
		recipients.add(recipient);
		recipient.setMessage(this);
	}

	public void setRecipients(Set<MessageCenter> competencies) {
		if (this.recipients != null) {
			throw new DataAccessException("Cannot insert, Hibernate will lose track!");
		}
		this.recipients = competencies;
		this.recipients.forEach(x -> x.setMessage(this));
	}

	public Set<MessageCenter> getRecipients() {
		if (recipients == null) {
			recipients = new LinkedHashSet<MessageCenter>();
		}
		return new LinkedHashSet<MessageCenter>(recipients);
	}


	public Date getSendedTime() {
		return sendedTime;
	}


	public void setSendedTime(Date sendedTime) {
		this.sendedTime = sendedTime;
	}
	
	
}
