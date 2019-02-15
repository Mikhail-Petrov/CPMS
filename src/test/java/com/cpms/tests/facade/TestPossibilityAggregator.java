package com.cpms.tests.facade;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

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
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TaskRequirement;
import com.cpms.operations.interfaces.IPossibilityAggregator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestingConfig.class)
public class TestPossibilityAggregator {
	
	private IPossibilityAggregator aggregator;

	@Autowired
	@Qualifier("possibilityAggregator")
	public void setTestable(IPossibilityAggregator aggregator) {
		this.aggregator = aggregator;
	}
	
	@Test
	public void aggregatesCorrectly() {
		Skill s1 = new Skill("S1", null);
		s1.setMaxLevel(3);
		s1.setId(1);
		Skill s2 = new Skill("S2", null);
		s2.setMaxLevel(3);
		s2.setId(2);
		Skill s3 = new Skill("S3", null);
		s3.setMaxLevel(3);
		s3.setId(3);
		Task t1 = new Task("t1", null);
		Task t2 = new Task("t2", null);
		Task t3 = new Task("t3", null);
		t1.addRequirement(new TaskRequirement(s1, 1));
		t1.addRequirement(new TaskRequirement(s2, 1));
		t1.addRequirement(new TaskRequirement(s3, 1));
		t2.addRequirement(new TaskRequirement(s1, 2));
		t2.addRequirement(new TaskRequirement(s2, 2));
		t2.addRequirement(new TaskRequirement(s3, 2));
		t3.addRequirement(new TaskRequirement(s1, 3));
		t3.addRequirement(new TaskRequirement(s2, 3));
		t3.addRequirement(new TaskRequirement(s3, 3));
		Profile p1 = new Profile();
		p1.addCompetency(new Competency(s2, 3));
		p1.addCompetency(new Competency(s1, 3));
		p1.addCompetency(new Competency(s3, 2));
		List<Task> tasks = new ArrayList<Task>();
		tasks.add(t1);
		tasks.add(t2);
		tasks.add(t3);
		
		List<Task> res = aggregator.aggregatePossibilities(p1, tasks);
		assertTrue("Should have found 2 tasks", res.size() == 2);
		assertTrue("Should have found task 1", res.stream()
				.anyMatch(x -> x.getName().equals("t1")));
		assertTrue("Should have found task 2", res.stream()
				.anyMatch(x -> x.getName().equals("t2")));
	}
	
}
