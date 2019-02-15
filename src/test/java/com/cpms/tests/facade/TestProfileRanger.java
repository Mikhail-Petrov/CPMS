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
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.operations.interfaces.IProfileRanger;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestingConfig.class)
public class TestProfileRanger {
	
	private IProfileRanger ranger;
	
	@Autowired
	@Qualifier("profileRanger")
	public void setRanger(IProfileRanger ranger) {
		this.ranger = ranger;
	}
	
	@Test
	public void rangesCorrectly() {
		Skill s1 = new Skill("S1", null);
		s1.setMaxLevel(3);
		s1.setId(1);
		Skill s2 = new Skill("S2", null);
		s2.setMaxLevel(3);
		s2.setId(2);
		Skill s3 = new Skill("S3", null);
		s3.setMaxLevel(3);
		s3.setId(3);
		Profile p1 = new Profile("C1", null, "asd", null, null);
		p1.setId(1);
		Profile p2 = new Profile("C2", null, "asd", null, null);
		p2.setId(2);
		Profile p3 = new Profile("C3", null, "asd", null, null);
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
		profiles.add(p2);
		profiles.add(p3);
		profiles.add(p1);
		
		List<Profile> res = ranger.rangeProfiles(profiles,
				competencies4,
				true);
		
		assertTrue("Profile 3 should be first.", res.get(0).getPresentationName()
				.equals(p3.getPresentationName()));
		assertTrue("Profile 2 should be second.", res.get(1).getPresentationName()
				.equals(p2.getPresentationName()));
		assertTrue("Profile 1 should be third.", res.get(2).getPresentationName()
				.equals(p1.getPresentationName()));
		
		res = ranger.rangeProfiles(profiles,
				competencies4,
				false);
		
		assertTrue("Profile 3 should be first.", res.get(0).getPresentationName()
				.equals(p1.getPresentationName()));
		assertTrue("Profile 2 should be second.", res.get(1).getPresentationName()
				.equals(p2.getPresentationName()));
		assertTrue("Profile 1 should be third.", res.get(2).getPresentationName()
				.equals(p3.getPresentationName()));
	}
	
}
