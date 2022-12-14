package com.cpms.data.entities;

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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import com.cpms.data.AbstractDomainObject;

/**
 * Entity class for skill level.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Entity
@SuppressWarnings("serial")
@Table(name = "Languages")
public class Language extends AbstractDomainObject implements Comparable<Language> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@Column(name = "Country", nullable = false, length = 1000)
	private String country;
	
	@Column(name = "ISOCode", nullable = false, length = 1000)
	private String iso;
	
	@Column(name = "LangCode", nullable = false, length = 1000)
	private String code;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "local")
	@Cascade({CascadeType.DETACH})
	private Set<Profile> experts;
	
	public Language() {}
	
	public void setId(long id) {
		this.id = id;
	}

	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public Class<?> getEntityClass() {
		return Language.class;
	}

	@Override
	public String getPresentationName() {
		return getCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Language localize(Locale locale) {
		return new Language();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getIso() {
		return iso;
	}

	public void setIso(String iso) {
		this.iso = iso;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Set<Profile> getExperts() {
		return experts;
	}

	public void setExperts(Set<Profile> experts) {
		this.experts = experts;
	}

	@Override
	public int compareTo(Language object) {
		return this.getPresentationName().toLowerCase().compareTo(object.getPresentationName().toLowerCase());
	}
	
	public static Language findByCode(String code, List<Language> langs) {
		for (Language lang : langs)
			if (lang.getCode().equals(code))
				return lang;
		return null;
	}
}
