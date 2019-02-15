package com.cpms.tests.facade;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cpms.config.testing.TestingConfig;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.operations.interfaces.IProfileComparator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestingConfig.class)
public class TestProfileComparator {
	
	private IProfileComparator profileComparator;
	
	@Autowired
	@Qualifier("profileComparator")
	public void setProfileComparator(IProfileComparator profileComparator) {
		this.profileComparator = profileComparator;
	}
	
	@Test
	public void comparesCorrectlyCase1() {
		Skill s1 = new Skill("S1", null);
		s1.setMaxLevel(2);
		s1.setId(1);
		Skill s2 = new Skill("S2", null);
		s2.setMaxLevel(2);
		s2.setId(2);
		Skill s3 = new Skill("S3", null);
		s3.setMaxLevel(2);
		s3.setId(3);
		Profile p1 = new Profile();
		p1.setId(1);
		p1.addCompetency(new Competency(s1, 2));
		p1.addCompetency(new Competency(s2, 2));
		p1.addCompetency(new Competency(s3, 1));
		Profile p2 = new Profile();
		p2.setId(2);
		p2.addCompetency(new Competency(s2, 2));
		p2.addCompetency(new Competency(s1, 2));
		p2.addCompetency(new Competency(s3, 1));
		
		assertEquals("Result of comparing equal competency sets should be 1", 1,
				profileComparator.compareProfiles(p1, p2), 0.001);
	}
	
	@Test
	public void comparesCorrectlyCase2() {
		Skill s1 = new Skill("S1", null);
		s1.setMaxLevel(2);
		s1.setId(1);
		Skill s2 = new Skill("S2", null);
		s2.setMaxLevel(2);
		s2.setId(2);
		Profile p1 = new Profile();
		p1.setId(1);
		p1.addCompetency(new Competency(s1, 2));
		Profile p2 = new Profile();
		p2.setId(2);
		p2.addCompetency(new Competency(s2, 2));
		
		assertEquals("Result of comparing non overlaping competency sets should be 0",
				0, profileComparator.compareProfiles(p1, p2), 0.0000);
	}
	
	@Test
	public void comparesCorrectlyCase3() {
		Skill s1 = new Skill("S1", null);
		s1.setMaxLevel(2);
		s1.setId(1);
		Skill s2 = new Skill("S2", null);
		s2.setMaxLevel(2);
		s2.setId(2);
		Profile p1 = new Profile();
		p1.setId(1);
		p1.addCompetency(new Competency(s1, 2));
		p1.addCompetency(new Competency(s2, 1));
		Profile p2 = new Profile();
		p2.setId(2);
		p2.addCompetency(new Competency(s2, 2));
		p2.addCompetency(new Competency(s1, 1));
		
		assertTrue("Result of comparing overlaping competency sets should be in [0,1]",
				profileComparator.compareProfiles(p1, p2) > 0
				&& profileComparator.compareProfiles(p1, p2) < 1);
	}
}
