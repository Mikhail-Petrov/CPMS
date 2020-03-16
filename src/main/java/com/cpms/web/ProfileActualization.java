package com.cpms.web;

import java.util.ArrayList;
import java.util.Map;

import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.TaskRequirement;

public class ProfileActualization {
	
	private long id;
	
	private String name;
	
	private ArrayList<Competency> competencies;
	
	private ArrayList<Integer> values;

	public ProfileActualization(Profile profile) {
		setId(profile.getId());
		setName(profile.getName());
		setCompetencies(new ArrayList<>());
		setValues(new ArrayList<>());
	}
	
	public void addCompetency(Competency comp, int value) {
		competencies.add(comp);
		values.add(value);
	}
	
	public void calculateImpacts(Map<Long, Integer> Ps, double R, int s, Map<Long, Integer> reqs) {
		for (int i = 0; i < competencies.size(); i++) {
			Competency comp = competencies.get(i);
			int C = values.get(i), M = comp.getSkill().getMaxLevel();
			double L = comp.getLevel(), r = reqs.get(comp.getSkill().getId()), P = Ps.get(comp.getSkill().getId());
			double D = ((L/P)+(r/R))/2.0;
			int newVal = C + (int) (M*D*s);
			if (newVal > M) newVal = M;
			if (newVal < 1) newVal = 1;
			values.set(i, newVal);
		}
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public ArrayList<Competency> getCompetencies() {
		return competencies;
	}

	public void setCompetencies(ArrayList<Competency> competencies) {
		this.competencies = competencies;
	}

	public ArrayList<Integer> getValues() {
		return values;
	}

	public void setValues(ArrayList<Integer> values) {
		this.values = values;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
