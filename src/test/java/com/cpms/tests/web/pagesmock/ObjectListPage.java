package com.cpms.tests.web.pagesmock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ObjectListPage extends AbstractPage {
	
	private WebElement pagePrevious;
	private WebElement pageNext;
	
	@FindBy(css = "input[type=\"text\"]")
	private WebElement searchField;

	public ObjectListPage(WebDriver driver) {
		super(driver);
	}
	
	public int countObjects() {
		return driver.findElements(By.id("obj")).size();
	}
	
	public ObjectListPage browsePageNext() {
		pageNext.click();
		return PageFactory.initElements(driver, ObjectListPage.class);
	}
	
	public ObjectListPage browsePagePrevious() {
		pagePrevious.click();
		return PageFactory.initElements(driver, ObjectListPage.class);
	}
	
	public ObjectListPage fullTextSearch(String request) {
		searchField.clear();
		searchField.sendKeys(request);
		searchField.submit();
		return PageFactory.initElements(driver, ObjectListPage.class);
	}
	
	public int getPageNumber() {
		Pattern pattern = Pattern.compile(".*page\\=([0-9]+).*");
		Matcher matcher = pattern.matcher(driver.getCurrentUrl());
		String pageNumber = null;
		if (matcher.matches() && matcher.groupCount() > 0) {
			pageNumber = matcher.group(1);
		}
		if (pageNumber != null && pageNumber != "") {
			return Integer.parseInt(pageNumber);
		} else {
			return 1;
		}
	}

	public String getSearchFieldValue() {
		return searchField.getAttribute("value");
	}
	
	public String getObjectsValue(int objectsIndex) {
		return driver.findElements(By.id("obj"))
				.get(objectsIndex)
				.findElement(By.xpath("h3/a/span"))
				.getText();
	}
	
	public static ObjectListPage to(WebDriver driver, String objectName) {
        get(driver, "viewer/" + objectName.toLowerCase() + "s");
        return PageFactory.initElements(driver, ObjectListPage.class);
    }

}
