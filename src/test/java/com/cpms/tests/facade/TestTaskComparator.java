package com.cpms.tests.facade;

import static org.junit.Assert.*;

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
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TaskRequirement;
import com.cpms.operations.interfaces.ITaskComparator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestingConfig.class)
public class TestTaskComparator {
	
	private ITaskComparator taskComparator;
	
	@Autowired
	@Qualifier("taskComparator")
	public void setTaskComparator(ITaskComparator taskComparator) {
		this.taskComparator = taskComparator;
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
		Profile p1 = new Company();
		p1.addCompetency(new Competency(s1, 2));
		p1.addCompetency(new Competency(s2, 2));
		p1.addCompetency(new Competency(s3, 1));
		Task t1 = new Task("T1", null);
		t1.addRequirement(new TaskRequirement(s2, 2));
		t1.addRequirement(new TaskRequirement(s1, 1));
		t1.addRequirement(new TaskRequirement(s3, 1));
		
		assertTrue("Should be sufficient",
				taskComparator.taskCompare(p1, t1));
	}
	
	@Test
	public void comparesCorrectlyCase2() {
		Skill s1 = new Skill("S1", null);
		s1.setMaxLevel(2);
		s1.setId(1);
		Skill s2 = new Skill("S2", null);
		s2.setMaxLevel(2);
		s2.setId(2);
		Skill s3 = new Skill("S3", null);
		s3.setMaxLevel(2);
		s3.setId(3);
		Profile p1 = new Company();
		p1.addCompetency(new Competency(s1, 2));
		p1.addCompetency(new Competency(s2, 2));
		Task t1 = new Task("T1", null);
		t1.addRequirement(new TaskRequirement(s1, 1));
		t1.addRequirement(new TaskRequirement(s3, 1));
		
		assertFalse("Should not be sufficient",
				taskComparator.taskCompare(p1, t1));
	}
	
	@Test
	public void comparesCorrectlyCase3() {
		Skill s1 = new Skill("S1", null);
		s1.setMaxLevel(2);
		s1.setId(1);
		Skill s2 = new Skill("S2", null);
		s2.setMaxLevel(2);
		s2.setId(2);
		Skill s3 = new Skill("S3", null);
		s3.setMaxLevel(2);
		s3.setId(3);
		Profile p1 = new Company();
		p1.addCompetency(new Competency(s1, 1));
		p1.addCompetency(new Competency(s2, 1));
		p1.addCompetency(new Competency(s3, 1));
		Task t1 = new Task("T1", null);
		t1.addRequirement(new TaskRequirement(s2, 1));
		t1.addRequirement(new TaskRequirement(s1, 2));
		t1.addRequirement(new TaskRequirement(s3, 1));
		
		assertFalse("Should not be sufficient",
				taskComparator.taskCompare(p1, t1));
	}
	
}
