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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import com.cpms.data.AbstractDomainObject;

/**
 * Entity class for skill.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Indexed
@Table(name = "Keyword")
public class Keyword extends AbstractDomainObject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "domainid", nullable = true)
	@Cascade({CascadeType.DETACH})
	private Topic domain;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "documentid", nullable = false)
	@Cascade({CascadeType.DELETE})
	private Article doc;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "termid", nullable = false)
	@Cascade({CascadeType.DELETE})
	private Term term;
	
	@Column(name = "count", nullable = false)
	private int count;
	
	public Keyword() {
		count = 0;
	}
	
	public Keyword(Keyword old) {
		setDoc(old.getDoc());
		setDomain(old.getDomain());
		setTerm(old.getTerm());
		setCount(0);
	}

	public int getCount() {
		return count;
	}

	public void setCount(int type) {
		this.count = type;
	}

	public Article getDoc() {
		return doc;
	}

	public void setDoc(Article owner) {
		this.doc = owner;
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
		return Keyword.class;
	}

	@Override
	public String getPresentationName() {
		return getTerm().getPresentationName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Keyword localize(Locale locale) {
		Keyword returnValue = new Keyword();
		returnValue.setId(getId());
		returnValue.setDomain(getDomain());
		returnValue.setDoc(getDoc());
		returnValue.setCount(getCount());
		returnValue.setTerm(getTerm());
		return returnValue;
	}


	public Topic getDomain() {
		return domain;
	}


	public void setDomain(Topic domain) {
		this.domain = domain;
	}


	public Term getTerm() {
		return term;
	}


	public void setTerm(Term term) {
		this.term = term;
	}
	
}
