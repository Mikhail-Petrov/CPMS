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
import com.cpms.tests.web.pagesmock.ObjectListPage;
import com.cpms.web.PagingUtils;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		TestingConfig.class,
		WebConfig.class, 
		SecurityConfig.class})
@WebAppConfiguration
@WithMockUser
public class TestPagination {

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
	}
	
	@After
	public void destroy() {
	    if (driver != null) {
	        driver.close();
	    }
	    ((ICleanable)profileDAO).cleanAndReset();
	}
	
	@Test
	public void testPaginationLinks() {
		int pageSize = PagingUtils.PAGE_SIZE;
		int thirdPageSize = pageSize > 1 ? pageSize/2 : 1;
		for (int i=0; i<(pageSize * 2 + thirdPageSize); i++) {
			Profile newCompany = new Profile();
			newCompany.setName("C1." + i);
			profileDAO.insert(newCompany);
		}
		
		ObjectListPage page = ObjectListPage.to(driver, "profile");
		assertEquals("Loaded page with correct object ammount", 
				pageSize, page.countObjects());
		assertEquals("Loaded page should be first",
				1, page.getPageNumber());
		
		page = page.browsePageNext();
		assertEquals("Loaded page with correct object ammount", 
				pageSize, page.countObjects());
		assertEquals("Loaded page should be second",
				2, page.getPageNumber());
		
		page = page.browsePageNext();
		assertEquals("Loaded page with correct object ammount", 
				thirdPageSize, page.countObjects());
		assertEquals("Loaded page should be third",
				3, page.getPageNumber());
		
		page = page.browsePagePrevious();
		assertEquals("Loaded page with correct object ammount", 
				pageSize, page.countObjects());
		assertEquals("Loaded page should be second",
				2, page.getPageNumber());
		
		page = page.browsePagePrevious();
		assertEquals("Loaded page with correct object ammount", 
				pageSize, page.countObjects());
		assertEquals("Loaded page should be first",
				1, page.getPageNumber());
	}
	
	@Test
	public void testPaginationOverflow() {
		ObjectListPage page = ObjectListPage.to(driver, "profile");
		assertEquals("Loaded page with correct object ammount", 
				0, page.countObjects());
		assertEquals("Loaded page should be first",
				1, page.getPageNumber());
		
		page = page.browsePageNext();
		assertEquals("Loaded page with correct object ammount", 
				0, page.countObjects());
		assertEquals("Loaded page should be first",
				1, page.getPageNumber());
	}
	
	@Test
	public void testFullTextSearch() {
		Profile concreteCompany2 = new Profile();
		concreteCompany2.setName("Lorem Ipsum");
		profileDAO.insert(concreteCompany2);
		
		int pageSize = PagingUtils.PAGE_SIZE;
		for (int i=0; i< pageSize * 3; i++) {
			Profile newCompany = new Profile();
			newCompany.setName("C1." + i);
			profileDAO.insert(newCompany);
		}
		
		Profile concreteCompany1 = new Profile();
		concreteCompany1.setName("Boyd's Toasts");
		profileDAO.insert(concreteCompany1);
		
		ObjectListPage page = ObjectListPage.to(driver, "profile");
		assertEquals("Loaded page with correct object ammount", 
				pageSize, page.countObjects());
		assertEquals("Loaded page should be first",
				1, page.getPageNumber());
		
		page = page.fullTextSearch("lorem");
		assertEquals("Loaded page with correct object ammount", 
				1, page.countObjects());
		assertEquals("Loaded page should be first",
				1, page.getPageNumber());
		assertEquals("Request should match the string in field",
				"lorem", page.getSearchFieldValue());
		assertTrue("Request should match the found object",
				page.getObjectsValue(0).toLowerCase().contains("lorem"));
		
		page = page.fullTextSearch("C");
		assertEquals("Loaded page with correct object ammount", 
				pageSize, page.countObjects());
		assertEquals("Loaded page should be first",
				1, page.getPageNumber());
		
		page = page.browsePagePrevious();
		assertEquals("Loaded page with correct object ammount", 
				pageSize, page.countObjects());
		assertEquals("Loaded page should be first",
				1, page.getPageNumber());
		
		page = page.browsePageNext();
		assertEquals("Loaded page with correct object ammount", 
				pageSize, page.countObjects());
		assertEquals("Loaded page should be first",
				2, page.getPageNumber());
	}
	
}
