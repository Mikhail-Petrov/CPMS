package com.cpms.data.entities;

import java.util.LinkedHashSet;
import java.util.Set;

public class Resident implements Comparable<Resident> {
	private Set<Competency> competencies;
	private long cost, id;
	private String name;

	public Resident(Profile profile) {
		id = profile.getId();
		// name = UserSessionData.localizeText(profile.getTitle_RU(),
		// profile.getTitle());
		name = profile.getPresentationName();
		competencies = new LinkedHashSet<Competency>(profile.getCompetencies());
		cost = profile.getPrice();
		if (cost == 0)
			cost = 1;
	}

	public Resident(Resident profile) {
		id = profile.getId();
		// name = UserSessionData.localizeText(profile.getTitle_RU(),
		// profile.getTitle());
		name = profile.getName();
		competencies = new LinkedHashSet<Competency>(profile.getCompetencies());
		cost = profile.getCost();
	}

	public Set<Competency> getCompetencies() {
		return competencies;
	}

	public void setCompetencies(Set<Competency> competencies) {
		this.competencies = competencies;
	}

	public long getCost() {
		return cost;
	}

	public void setCost(long cost) {
		this.cost = cost;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void removeUnusedCompetencies(Set<TaskRequirement> requirements) {
		for (Competency comp : new LinkedHashSet<Competency>(competencies)) {
			boolean isUsed = false;
			for (TaskRequirement req : requirements)
				if (req.getSkill().getId() == comp.getSkill().getId() && req.getLevel() <= comp.getLevel())
					isUsed = true;
			if (!isUsed)
				competencies.remove(comp);
			}
	}

	@Override
	public int compareTo(Resident r) {
		return Long.compare(getCost(), r.getCost());// getName().toLowerCase().compareTo(r.getName().toLowerCase());
	}
}
