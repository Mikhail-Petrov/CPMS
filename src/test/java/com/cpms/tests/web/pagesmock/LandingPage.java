package com.cpms.tests.web.pagesmock;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

public class LandingPage extends AbstractPage {

	private WebElement jumbotron;
	
	public LandingPage(WebDriver driver) {
    	super(driver);
	}
    
    public boolean connected() {
    	return jumbotron != null && jumbotron.isDisplayed();
    }

    public static LandingPage to(WebDriver driver) {
        get(driver, "");
        return PageFactory.initElements(driver, LandingPage.class);
    }

}
