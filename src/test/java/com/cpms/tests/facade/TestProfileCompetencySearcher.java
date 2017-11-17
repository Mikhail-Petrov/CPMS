package com.cpms.tests.facade;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cpms.config.testing.TestingConfig;
import com.cpms.data.entities.Company;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.operations.interfaces.IProfileCompetencySearcher;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestingConfig.class)
public class TestProfileCompetencySearcher {
	
	private IProfileCompetencySearcher searcher;
	
	@Autowired
	@Qualifier("profileCompetencySearcher")
	public void setProfileCompetencySearhcer(IProfileCompetencySearcher searcher) {
		this.searcher = searcher;
	}
	
	@Test
	public void searchesCorrectly() {
		Skill s1 = new Skill("S1", null);
		s1.setMaxLevel(3);
		s1.setId(1);
		Skill s2 = new Skill("S2", null);
		s2.setMaxLevel(3);
		s2.setId(2);
		Skill s3 = new Skill("S3", null);
		s3.setMaxLevel(3);
		s3.setId(3);
		Profile p1 = new Company();
		p1.setId(1);
		Profile p2 = new Company();
		p2.setId(2);
		Profile p3 = new Company();
		p3.setId(3);
		p1.addCompetency(new Competency(s1, 1));
		p1.addCompetency(new Competency(s2, 1));
		p1.addCompetency(new Competency(s3, 1));
		p2.addCompetency(new Competency(s2, 2));
		p2.addCompetency(new Competency(s1, 2));
		p2.addCompetency(new Competency(s3, 2));
		p3.addCompetency(new Competency(s1, 3));
		p3.addCompetency(new Competency(s2, 3));
		p3.addCompetency(new Competency(s3, 3));
		Set<Competency> competencies4 = new LinkedHashSet<Competency>();
		competencies4.add(new Competency(s2, 3));
		competencies4.add(new Competency(s1, 3));
		competencies4.add(new Competency(s3, 2));
		List<Profile> profiles = new ArrayList<Profile>();
		profiles.add(p1);
		profiles.add(p2);
		profiles.add(p3);
		
		List<Profile> res = searcher.searchForProfiles(profiles,
				competencies4, 0.2);
		assertTrue("Should only have 1 profile.", res.size() == 1);
		assertTrue("The only found profile is 3rd", res.get(0).equals(p3));
	}
	
	
}
