package com.cpms.tests.facade;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cpms.config.testing.TestingConfig;
import com.cpms.dao.interfaces.*;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.SkillLevel;
import com.cpms.exceptions.DataAccessException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestingConfig.class})
public class TestSkillDAO {
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	private IDAO<Skill> skillDAO;
	
	@Autowired
	@Qualifier("skillDAO")
	public void setSkillDAO(IDAO<Skill> skillDAO) {
		this.skillDAO = skillDAO;
	}
	
	private void clear() {
		((ICleanable)skillDAO).cleanAndReset();
	}

	@Test
	public void canInsertAndThenRetrieve() {
		clear();
		
		Skill skill1 = new Skill("Skill#1", null);
		skill1.setMaxLevel(1);
		skill1 = skillDAO.insert(skill1);
		Skill skill2 = new Skill("Skill#2", null);
		skill2.setMaxLevel(1);
		skill2.setParent(skill1);
		skill2 = skillDAO.insert(skill2);
		Skill skill3 = new Skill("Skill#3", null);
		skill3.setMaxLevel(1);
		skill3.setParent(skill1);
		skill3.setAbout("Description");
		skill3 = skillDAO.insert(skill3);
		
		List<Skill> skills = skillDAO.getAll();
		assertNotNull("Must have retrieved something.", skills);
		assertTrue("Must have 3 elements", skills.size() == 3);
		assertTrue("Ellement one must be Skill#1", skills.get(0).getName().equals("Skill#1"));
		assertTrue("Ellement one must be Skill#2", skills.get(1).getName().equals("Skill#2"));
		assertTrue("Ellement one must be Skill#3", skills.get(2).getName().equals("Skill#3"));
		
		clear();
	}
	
	@Test
	public void canRetrieveNonexistantEntity() {
		clear();
		
		Skill skill = skillDAO.getOne(1);
		assertNull("Should be null.", skill);
		
		clear();
	}
	
	@Test
	public void canInsertAndThenUpdate() {
		clear();
		
		Skill skill1 = new Skill("Skill#1", null);
		skill1.setMaxLevel(1);
		skillDAO.insert(skill1);
		
		skill1.setName("Skill#1.2");
		skillDAO.update(skill1);
		List<Skill> skills = skillDAO.getAll();
		assertTrue("Skill must have changed", skills.get(0).getName().equals("Skill#1.2"));
		
		clear();
	}
	
	@Test
	public void canInsertAndThenDelete() {
		clear();
		
		Skill skill1 = new Skill("Skill#1", null);
		skill1.setMaxLevel(1);
		skillDAO.insert(skill1);
		
		skillDAO.delete(skill1);
		List<Skill> skills = skillDAO.getAll();
		assertNotNull("Must have retrieved something.", skills);
		assertTrue("Must have no elements", skills.size() == 0);
		
		clear();
	}
	
	@Test
	public void canInsertAndThenCascadeDelete() {
		clear();
		
		Skill skill1 = new Skill("Skill#1", null);
		skill1.setMaxLevel(1);
		final Skill skill1c = skillDAO.insert(skill1);
		Skill skill2 = new Skill("Skill#2", null);
		skill2.setMaxLevel(1);
		skill2.setParent(skill1);
		final Skill skill2c = skillDAO.insert(skill2);
		Skill skill3 = new Skill("Skill#3", null);
		skill3.setMaxLevel(1);
		skill3.setParent(skill2c);
		skill3.setAbout("Description");
		final Skill skill3c = skillDAO.insert(skill3);
		
		skillDAO.delete(skill2c);
		List<Skill> skills = skillDAO.getAll();
		assertNotNull("Must have retrieved something.", skills);
		assertEquals("Must have two elements", 2, skills.size());
		assertTrue("Must have skill 2.", skills.stream()
				.anyMatch(x -> x.equals(skill3c)));
		assertTrue("Must have skill 3.", skills.stream()
				.anyMatch(x -> x.equals(skill1c))); 
		
		clear();
	}
	
	@Test
	public void cannotInsertWithWrongForeignKey() {
		clear();
		
		Skill skill1 = new Skill("Skill#1", null);
		skill1.setMaxLevel(1);
		Skill skill2 = new Skill("Skill#2", null);
		skill2.setMaxLevel(1);
		skill2.setParent(skill1);
		
		exception.expect(org.springframework.dao.DataAccessException.class);
		skillDAO.insert(skill2);
		exception.reportMissingExceptionWithMessage("Doesn't throw an exception here.");
		exception = ExpectedException.none();
		
		clear();
	}

	@Test
	public void canInsertAndThenRetrieveSkillLevel() {
		clear();
		
		Skill skill1 = new Skill("Skill#1", null);
		skill1.setMaxLevel(3);
		SkillLevel sl1 = new SkillLevel("1_description");
		sl1.setLevel(1);
		skill1.addLevel(sl1);
		SkillLevel sl2 = new SkillLevel("2_description");
		sl2.setLevel(2);
		skill1.addLevel(sl2);
		SkillLevel sl3 = new SkillLevel("3_description");
		sl3.setLevel(3);
		skill1.addLevel(sl3);
		
		skillDAO.insert(skill1);
		
		Set<SkillLevel> sls = skillDAO.getAll().get(0).getLevels();
		
		assertTrue("Must have 3 levels.", sls.size() == 3);
		assertTrue("Must have level 1.", sls.stream()
				.filter(x -> x.getLevel() == 1)
				.findFirst().orElse(null) != null);
		assertTrue("Must have level 2.", sls.stream()
				.filter(x -> x.getLevel() == 2)
				.findFirst().orElse(null) != null);
		assertTrue("Must have level 3.", sls.stream()
				.filter(x -> x.getLevel() == 3)
				.findFirst().orElse(null) != null);
		
		clear();
	}
	
	@Test
	public void canInsertAndThenUpdateSkillLevel() {
		clear();
		
		Skill s1 = new Skill("Skill#1", null);
		s1.setMaxLevel(3);
		SkillLevel sl1 = new SkillLevel("1_description");
		sl1.setLevel(1);
		s1.addLevel(sl1);
		SkillLevel sl2 = new SkillLevel("2_description");
		sl2.setLevel(3);
		s1.addLevel(sl2);
		
		s1 = skillDAO.insert(s1);
		
		
		s1.removeLevel(s1.getLevels().stream()
				.filter(x -> x.getLevel() == 3)
				.findFirst().orElse(null));
		s1.getLevels().stream()
		.filter(x -> x.getLevel() == 1)
		.findFirst().orElse(null).setLevel(2);
		s1 = skillDAO.update(s1);
		
		Set<SkillLevel> sls = skillDAO.getAll().stream().findFirst()
				.orElse(null).getLevels();
		
		assertTrue("Has a level", sls.size() > 0);
		assertTrue("New level is level 2", sls.stream()
				.filter(x -> x.getLevel() == 2)
				.findFirst().orElse(null) != null 
				&&  sls.stream()
				.filter(x -> x.getLevel() == 2)
				.findFirst().orElse(null).getId() != -1);
		
		clear();
	}
	
	@Test
	public void canInsertAndThenUpdateOnlyDescription() {
		clear();
		
		Skill s1 = new Skill("Skill#1", null);
		s1.setMaxLevel(3);
		SkillLevel sl1 = new SkillLevel("1_description");
		sl1.setLevel(1);
		sl1.setSkill(s1);
		
		skillDAO.insert(s1);
		
		sl1.setAbout("2");
		skillDAO.update(s1);
		
		clear();
	}
	
	@Test
	public void canInsertAndThenDeleteSkillLevel() {
		clear();
		
		Skill skill1 = new Skill("Skill#1", null);
		skill1.setMaxLevel(3);
		SkillLevel sl1 = new SkillLevel("1_description");
		sl1.setLevel(1);
		skill1.addLevel(sl1);
		SkillLevel sl2 = new SkillLevel("2_description");
		sl2.setLevel(2);
		skill1.addLevel(sl2);
		SkillLevel sl3 = new SkillLevel("3_description");
		sl3.setLevel(3);
		skill1.addLevel(sl3);
		
		skill1 = skillDAO.insert(skill1);
		
		skill1.removeLevel(skill1.getLevels().stream()
				.filter(x -> x.getLevel() == 2).findFirst().orElse(null));
		skill1 = skillDAO.update(skill1);
		
		Set<SkillLevel> sls = skill1.getLevels();
		
		assertEquals("Must have 2 levels.", 2, sls.size());
		assertTrue("Must have level 1.", sls.stream()
				.filter(x -> x.getLevel() == 1)
				.findFirst().orElse(null) != null);
		assertTrue("Must have level 2 undefined", sls.stream()
				.filter(x -> x.getLevel() == 2)
				.findFirst().orElse(null) == null);
		assertTrue("Must have level 3.", sls.stream()
				.filter(x -> x.getLevel() == 3)
				.findFirst().orElse(null) != null);
		
		clear();
	}
	
	@Test
	public void cannotInsertDuplicateLevel() {
		clear();
		
		exception.expect(DataAccessException.class);
		
		Skill skill1 = new Skill("Skill#1", null);
		skill1.setMaxLevel(3);
		SkillLevel sl1 = new SkillLevel("1_description");
		sl1.setLevel(1);
		sl1.setId(1);
		skill1.addLevel(sl1);
		SkillLevel sl2 = new SkillLevel("2_description");
		sl2.setLevel(1);
		sl2.setId(2);
		skill1.addLevel(sl1);
		
		skillDAO.insert(skill1);
		exception.reportMissingExceptionWithMessage("Doesn't throw an exception here.");
		exception = ExpectedException.none();
		
		clear();
	}
	
	@Test
	public void cannotInsertLevelLargerThanMaxValue() {
		clear();
		
		exception.expect(DataAccessException.class);
		
		Skill skill1 = new Skill("Skill#1", null);
		skill1.setMaxLevel(3);
		SkillLevel sl1 = new SkillLevel("1_description");
		sl1.setLevel(1);
		skill1.addLevel(sl1);
		SkillLevel sl2 = new SkillLevel("2_description");
		sl2.setLevel(4);
		skill1.addLevel(sl2);
	
		skillDAO.insert(skill1);
		exception.reportMissingExceptionWithMessage("Doesn't throw an exception here.");
		exception = ExpectedException.none();
		
		clear();
	}
	
	@Test
	public void cannotUpdateDuplicateLevel() {
		clear();
		
		exception.expect(DataAccessException.class);
		
		Skill skill1 = new Skill("Skill#1", null);
		skill1.setMaxLevel(3);
		SkillLevel sl1 = new SkillLevel("1_description");
		sl1.setLevel(1);
		skill1.addLevel(sl1);
		SkillLevel sl2 = new SkillLevel("2_description");
		sl2.setLevel(2);
		skill1.addLevel(sl2);
		
		
		skillDAO.insert(skill1);
		sl2.setLevel(1);
		skillDAO.update(skill1);
		exception.reportMissingExceptionWithMessage("Doesn't throw an exception here.");
		exception = ExpectedException.none();
		
		clear();
	}
	
	@Test
	public void cannotUpdateLevelLargerThanMaxValue() {
		clear();
		
		exception.expect(DataAccessException.class);
		
		Skill skill1 = new Skill("Skill#1", null);
		skill1.setMaxLevel(3);
		SkillLevel sl1 = new SkillLevel("1_description");
		sl1.setLevel(1);
		skill1.addLevel(sl1);
		SkillLevel sl2 = new SkillLevel("2_description");
		sl2.setLevel(2);
		skill1.addLevel(sl2);
		
		skillDAO.insert(skill1);
		
		sl2.setLevel(4);
		skillDAO.update(skill1);
		exception.reportMissingExceptionWithMessage("Doesn't throw an exception here.");
		exception = ExpectedException.none();
		
		clear();
	}
	
}
