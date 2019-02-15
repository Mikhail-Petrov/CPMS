package com.cpms.tests.facade;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cpms.config.testing.TestingConfig;
import com.cpms.dao.interfaces.IApplicationsService;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.data.applications.CompetencyApplication;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Motivation;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.facade.ICPMSFacade;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestingConfig.class})
public class TestApplicationsService {
	
	private class TestCaseData {
		public Skill s1, s2, s3;
		public Profile p1, p2;
	}
	
	private ICPMSFacade facade;
	private IApplicationsService applicationsService;
	
	@Autowired
	@Qualifier("facade")
	public void setFacade(ICPMSFacade facade) {
		this.facade = facade;
	}
	
	@Autowired
	@Qualifier("applicationsService")
	public void setApplicationsService(IApplicationsService applicationsService) {
		this.applicationsService = applicationsService;
	}
	
	private void clear() {
		((ICleanable)facade.getProfileDAO()).cleanAndReset();
		((ICleanable)facade.getTaskDAO()).cleanAndReset();
		((ICleanable)facade.getSkillDAO()).cleanAndReset();
		((ICleanable)applicationsService).cleanAndReset();
	}
	
	private TestCaseData prepareTestCase() {
		TestCaseData testCase = new TestCaseData();
		
		Skill skill1 = new Skill("Skill1", null);
		skill1.setMaxLevel(10);
		Skill skill1c = facade.getSkillDAO().insert(skill1);
		Skill skill2 = new Skill("Skill2", null);
		skill2.setMaxLevel(10);
		skill2.setParent(skill1c);
		Skill skill2c = facade.getSkillDAO().insert(skill2);
		Skill skill3 = new Skill("Skill3", null);
		skill3.setMaxLevel(10);
		skill3.setParent(skill2c);
		testCase.s3 = facade.getSkillDAO().insert(skill3);
		testCase.s1 = skill1c;
		testCase.s2 = skill2c;
		
		Profile profile1 = new Profile("C1.1", null, "Some address", null, null);
		Competency cmp1 = new Competency(skill1c, 5);
		profile1.addCompetency(cmp1);
		testCase.p1 = facade.getProfileDAO().insert(profile1);
		Profile profile2 = new Profile("C2.1", null, "Some address", null, null);
		Competency cmp2 = new Competency(skill2c, 5);
		profile2.addCompetency(cmp2);
		testCase.p2 = facade.getProfileDAO().insert(profile2);
		
		return testCase;
	}
	
	@Test
	public void canSubmitAndThenDelete() {
		clear();
		TestCaseData testCase = prepareTestCase();
		
		CompetencyApplication competencyApplication = new CompetencyApplication(
				testCase.s2.getId(), testCase.p1.getId(), 9);
		applicationsService.suggestCompetency(competencyApplication);
		
		List<CompetencyApplication> applicationsC = applicationsService.retrieveSuggestedCompetencies();
		
		assertEquals("Should have found one competency application.", 1, applicationsC.size());
		
		CompetencyApplication competencyApplication2 = new CompetencyApplication(
				testCase.s3.getId(), testCase.p1.getId(), 9);
		applicationsService.suggestCompetency(competencyApplication2);
		
		applicationsService.deleteSuggestedCompetency(applicationsC.get(0).getId());
		
		applicationsC = applicationsService.retrieveSuggestedCompetencies();
		
		assertEquals("Should have found one competency application.", 1, applicationsC.size());
		
		clear();
	}
	
	@Test
	public void canSubmitAndThenApprove() {
		clear();
		prepareTestCase();
		
		clear();
	}

}
