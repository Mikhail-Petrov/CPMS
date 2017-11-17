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
import com.cpms.data.entities.Company;
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
@WithMockUser(roles = {"ADMIN"})
public class TestProfileLifecycle {
	
	private static String SAMPLE_TITLE = "FABLAB";
	private static String SAMPLE_TITLE_RU = "ФАБЛАБ";
	private static String SAMPLE_WEBSITE = "www.fablab.ru";
	private static String SAMPLE_ADDRESS = "Saint-Petersburg, Birzhevaja line 14-16";
	private static String SAMPLE_ADDRESS_RU = "Санкт-Петербург, Биржевая линия 14-16";
	private static String SAMPLE_ABOUT = "ITMO University FABLAB";
	private static String SAMPLE_ABOUT_RU = "Фаблаб университета ИТМО";
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
			Company newCompany = new Company();
			newCompany.setTitle("C1." + i);
			newCompany.setTitle_RU("Ц1." + i);
			profileDAO.insert(newCompany);
		}
	}
	
	@Test
	public void ProfileLifeCycleWorks() {
		Company sampleCompany = new Company();
		sampleCompany.setTitle(SAMPLE_TITLE);
		sampleCompany.setTitle_RU(SAMPLE_TITLE_RU);
		sampleCompany.setWebsite(SAMPLE_WEBSITE);
		sampleCompany.setAddress(SAMPLE_ADDRESS);
		sampleCompany.setAddress_RU(SAMPLE_ADDRESS_RU);
		sampleCompany.setAbout(SAMPLE_ABOUT);
		sampleCompany.setAbout_RU(SAMPLE_ABOUT_RU);
		sampleCompany.setEmail(SAMPLE_EMAIL);
		
		ProfileFormPage page = ProfileFormPage.to(driver);
		page.fillFormFromObject(sampleCompany);
		AbstractPage recievedPage = page.submit();
		assertEquals("Should be a profile page", 
				ProfileFormPage.class, recievedPage.getClass());
		page = ((ProfileFormPage)recievedPage);
		assertFalse("Should have submitted succesfully", page.isCreate());
		
		Company newCompany = page.fillObjectFromForm();
		assertEquals("All fields should be equal",
				sampleCompany.getTitle(), newCompany.getTitle());
		assertEquals("All fields should be equal",
				sampleCompany.getTitle_RU(), newCompany.getTitle_RU());
		assertEquals("All fields should be equal",
				sampleCompany.getWebsite(), newCompany.getWebsite());
		assertEquals("All fields should be equal",
				sampleCompany.getAddress(), newCompany.getAddress());
		assertEquals("All fields should be equal",
				sampleCompany.getAddress_RU(), newCompany.getAddress_RU());
		assertEquals("All fields should be equal",
				sampleCompany.getAbout(), newCompany.getAbout());
		assertEquals("All fields should be equal",
				sampleCompany.getAbout_RU(), newCompany.getAbout_RU());
		assertEquals("All fields should be equal",
				sampleCompany.getEmail(), newCompany.getEmail());
	}

}
