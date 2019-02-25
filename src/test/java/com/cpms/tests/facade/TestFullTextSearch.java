package com.cpms.tests.facade;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cpms.config.testing.TestingConfig;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.Task;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestingConfig.class})
public class TestFullTextSearch {
	
	//TODO russian full text search test

	private IDAO<Profile> profileDAO;
	private IDAO<Skill> skillDAO;
	private IDAO<Task> taskDAO;
	
	@Autowired
	@Qualifier("profileDAO")
	public void setProdileDAO(IDAO<Profile> profileDAO) {
		this.profileDAO = profileDAO;
	}
	
	@Autowired
	@Qualifier("skillDAO")
	public void setSkillDAO(IDAO<Skill> skillDAO) {
		this.skillDAO = skillDAO;
	}
	
	@Autowired
	@Qualifier("taskDAO")
	public void setTaskDAO(IDAO<Task> taskDAO) {
		this.taskDAO = taskDAO;
	}
	
	@Test
	public void searchesCorrectlyCompanies() {
		((ICleanable)profileDAO).cleanAndReset();
		
		Profile p1 = new Profile("Development", null, "asd", null, null, null);
		Profile p2 = new Profile("Web Development", null, "asd", null, null, null);
		Profile p3 = new Profile("Design", null, "asd", null, null, null);
		
		final Profile p1c = profileDAO.insert(p1);
		final Profile p2c = profileDAO.insert(p2);
		profileDAO.insert(p3);
		
		List<Profile> profiles = profileDAO.search("Development", Profile.class);
		assertEquals("Should have found 2 profiles.", 2, profiles.size());
		assertTrue("Should have found profile 1.", profiles.stream()
				.anyMatch(x -> x.equals(p1c)));
		assertTrue("Should have found profile 2.", profiles.stream()
				.anyMatch(x -> x.equals(p2c)));
		
		profiles = profileDAO.searchRange("development", Profile.class, 1, 2);
		assertEquals("Should have found 1 profile.", 1, profiles.size());
		assertTrue("Should have found profile 2.", profiles.stream()
				.anyMatch(x -> x.equals(p2c)));
		
		((ICleanable)profileDAO).cleanAndReset();
	}
	
	@Test
	public void searchesCorrectlySkill() {
//		((ICleanable)skillDAO).cleanAndReset();
		
		Skill s1 = new Skill("Java Programming", null);
		Skill s2 = new Skill("JavaScript Programming", null);
		Skill s3 = new Skill("Programming", null);
		Skill s4 = new Skill("Design", null);
		
		s1.setMaxLevel(1);
		s2.setMaxLevel(1);
		s3.setMaxLevel(1);
		s4.setMaxLevel(1);
		
		final Skill s3c = skillDAO.insert(s3);
		final Skill s4c = skillDAO.insert(s4);
		final Skill s2c = skillDAO.insert(s2);
		final Skill s1c = skillDAO.insert(s1);
		
		List<Skill> javaSkills = skillDAO.search("java", Skill.class);
		assertEquals("Should have found 2 skills.", 2, javaSkills.size());
		assertTrue("Should have found skill 1.", javaSkills.stream()
				.anyMatch(x -> x.equals(s1c)));
		assertTrue("Should have found skill 2.", javaSkills.stream()
				.anyMatch(x -> x.equals(s2c)));
		
		List<Skill> programmingSkills =
				skillDAO.search("programming", Skill.class);
		assertEquals("Should have found 3 skills.", 3, programmingSkills.size());
		assertTrue("Should have found skill 1.", programmingSkills.stream()
				.anyMatch(x -> x.equals(s1c)));
		assertTrue("Should have found skill 2.", programmingSkills.stream()
				.anyMatch(x -> x.equals(s2c)));
		assertTrue("Should have found skill 3.", programmingSkills.stream()
				.anyMatch(x -> x.equals(s3c)));
		
		List<Skill> designSkills =
				skillDAO.search("design", Skill.class);
		assertEquals("Should have found 1 skills.", 1, designSkills.size());
		assertTrue("Should have found skill 1.", designSkills.stream()
				.anyMatch(x -> x.equals(s4c)));
	}
	
	@Test
	public void searchesCorrectlyTask() {
		((ICleanable)taskDAO).cleanAndReset();
		
		Task t1 = new Task();
		t1.setName("Web application development");
		Task t2 = new Task();
		t2.setName("Web application design");
		
		final Task t1c = taskDAO.insert(t1);
		final Task t2c = taskDAO.insert(t2);
		
		List<Task> tasks = taskDAO.search("development", Task.class);
		assertEquals("Should have found 1 task.", 1, tasks.size());
		assertTrue("Should have found task 1.", tasks.stream()
				.anyMatch(x -> x.equals(t1c)));
		
		tasks = taskDAO.search("design", Task.class);
		assertEquals("Should have found 1 task.", 1, tasks.size());
		assertTrue("Should have found task 2.", tasks.stream()
				.anyMatch(x -> x.equals(t2c)));
		
		tasks = taskDAO.search("web", Task.class);
		assertEquals("Should have found 2 task.", 2, tasks.size());
		assertTrue("Should have found task 1.", tasks.stream()
				.anyMatch(x -> x.equals(t1c)));
		assertTrue("Should have found task 2.", tasks.stream()
				.anyMatch(x -> x.equals(t2c)));
		
		((ICleanable)taskDAO).cleanAndReset();
	}
	
}
