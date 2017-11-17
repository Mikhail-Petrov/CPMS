package com.cpms.tests.facade;

import static org.junit.Assert.*;

import java.util.List;

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
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TaskRequirement;
import com.cpms.exceptions.DataAccessException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestingConfig.class})
public class TestTaskDAO {
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	private IDAO<Task> taskDAO;
	private IDAO<Skill> skillDAO;
	
	@Autowired
	@Qualifier("taskDAO")
	public void setTaskDAO(IDAO<Task> taskDAO) {
		this.taskDAO = taskDAO;
	}
	
	@Autowired
	@Qualifier("skillDAO")
	public void setSkillDAO(IDAO<Skill> skillDAO) {
		this.skillDAO = skillDAO;
	}
	
	private void clear() {
		((ICleanable)taskDAO).cleanAndReset();
		((ICleanable)skillDAO).cleanAndReset();
	}
	
	@Test
	public void canInsertAndThenRetrieve() {
		clear();
		
		Skill skill1 = new Skill("Skill#1", null);
		skill1.setMaxLevel(1);
		skill1.setName_RU("Умение 1");
		skillDAO.insert(skill1);
		Task t1 = new Task("Task 1", null);
		t1.setName_RU("Task 1");
		Task t2 = new Task("Task 2", "Info");
		t2.setName_RU("Task 2");
		TaskRequirement r1 = new TaskRequirement(skill1, 1);
		t2.addRequirement(r1);
		
		t1 = taskDAO.insert(t1);
		t2 = taskDAO.insert(t2);
		
		List<Task> tasks = taskDAO.getAll();
		
		assertEquals("Must have 2 tasks", 2, tasks.size());
		assertEquals("Tasks must be equal", t1, tasks.get(0));
		assertEquals("Tasks must be equal", t2, tasks.get(1));
		assertTrue("Task must not have any requirements", 
				tasks.get(0).getRequirements().size() == 0);
		assertTrue("Task must have a single TaskRequirement", 
				tasks.get(1).getRequirements().size() == 1);
		assertTrue("Those requirements must be equal",
				tasks.get(1).getRequirements().stream()
				.anyMatch(x -> x.getSkill().getId() ==
				skill1.getId()));
		
		clear();
	}
	
	@Test
	public void canInsertAndThenUpdate() {
		clear();
		
		Skill s1 = new Skill("Skill#1", null);
		s1.setMaxLevel(1);
		s1.setName_RU("Умение 1");
		Skill s2 = new Skill("Skill#2", null);
		s2.setName_RU("Умение 2");
		s2.setMaxLevel(1);
		skillDAO.insert(s1);
		skillDAO.insert(s2);
		
		TaskRequirement c1 = new TaskRequirement(s1, 1);
		Task t1 = new Task("Task 1", null);
		t1.setName_RU("Задача 1");
		t1.addRequirement(c1);
		
		taskDAO.insert(t1);
		
		t1.setName("Task 2");
		TaskRequirement c2 = new TaskRequirement(s2, 1);
		t1.addRequirement(c2);
		t1 = taskDAO.update(t1);
		
		List<Task> tasks = taskDAO.getAll();
		
		assertTrue("Must have 1 task", tasks.size() == 1);
		assertEquals("Tasks must be equal", t1, tasks.get(0));
		assertTrue("Task must have two requirements", 
				tasks.get(0).getRequirements().size() == 2);
		assertTrue("Those requirements must be equal",
				tasks.get(0).getRequirements().stream()
				.anyMatch(x -> x.getSkill().equals(c2.getSkill())));
		
		clear();
	}
	
	@Test
	public void canInsertAndThenDelete() {
		clear();
		
		Skill s1 = new Skill("Skill#1", null);
		s1.setName_RU("Умение 1");
		s1.setMaxLevel(1);
		skillDAO.insert(s1);
		TaskRequirement c1 = new TaskRequirement(s1, 1);
		Task t1 = new Task("Task 1", null);
		t1.addRequirement(c1);
		t1.setName_RU("Task 1");
		
		taskDAO.insert(t1);
		taskDAO.delete(t1);
		
		List<Task> tasks = taskDAO.getAll();
		
		assertTrue("Must not have any task", tasks.size() == 0);
		
		clear();
	}
	
	@Test
	public void cannotInsertDuplicateCompetencies() {
		clear();
		
		exception.expect(DataAccessException.class);
		
		Skill s1 = new Skill("Skill#1", null);
		s1.setName_RU("Умение 1");
		s1.setMaxLevel(2);
		skillDAO.insert(s1);
		
		TaskRequirement comp = new TaskRequirement(s1, 1);
		TaskRequirement comp2 = new TaskRequirement(s1, 2);
		Task task = new Task("Task 1", null);
		task.setName_RU("Task 1");
		task.addRequirement(comp);
		task.addRequirement(comp2);
		
		taskDAO.insert(task);
		exception.reportMissingExceptionWithMessage("Doesn't throw an exception here");
		exception = ExpectedException.none();
		
		clear();
	}
	
	@Test
	public void cannotUpdateDuplicateCompetencies() {
		clear();
		
		exception.expect(DataAccessException.class);
		
		Skill s1 = new Skill("Skill#1", null);
		s1.setMaxLevel(2);
		s1.setName_RU("Умение 1");
		skillDAO.insert(s1);
		Skill s2 = new Skill("Skill#2", null);
		s2.setMaxLevel(2);
		s2.setName_RU("Умение 2");
		skillDAO.insert(s2);
		
		TaskRequirement comp = new TaskRequirement(s1, 1);
		TaskRequirement comp2 = new TaskRequirement(s2, 1);
		Task task = new Task("Task 1", null);
		task.setName_RU("Task 1");
		task.addRequirement(comp);
		task.addRequirement(comp2);
		taskDAO.insert(task);
		
		comp2.setSkill(s1);
		
		taskDAO.update(task);
		exception.reportMissingExceptionWithMessage("Doesn't throw an exception here");
		exception = ExpectedException.none();
		
		clear();
	}
	
}
