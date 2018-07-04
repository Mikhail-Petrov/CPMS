package com.cpms.data.entities;

import java.util.HashSet;
import java.util.Set;

public class Option {
	private Set<Resident> residents;
	private long optimality;
	
	private Task task;
	private Set<TaskRequirement> requirements;
	
	public Option(Task task) {
		this.task = task;
		requirements = task.getRequirements();
		optimality = 0;
		residents = new HashSet<>();
	}
	
	public Set<Resident> getResidents() {
		return residents;
	}
	public void setResidents(Set<Resident> residents) {
		this.residents = residents;
	}
	public long getOptimality() {
		return optimality;
	}
	public void setOptimality(long optimality) {
		this.optimality = optimality;
	}
	
	public void addResident(Profile profile) {
		residents.add(new Resident(profile));
		calculateOptimality();
	}
	
	private void calculateOptimality() {
		optimality = 0;
	}
}
