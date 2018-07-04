package com.cpms.data.entities;

import java.util.HashSet;
import java.util.Set;

import com.cpms.web.UserSessionData;

public class Resident {
	private Set<Competency> competencies;
	private long cost, id;
	private String name;
	
	public Resident(Profile profile) {
		id = profile.getId();
		//name = UserSessionData.localizeText(profile.getTitle_RU(), profile.getTitle());
		name = profile.getPresentationName();
		//competencies = profile.getCompetencies();
		competencies = new HashSet<>();
		for (Competency comp : profile.getCompetencies())
			if (competencies.size() < 3)
				competencies.add(comp);
		cost = profile.getPrice();
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
}
