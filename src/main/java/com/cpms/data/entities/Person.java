package com.cpms.data.entities;

import java.util.Locale;

import javax.persistence.Entity;
import javax.validation.constraints.Size;

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import com.cpms.data.DomainObject;

/**
 * Entity class for person competency profiles. Created for demonstrating
 * purposes, not actually used. Supported on DAO level.
 * 
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Entity
@Indexed
@SuppressWarnings("serial")
public final class Person extends Profile {
	
	@Size(max = 100)
	@NotEmpty
	@Field
	private String name;
	
	@Size(max = 100)
	@NotEmpty
	@Field
	private String name_RU;
	
	@Size(max = 100)
	@NotEmpty
	@Field
	private String familyname;
	
	@Size(max = 100)
	@NotEmpty
	@Field
	private String familyname_RU;

	@Size(max = 100)
	private String company;
	
	@Size(max = 100)
	private String company_RU;
	
	@Size(max = 100)
	@Email
	private String email;
	
	@Size(max = 1000)
	private String about;
	
	@Size(max = 1000)
	private String about_RU;
	
	public Person(String name, String familyname, String company,
			String email, String avatarFilepath, String about) {
		this.name = name;
		this.familyname = familyname;
		this.company = company;
		this.email = email;
		this.about = about;
	}
	
	public Person(){}

	public String getName_RU() {
		return name_RU;
	}

	public void setName_RU(String name_RU) {
		this.name_RU = name_RU;
	}

	public String getFamilyname_RU() {
		return familyname_RU;
	}

	public void setFamilyname_RU(String familyname_RU) {
		this.familyname_RU = familyname_RU;
	}

	public String getCompany_RU() {
		return company_RU;
	}

	public void setCompany_RU(String company_RU) {
		this.company_RU = company_RU;
	}

	public String getAbout_RU() {
		return about_RU;
	}

	public void setAbout_RU(String about_RU) {
		this.about_RU = about_RU;
	}

	@Override
	public String getPresentationName() {
		return name + " " + familyname;
	}

	public String getFamilyname() {
		return familyname;
	}

	public String getCompany() {
		return company;
	}

	public String getEmail() {
		return email;
	}

	public String getAbout() {
		return about;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFamilyname(String familyname) {
		this.familyname = familyname;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	@Override
	public Profile clone() {
		Person clone = new Person();
		clone.setAbout(about);
		clone.setCompany(company);
		clone.setEmail(email);
		clone.setFamilyname(familyname);
		clone.setName(familyname);
		clone.setId(super.getId());
		return clone;
	}

	@Override
	public void update(Profile source) {
		Person sourcePerson = (Person)source;
		setAbout(sourcePerson.getAbout());
		setCompany(sourcePerson.getCompany());
		setEmail(sourcePerson.getEmail());
		setFamilyname(sourcePerson.getFamilyname());
		setName(sourcePerson.getName());
	}

	@Override
	public <T extends DomainObject> T localize(Locale locale) {
		throw new NotImplementedException();
	}
}
