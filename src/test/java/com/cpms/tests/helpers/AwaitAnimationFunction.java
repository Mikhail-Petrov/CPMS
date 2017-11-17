package com.cpms.tests.helpers;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import com.google.common.base.Function;

public class AwaitAnimationFunction implements Function<WebDriver, Boolean> {
	
	private String elementId;
	
	public AwaitAnimationFunction(String elementId) {
		this.elementId = elementId;
	}

	@Override
	public Boolean apply(WebDriver driver) {
		JavascriptExecutor javaScriptExecutor = (JavascriptExecutor)driver;
        String isAnimated = javaScriptExecutor
            .executeScript(
            		String.format("return $('%s').is(':animated')", elementId))
            .toString().toLowerCase();
        return !Boolean.parseBoolean(isAnimated);
	}

}
