package com.cpms.tests.web.pagesmock;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import com.cpms.data.entities.Profile;

public class ProfileFormPage extends AbstractPage {
	
	private WebElement id;
	private WebElement title;
	private WebElement website;
	private WebElement about;
	
	private boolean create;
	
	public ProfileFormPage(WebDriver driver) {
    	super(driver);
	}
	
	public void fillFormFromObject(Profile company) {
		title.clear();
		title.sendKeys(company.getName());
		website.clear();
		website.sendKeys(company.getPosition());
		about.clear();
		about.sendKeys(company.getAbout());
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
	
	public Profile fillObjectFromForm() {
		Profile company = new Profile();
		company.setId(Long.parseLong(id.getAttribute("value")));
		company.setName(title.getAttribute("value"));
		company.setPosition(website.getAttribute("value"));
		company.setAbout(about.getAttribute("value"));
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
