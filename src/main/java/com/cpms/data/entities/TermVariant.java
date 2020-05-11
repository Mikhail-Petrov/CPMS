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
import javax.persistence.UniqueConstraint;
import com.cpms.data.AbstractDomainObject;
import com.cpms.web.UserSessionData;

@Entity
@SuppressWarnings("serial")
@Table(name = "Termvariant", uniqueConstraints =
	@UniqueConstraint(columnNames = {"text", "termid"}, name = "TermVariantUnique"))
public class TermVariant extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "termid", nullable = false)
	private Term term;
	
	@Column(name = "text", nullable = true, length = 1000)
	private String text;
	
	public TermVariant() {}
	
	public TermVariant(String text) {
		this.text = text;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public long getId() {
		return id;
	}

	public Term getTerm() {
		return term;
	}

	public String getText() {
		return text;
	}

	@Override
	public Class<?> getEntityClass() {
		return TermVariant.class;
	}

	@Override
	public String getPresentationName() {
		return text;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TermVariant localize(Locale locale) {
		TermVariant returnValue = new TermVariant();
		returnValue.setId(getId());
		returnValue.setTerm(null);
		returnValue.setText(
				UserSessionData.localizeText(getText()));
		return returnValue;
	}
}
