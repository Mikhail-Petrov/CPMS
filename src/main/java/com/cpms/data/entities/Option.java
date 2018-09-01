package com.cpms.data.entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Option implements Comparable<Option> {
	private Set<Resident> residents;
	private double optimality;
	
	private Task task;
	private Set<TaskRequirement> requirements;
	
	public Option(Task task) {
		this.task = task;
		requirements = task.getRequirements();
		optimality = 0;
		residents = new HashSet<>();
	}
	
	public Option(Option option) {
		task = option.task;
		requirements = task.getRequirements();
		residents = new HashSet<>();
		option.residents.forEach(x -> residents.add(new Resident(x)));
		calculateOptimality();
		residents.forEach(x -> x.removeUnusedCompetencies(requirements));
	}
	
	public Task getTask() {
		return task;
	}
	public void setTask (Task task) {
		this.task = task;
	}
	public Set<Resident> getResidents() {
		return residents;
	}
	public void setResidents(Set<Resident> residents) {
		this.residents = residents;
	}
	public double getOptimality() {
		return optimality;
	}
	public void setOptimality(double optimality) {
		this.optimality = optimality;
	}
	
	public void addResident(Profile profile) {
		residents.add(new Resident(profile));
	}
	
	private void calculateOptimality() {
		if (residents.isEmpty()) {
			optimality = 0;
			return;
		}
		// for all requirements find best levels and required levels
		HashMap<Long, Integer> bestLevels = new HashMap<>();
		HashMap<Long, Integer> reqLevels = new HashMap<>();
		for (TaskRequirement req : requirements) {
			long curKey = req.getSkill().getId();
			bestLevels.put(curKey, 0);
			for (Resident res : residents)
				for (Competency comp : res.getCompetencies())
					if (comp.getSkill().getId() == curKey) {
						if (comp.getLevel() > bestLevels.get(curKey))
							bestLevels.put(curKey, comp.getLevel());
						break;
					}
			reqLevels.put(curKey, req.getLevel());
		}
		// calculate cost and levels
		long cost = 0;
		double levels = 0;
		for (Resident res : residents) {
			for (Competency comp : res.getCompetencies()) {
				long curKey = comp.getSkill().getId();
				if (bestLevels.containsKey(curKey) && comp.getLevel() == bestLevels.get(curKey)) {
					// for each 'best' resident's competency add his level
					levels += comp.getLevel() / reqLevels.get(curKey);
				}
				if (reqLevels.containsKey(curKey) && comp.getLevel() >= reqLevels.get(curKey)) {
					// for each 'good' resident's competency add his cost
					cost += res.getCost();
				}
			}
		}
		// minus unfilled requirements
		for (Map.Entry<Long, Integer> entry : bestLevels.entrySet()) {
			int reqLevel = reqLevels.get(entry.getKey());
			if (entry.getValue() < reqLevel)
				levels -= 1 - entry.getValue() / reqLevel;
		}
		// calculate Reconcilability
		double reconcilability = 1;
		optimality = levels * reconcilability / (double) cost;
	}
	
	double getWorstOptimality() {
		return -requirements.size() - 1;
	}
	
	public void removeBadResident() {
		// find and remove worse resident
		double bestOptimality = getWorstOptimality();
		Resident worseRes = null;
		Set<Resident> oldResidents = new HashSet<>(residents);
		for (Resident resident : oldResidents) {
			// calculate optimality without this resident
			residents.remove(resident);
			calculateOptimality();
			if (optimality >= bestOptimality) {
				bestOptimality = optimality;
				worseRes = resident;
			}
			residents.add(resident);
		}
		residents.remove(worseRes);
		calculateOptimality();
	}

	@Override
	public int compareTo(Option o) {
		return optimality > o.optimality ? -1 : (optimality < o.optimality ? 1 : 0);
	}
}
