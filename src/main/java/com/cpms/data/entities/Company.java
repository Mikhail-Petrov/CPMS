package com.cpms.data.entities;

import java.util.Locale;

import javax.persistence.Entity;
import javax.validation.constraints.Size;

import org.apache.solr.analysis.LowerCaseFilterFactory;
import org.apache.solr.analysis.WhitespaceTokenizerFactory;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.hibernate.validator.constraints.Email;

import com.cpms.data.validation.BilingualValidation;

/**
 * Entity class for company competency profiles.
 * 
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Entity
@Indexed
@SuppressWarnings("serial")
@BilingualValidation(fieldOne="title", fieldTwo="title_RU", 
	nullable = true, minlength = 3, maxlength = 100)
@BilingualValidation(fieldOne="address", fieldTwo="address_RU", 
	nullable = true, minlength = 0, maxlength = 200)
@BilingualValidation(fieldOne="about", fieldTwo="about_RU", 
	nullable = true, minlength = 0, maxlength = 1000)
@AnalyzerDef(name = "userSearchAnalyzerCompany",
	tokenizer = @TokenizerDef(factory = WhitespaceTokenizerFactory.class),
	filters = {
		@TokenFilterDef(factory = LowerCaseFilterFactory.class)
	})
public final class Company extends Profile {
	
	@Field
	@Analyzer(definition = "userSearchAnalyzerCompany")
	private String title;
	
	@Field
	@Analyzer(definition = "userSearchAnalyzerCompany")
	private String title_RU;
	
	@Size(max = 100)
	private String website;
	
	private String address;
	
	private String address_RU;
	
	@Size(max = 100)
	@Email
	private String email;
	
	private String about;
	
	private String about_RU;
	
	public Company(String title, String website, String address,
			String email, String about) {
		this.title = title;
		this.website = website;
		this.address = address;
		this.about = about;
		this.email = email;
	}
	
	public Company() {}
	
	public String getTitle_RU() {
		return title_RU;
	}

	public void setTitle_RU(String title_RU) {
		this.title_RU = title_RU;
	}

	public String getAddress_RU() {
		return address_RU;
	}

	public void setAddress_RU(String address_RU) {
		this.address_RU = address_RU;
	}

	public String getAbout_RU() {
		return about_RU;
	}

	public void setAbout_RU(String about_RU) {
		this.about_RU = about_RU;
	}

	public String getEmail() {
		return email;
	}

	public String getTitle() {
		return title;
	}

	public String getWebsite() {
		return website;
	}

	public String getAddress() {
		return address;
	}

	public String getAbout() {
		return about;
	}

	@Override
	public String getPresentationName() {
		if (title == null || title.isEmpty()) {
			return title_RU;
		}
		if (title_RU == null || title_RU.isEmpty()) {
			return title;
		}
		return title + " (" + title_RU + ")";
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	@Override
	public Profile clone() {
		Company clone = new Company();
		clone.setAbout(about);
		clone.setAddress(address);
		clone.setEmail(email);
		clone.setTitle(title);
		clone.setWebsite(website);
		clone.setId(super.getId());
		clone.setTitle_RU(title_RU);
		clone.setAddress_RU(address_RU);
		clone.setAbout_RU(about_RU);
		return clone;
	}

	@Override
	public void update(Profile source) {
		Company sourceCompany = (Company)source;
		setAbout(sourceCompany.getAbout());
		setAddress(sourceCompany.getAddress());
		setEmail(sourceCompany.getEmail());
		setTitle(sourceCompany.getTitle());
		setWebsite(sourceCompany.getWebsite());
		setTitle_RU(sourceCompany.getTitle_RU());
		setAddress_RU(sourceCompany.getAddress_RU());
		setAbout_RU(sourceCompany.getAbout_RU());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Company localize(Locale locale) {
		Company returnValue = (Company)this.clone();
		returnValue.setAddress(
				localizeBilingualField(getAddress(), getAddress_RU(), locale));
		returnValue.setAbout(
				localizeBilingualField(getAbout(), getAbout_RU(), locale));
		getCompetencies()
			.forEach(x -> returnValue.addCompetency(x.localize(locale)));
		return returnValue;
	}

}
