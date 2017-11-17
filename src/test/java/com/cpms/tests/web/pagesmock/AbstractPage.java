package com.cpms.tests.web.pagesmock;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cpms.tests.helpers.JsAnimationWaitHelper;

public abstract class AbstractPage {
	
	private static String baseUrl;
	
	protected WebDriver driver;
	
	@FindBy(css = "label.error, .alert-error")
	private WebElement errors;
	
	private WebElement navbarButton;
	private WebElement navbarHome;
	private WebElement navbarBack;
	private WebElement navbarUser;
	private WebElement navbarDashboard;
	private WebElement navbarSearch;
	
	private JsAnimationWaitHelper helper;
	
	public static void setBaseUrl(String newBaseUrl) {
		baseUrl = newBaseUrl;
	}
	
	static void get(WebDriver driver, String relativeUrl) {
        driver.get(baseUrl + relativeUrl);
    }

	public AbstractPage(WebDriver driver) {
		setDriver(driver);
		helper = new JsAnimationWaitHelper(driver);
	}

	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}

	public String getErrors() {
		return errors.getText();
	}
	
	public boolean navbarToggled() {
		return navbarButton.isDisplayed()
				&& navbarBack.isDisplayed()
				&& navbarDashboard.isDisplayed()
				&& navbarHome.isDisplayed()
				&& navbarSearch.isDisplayed()
				&& navbarUser.isDisplayed();
	}
	
	public boolean navbarWorks() {
		if (navbarToggled()) {
			navbarButton.click();
			helper.UntilAnimationIsDone("navbarCollapse", 2);
		}
		boolean navbarWasHidden = !navbarToggled();
		navbarButton.click();
		helper.UntilAnimationIsDone("navbarCollapse", 2);
		boolean navbarWasShown = navbarToggled();
		return navbarWasHidden && navbarWasShown;
	}

}
