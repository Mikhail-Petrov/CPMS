package com.cpms.web;

import com.cpms.data.entities.Skill;

/**
 * Class for skill matching while creating profile
 * 
 */
public class CompetencyMatching implements Comparable<CompetencyMatching> {

	private Skill skill;
	
	private int level;
	
	private double val;
	
	public CompetencyMatching() {
	}
	
	public CompetencyMatching(Skill skill, int level, double val) {
		this.skill = skill;
		this.level = level;
		this.val = val;
	}

	public Skill getSkill() {
		return skill;
	}

	public void setSkill(Skill skill) {
		this.skill = skill;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public double getVal() {
		return val;
	}

	public void setVal(double val) {
		this.val = val;
	}

	@Override
	public int compareTo(CompetencyMatching o) {
		return -Double.compare(getVal(), o.getVal());
	}
	
}
