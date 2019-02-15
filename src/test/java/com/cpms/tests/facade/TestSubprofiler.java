package com.cpms.tests.facade;

import static org.junit.Assert.*;

import java.util.HashSet;
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
import com.cpms.operations.interfaces.ISubprofiler;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestingConfig.class)
public class TestSubprofiler {
	
	private ISubprofiler subprofiler;
	
	@Autowired
	@Qualifier("subprofiler")
	public void setTestable(ISubprofiler subprofiler) {
		this.subprofiler = subprofiler;
	}
	
	@Test
	public void suprofilesCorrectly() {
		Skill s1 = new Skill("S1", null);
		s1.setMaxLevel(3);
		s1.setId(1);
		Skill s2 = new Skill("S2", null);
		s2.setMaxLevel(3);
		s2.setId(2);
		Skill s3 = new Skill("S3", null);
		s3.setMaxLevel(3);
		s3.setId(3);
		Profile p1 = new Profile();
		p1.addCompetency(new Competency(s1, 1));
		p1.addCompetency(new Competency(s2, 1));
		Set<Skill> skills = new HashSet<Skill>();
		skills.add(s2);
		skills.add(s3);
		
		Profile p2 = subprofiler.subprofile(p1, skills);
		
		assertFalse("Must not have competency 1", p2.getCompetencies().stream()
				.anyMatch(x -> x.getSkill().getId() == 1));
		assertTrue("Must have competency 2", p2.getCompetencies().stream()
				.anyMatch(x -> x.getSkill().getName().equals("S2")));
		assertTrue("Must have competency 3", p2.getCompetencies().stream()
				.anyMatch(x -> x.getSkill().getName().equals("S3")));
		assertTrue("Competency 2 level must be 1", p2.getCompetencies().stream()
				.anyMatch(x -> x.getSkill().getName().equals("S2") && x.getLevel() == 1));
		assertTrue("Competency 3 level must be 0", p2.getCompetencies().stream()
				.anyMatch(x -> x.getSkill().getName().equals("S3") && x.getLevel() == 0));
		assertTrue("Competency 3 id must be 0", p2.getCompetencies().stream()
				.anyMatch(x -> x.getSkill().getName().equals("S3") && x.getId() == 0));
	}
	
}