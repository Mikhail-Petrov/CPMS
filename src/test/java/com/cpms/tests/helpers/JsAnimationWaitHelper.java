package com.cpms.tests.helpers;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;

public class JsAnimationWaitHelper {
 
    private WebDriver driver;
 
    public JsAnimationWaitHelper(WebDriver driver) {
        this.driver = driver;
    }
 
    public void until(Function<WebDriver, Boolean> waitCondition, long timeoutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, timeoutInSeconds);
        wait.until(waitCondition);
    }
 
    public void UntilAnimationIsDone(String elementId, long timeoutInSeconds) {
        this.until(new AwaitAnimationFunction(elementId), timeoutInSeconds);
    }
}
