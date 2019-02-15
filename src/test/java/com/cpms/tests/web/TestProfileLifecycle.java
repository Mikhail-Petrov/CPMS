package com.cpms.tests.web;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.htmlunit.webdriver.MockMvcHtmlUnitDriverBuilder;
import org.springframework.web.context.WebApplicationContext;

import com.cpms.config.security.SecurityConfig;
import com.cpms.config.testing.TestingConfig;
import com.cpms.config.web.WebConfig;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.Profile;
import com.cpms.tests.web.pagesmock.AbstractPage;
import com.cpms.tests.web.pagesmock.ProfileFormPage;
import com.cpms.web.PagingUtils;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		TestingConfig.class,
		WebConfig.class, 
		SecurityConfig.class})
@WebAppConfiguration
@WithMockUser(roles = {"MANAGER"})
public class TestProfileLifecycle {
	
	private static String SAMPLE_TITLE = "FABLAB";
	private static String SAMPLE_WEBSITE = "www.fablab.ru";
	private static String SAMPLE_ADDRESS = "Saint-Petersburg, Birzhevaja line 14-16";
	private static String SAMPLE_ABOUT = "ITMO University FABLAB";
	private static String SAMPLE_EMAIL = "fablab@gmail.com";
	
	@Autowired
	WebApplicationContext context;
	
	private IDAO<Profile> profileDAO;
	
	@Autowired
	@Qualifier("profileDAO")
	public void setProdileDAO(IDAO<Profile> profileDAO) {
		this.profileDAO = profileDAO;
	}
	
	WebDriver driver;

	@Before
	public void setup() {
	    driver = MockMvcHtmlUnitDriverBuilder
	        .webAppContextSetup(context, springSecurity())
	        .build();
	    driver.manage().timeouts().pageLoadTimeout(1, TimeUnit.SECONDS);
	    ((ICleanable)profileDAO).cleanAndReset();
	    insertSampleData();
	}
	
	@After
	public void destroy() {
	    if (driver != null) {
	        driver.close();
	    }
	    ((ICleanable)profileDAO).cleanAndReset();
	}
	
	public void insertSampleData() {
		int pageSize = PagingUtils.PAGE_SIZE;
		for (int i=0; i<(pageSize * 3); i++) {
			Profile newCompany = new Profile();
			newCompany.setName("C1." + i);
			profileDAO.insert(newCompany);
		}
	}
	
	@Test
	public void ProfileLifeCycleWorks() {
		Profile sampleCompany = new Profile();
		sampleCompany.setName(SAMPLE_TITLE);
		sampleCompany.setPosition(SAMPLE_WEBSITE);
		sampleCompany.setAbout(SAMPLE_ABOUT);
		
		ProfileFormPage page = ProfileFormPage.to(driver);
		page.fillFormFromObject(sampleCompany);
		AbstractPage recievedPage = page.submit();
		assertEquals("Should be a profile page", 
				ProfileFormPage.class, recievedPage.getClass());
		page = ((ProfileFormPage)recievedPage);
		assertFalse("Should have submitted succesfully", page.isCreate());
		
		Profile newCompany = page.fillObjectFromForm();
		assertEquals("All fields should be equal",
				sampleCompany.getName(), newCompany.getName());
		assertEquals("All fields should be equal",
				sampleCompany.getPosition(), newCompany.getPosition());
		assertEquals("All fields should be equal",
				sampleCompany.getAbout(), newCompany.getAbout());
	}

}
