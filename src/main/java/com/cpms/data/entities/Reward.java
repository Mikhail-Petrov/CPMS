package com.cpms.data.entities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.search.annotations.Field;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;

import com.cpms.data.AbstractDomainObject;

/**
 * Entity class for rewards.
 * 
 * @author Petrov Mikhail
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "REWARDS")
public class Reward extends AbstractDomainObject implements Comparable<Reward>{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;

	@Column(name = "SENDED", nullable = true)
	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	@Temporal(TemporalType.TIMESTAMP)
	private Date sendedTime;
	
	@Column(name = "Experts", nullable = true, length = 1000)
	private String experts;
	
	@Column(name = "Motivations", nullable = true, length = 1000)
	private String motivations;
	
	public Reward() {
		setExperts("");
		setMotivations("");
		setSendedTime(new Date(System.currentTimeMillis()));
	}
	
	public Reward(String experts, String motivations) {
		setExperts(experts);
		setMotivations(motivations);
		setSendedTime(new Date(System.currentTimeMillis()));
	}
	
	public Reward(Reward source) {
		this(source.getExperts(), source.getMotivations());
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
		return Reward.class;
	}

	@Override
	public String getPresentationName() {
		return new SimpleDateFormat("LLLL y", LocaleContextHolder.getLocale()).format(getSendedTime());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Reward localize(Locale locale) {
		return new Reward( experts, motivations);
	}

	@Override
	public int compareTo(Reward o) {
		return -this.getSendedTime().compareTo(o.getSendedTime());
	}

	public String getExperts() {
		return experts;
	}

	public void setExperts(String experts) {
		this.experts = experts;
	}

	public String getMotivations() {
		return motivations;
	}

	public void setMotivations(String motivations) {
		this.motivations = motivations;
	}

	public Date getSendedTime() {
		return sendedTime;
	}

	public void setSendedTime(Date sendedTime) {
		this.sendedTime = sendedTime;
	}
	
}
