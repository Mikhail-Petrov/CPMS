package com.cpms.tests.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.htmlunit.webdriver.MockMvcHtmlUnitDriverBuilder;
import org.springframework.web.context.WebApplicationContext;

import com.cpms.config.security.SecurityConfig;
import com.cpms.config.testing.TestingConfig;
import com.cpms.config.web.WebConfig;
import com.cpms.tests.web.pagesmock.LandingPage;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		TestingConfig.class,
		WebConfig.class, 
		SecurityConfig.class})
@WebAppConfiguration
@WithMockUser
public class TestLanding {
	
	@Autowired
	WebApplicationContext context;
	
	WebDriver driver;

	@Before
	public void setup() {
	    driver = MockMvcHtmlUnitDriverBuilder
	        .webAppContextSetup(context, springSecurity())
	        .build();
	    driver.manage().timeouts().pageLoadTimeout(1, TimeUnit.SECONDS);
	}
	
	@After
	public void destroy() {
	    if (driver != null) {
	        driver.close();
	    }
	}
	
	@Test
	public void tryToLand() {
		LandingPage page = LandingPage.to(driver);
		assertTrue("Should have landed successfully.", page.connected());
	}
	
	@Test
	public void navbarWorks() {
		LandingPage page = LandingPage.to(driver);
		assertTrue("Navbar should function.", page.navbarWorks());
	}

}
