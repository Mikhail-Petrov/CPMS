package com.cpms.tests.web.pagesmock;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import com.cpms.data.entities.Company;

public class ProfileFormPage extends AbstractPage {
	
	private WebElement id;
	private WebElement title;
	private WebElement title_RU;
	private WebElement website;
	private WebElement address;
	private WebElement address_RU;
	private WebElement about;
	private WebElement about_RU;
	private WebElement email;
	
	private boolean create;
	
	public ProfileFormPage(WebDriver driver) {
    	super(driver);
	}
	
	public void fillFormFromObject(Company company) {
		title.clear();
		title.sendKeys(company.getTitle());
		title_RU.clear();
		title_RU.sendKeys(company.getTitle_RU());
		website.clear();
		website.sendKeys(company.getWebsite());
		address.clear();
		address.sendKeys(company.getAddress());
		address_RU.clear();
		address_RU.sendKeys(company.getAddress_RU());
		about.clear();
		about.sendKeys(company.getAbout());
		about_RU.clear();
		about_RU.sendKeys(company.getAbout_RU());
		email.clear();
		email.sendKeys(company.getEmail());
	}
	
	public AbstractPage submit() {
		title.submit();
		if (driver.getCurrentUrl().contains("editor/profile")
				|| driver.getCurrentUrl().contains("viewer/profile")) {
			ProfileFormPage profilePage =
					PageFactory.initElements(driver, ProfileFormPage.class);
			profilePage.setCreate(driver.getCurrentUrl().contains("editor/profile"));
			return profilePage;
		} else {
			return PageFactory.initElements(driver, ErrorPage.class);
		}
	}
	
	public Company fillObjectFromForm() {
		Company company = new Company();
		company.setId(Long.parseLong(id.getAttribute("value")));
		company.setTitle(title.getAttribute("value"));
		company.setTitle_RU(title_RU.getAttribute("value"));
		company.setWebsite(website.getAttribute("value"));
		company.setAddress(address.getAttribute("value"));
		company.setAddress_RU(address_RU.getAttribute("value"));
		company.setAbout(about.getAttribute("value"));
		company.setAbout_RU(about_RU.getAttribute("value"));
		company.setEmail(email.getAttribute("value"));
		return company;
	}

    public boolean isCreate() {
		return create;
	}

	public void setCreate(boolean create) {
		this.create = create;
	}

	public static ProfileFormPage to(WebDriver driver) {
		get(driver, "editor/profile");
		ProfileFormPage page = 
				PageFactory.initElements(driver, ProfileFormPage.class);
		page.setCreate(false);
		return page;
    }

}
