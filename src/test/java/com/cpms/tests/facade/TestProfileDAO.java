package com.cpms.tests.facade;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
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
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.Company;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Motivation;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.exceptions.DataAccessException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestingConfig.class})
public class TestProfileDAO {
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	private IDAO<Profile> profileDAO;
	private IDAO<Skill> skillDAO;
	private SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
	
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
	
	private void clear() {
		((ICleanable)profileDAO).cleanAndReset();
		((ICleanable)skillDAO).cleanAndReset();
	}
	
	@Test
	public void canInsertAndThenRetrieveByID() {
		clear();
		
		Skill skill1 = new Skill("Skill#1", null);
		skill1.setMaxLevel(1);
		skill1.setName_RU("Умение 1");
		skillDAO.insert(skill1);
		
		Set<Competency> competencies1 = new LinkedHashSet<Competency>();
		Set<Competency> competencies2 = new LinkedHashSet<Competency>();
		competencies1.add(new Competency(skill1, 1));
		Profile p1 = new Company("С1.1", null, "asd", null, null);
		p1.setCompetencies(competencies1);
		((Company)p1).setTitle_RU("Ц1.1");
		Profile p2 = new Company("С2.2", null, "asd", null, null);
		((Company)p2).setTitle_RU("Ц2.2");
		p2.setCompetencies(competencies2);
		profileDAO.insert(p1);
		profileDAO.insert(p2);
		
		Profile p1u = profileDAO.getOne(p1.getId());
		Profile p2u = profileDAO.getOne(p2.getId());
		assertEquals("Profiles are supposed to be equal after extraction.",
				p1u, p1);
		assertEquals("Profiles are supposed to be equal after extraction.",
				p2u, p2);
		assertTrue("This profile is supposed to have a competency",
				p1u.getCompetencies().size() == 1);
		
		clear();
	}
	
	@Test
	public void canInsertAndThenRetrieveByOrder() {
		clear();
		
		Skill skill1 = new Skill("Skill#1", null);
		skill1.setMaxLevel(1);
		skill1.setName_RU("Умение 1");
		skillDAO.insert(skill1);
		
		Set<Competency> competencies1 = new LinkedHashSet<Competency>();
		Set<Competency> competencies2 = new LinkedHashSet<Competency>();
		competencies1.add(new Competency(skill1, 1));
		Profile p1 = new Company("С1.1", null, "asd", null, null);
		p1.setCompetencies(competencies1);
		((Company)p1).setTitle_RU("Ц1.1");
		Profile p2 = new Company("С2.2", null, "asd", null, null);
		((Company)p2).setTitle_RU("Ц2.2");
		p2.setCompetencies(competencies2);
		profileDAO.insert(p1);
		profileDAO.insert(p2);
		
		List<Profile> profiles = profileDAO.getRange(0, 1);
		assertTrue("Must have only 1 element.", profiles.size() == 1);
		assertTrue("Must be the same that before.", profiles.get(0).equals(p1) || profiles.get(0).equals(p2));
		
		profiles = profileDAO.getRange(1, 2);
		assertTrue("Must have only 1 element.", profiles.size() == 1);
		assertTrue("Must be the same that before.", profiles.get(0).equals(p1) || profiles.get(0).equals(p2));
		
		clear();
	}
		
	@Test
	public void cannotQueryByWrongRange() {
		clear();
		
		exception.expect(DataAccessException.class);
		profileDAO.getRange(2, 2);
		exception.reportMissingExceptionWithMessage("Doesn't throw an exception here");
		exception = ExpectedException.none();
		
		clear();
	}
	
	@Test
	public void canInsertAndThenUpdate() {
		clear();
		
		Skill skill1 = new Skill("Skill#1", null);
		skill1.setName_RU("Умение 1");
		skill1.setMaxLevel(3);
		Skill skill2 = new Skill("Skill#2", null);
		skill2.setName_RU("Умение 2");
		skill2.setMaxLevel(3);
		Skill skill3 = new Skill("Skill#3", null);
		skill3.setName_RU("Умение 3");
		skill3.setMaxLevel(3);
		final Skill skill1c = skillDAO.insert(skill1);
		final Skill skill2c = skillDAO.insert(skill2);
		final Skill skill3c = skillDAO.insert(skill3);
		
		Profile p2 = new Company("Some company that i've made", null, 
				"It's address", null, null);
		((Company)p2).setTitle_RU("Ц2");
		p2 = profileDAO.insert(p2);
		Competency cmp1 = new Competency(skill1c, 1);
		p2.addCompetency(cmp1);
		p2 = profileDAO.update(p2);
		p2.addCompetency(new Competency(skill2c, 1));
		p2 = profileDAO.update(p2);
		
		p2.removeCompetency(p2.getCompetencies().stream()
				.filter(x -> skill2c.equals(x.getSkill()))
				.findFirst().orElse(null));
		p2 = profileDAO.update(p2);
		p2.getCompetencies().stream()
		.filter(x -> x.getSkill().equals(skill1c))
		.findFirst().orElse(null).setLevel(2);
		p2 = profileDAO.update(p2);
		p2.addCompetency(new Competency(skill3c, 1));
		p2 = profileDAO.update(p2);
		
		List<Profile> profiles = profileDAO.getRange(0, 5);
		assertTrue("Should only have one profile", profiles.size() == 1);
		
		assertEquals("The new one has two competencies.", 2, p2.getCompetencies().size());
		assertFalse("Has no competency #2", p2.getCompetencies().stream()
				.anyMatch(x -> skill2c.equals(x.getSkill())));
		assertTrue("The first competency is #1.", p2.getCompetencies().stream()
				.anyMatch(x -> skill1c.equals(x.getSkill())));
		assertTrue("The second competency is #3.", p2.getCompetencies().stream()
				.anyMatch(x -> skill3c.equals(x.getSkill())));
		
		clear();
	}
		
	@Test
	public void canInsertAndThenDelete() {
		clear();
		
		Skill skill2 = new Skill("Skill#2", null);
		skill2.setMaxLevel(3);
		skill2.setName_RU("Умение 2");
		skillDAO.insert(skill2);
		
		Set<Competency> competencies2 = new LinkedHashSet<Competency>();
		Profile p2 = new Company("asd", null, "asd", null, null);
		((Company)p2).setTitle_RU("Ц2");
		p2.setCompetencies(competencies2);
		profileDAO.insert(p2);
		
		profileDAO.delete(p2);
		
		assertTrue("Must not have any profles.",
				profileDAO.getRange(0, 1).size() == 0);
		
		clear();
	}
	
	@Test 
	public void cannotInsertDuplicateCompetencies() {
		clear();
		
		Skill skill = new Skill("Skill#1", null);
		skill.setName_RU("Умение1");
		skill.setMaxLevel(1);
		skillDAO.insert(skill);
		
		Set<Competency> competencies = new LinkedHashSet<Competency>();
		Competency comp = new Competency(skill, 1);
		Competency com2 = new Competency(skill, 1);
		competencies.add(comp);
		competencies.add(com2);
		Profile p = new Company("C1.1", null, "asd", null, null);
		((Company)p).setTitle_RU("Ц1");
		p.setCompetencies(competencies);
		
		exception.expect(DataAccessException.class);
		profileDAO.insert(p);
		exception.reportMissingExceptionWithMessage("Doesn't throw an exception here");
		exception = ExpectedException.none();
		
		clear();
	}
	
	@Test 
	public void cannotUpdateDuplicateCompetencies() {
		clear();
		
		Skill skill = new Skill("Skill#1", null);
		skill.setName_RU("Умение1");
		skill.setMaxLevel(1);
		Skill skill2 = new Skill("Skill#2", null);
		skill2.setName_RU("Умение2");
		skill2.setMaxLevel(1);
		skill = skillDAO.insert(skill);
		final Skill skill2c = skillDAO.insert(skill2);
		
		Set<Competency> competencies = new LinkedHashSet<Competency>();
		Competency comp = new Competency(skill, 1);
		Competency comp2 = new Competency(skill2, 1);
		competencies.add(comp);
		competencies.add(comp2);
		Profile p = new Company("asd", "asd", null, null, null);
		((Company)p).setTitle_RU("Ц1");
		p.setCompetencies(competencies);
		p = profileDAO.insert(p);
		
		p.getCompetencies().stream().filter(x -> x.getSkill().equals(skill2c))
			.findFirst().orElse(null).setSkill(skill);
		
		exception.expect(DataAccessException.class);
		p = profileDAO.update(p);
		exception.reportMissingExceptionWithMessage("Doesn't throw an exception here ");
		exception = ExpectedException.none();
		
		clear();
	}
	
	private Date formatDate(String date) {
		try {
			return dateFormater.parse(date);
		} catch (ParseException pe) {
			throw new RuntimeException(pe);
		}
	}
	
	@Test
	public void canInsertAndThenRetrieve() {
		clear();
		
		Skill skill1 = new Skill("Skill#2", null);
		skill1.setName_RU("Умение2");
		skill1.setMaxLevel(1);
		skillDAO.insert(skill1);
		Set<Competency> competencies1 = new LinkedHashSet<Competency>();
		Competency cmp1 = new Competency(skill1, 1);
		competencies1.add(cmp1);
		Profile p1 = new Company("asd", null, "asd", null, null);
		((Company)p1).setTitle_RU("Ц1");
		p1.setCompetencies(competencies1);
		
		profileDAO.insert(p1);
		
		p1 = profileDAO.getOne(p1.getId());
		
		clear();
	}
	
	@Test
	public void canInsertAndThenDeleteCompetency() {
		clear();
		Skill skill1 = new Skill("Skill#1", null);
		skill1.setName_RU("Умение1");
		skill1.setMaxLevel(1);
		skillDAO.insert(skill1);
		List<Competency> competencies1 = new ArrayList<Competency>();
		competencies1.add(new Competency(skill1, 1));
		Profile p1 = new Company("asd", null, "asd", null, null);
		((Company)p1).setTitle_RU("Ц1");
		p1.addCompetency(competencies1.get(0));
		p1 = profileDAO.insert(p1);
		
		p1.removeCompetency(competencies1.get(0));
		p1 = profileDAO.update(p1);
		
		assertEquals("Must have no competencies.", 0, p1.getCompetencies().size());
		
		clear();
	}
	
}
