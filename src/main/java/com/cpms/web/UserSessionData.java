package com.cpms.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.Task;

/**
 * User session data, such as remember objects to be used in further search.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Component
public class UserSessionData {

	private Profile profile;
	private Task task;
	private Set<Skill> skills;
	private Set<Profile> profiles;
	private List<Competency> competencies;
	private Set<Task> tasks;

	public UserSessionData() {
		this.profile = null;
		this.task = null;
		this.skills = new HashSet<Skill>();
		this.profiles = new HashSet<Profile>();
		this.competencies = new ArrayList<Competency>();
	}
	
	
	public static String localizeText(String text) {
		Locale locale = LocaleContextHolder.getLocale();
		return text;
	}
	
	
	public static String localizeText(String text_ru, String text_en) {
		Locale locale = LocaleContextHolder.getLocale();
		if (locale.getLanguage().equals("ru"))
			return text_ru;
		else
			return text_en;
	}
	
	public Set<Task> getTasks() {
		return tasks;
	}

	public void setTasks(Set<Task> tasks) {
		this.tasks = tasks;
	}

	public Profile getProfile() {
		if (profile != null) {
			Profile clone = profile.clone();
			clone.setCompetencies(profile.getCompetencies());
			return clone;
		} else {
			return null;
		}
	}

	public void setProfile(Profile profile1) {
		this.profile = profile1;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Set<Skill> getSkills() {
		return skills;
	}

	public void setSkills(Set<Skill> skills) {
		this.skills = skills;
	}

	public Set<Profile> getProfiles() {
		return profiles;
	}

	public void setProfiles(Set<Profile> profiles) {
		this.profiles = profiles;
	}

	public List<Competency> getCompetencies() {
		return competencies;
	}

	public void setCompetencies(List<Competency> competencies) {
		this.competencies = competencies;
	}
	
	public void addSkill(Skill skill) {
		skills.add(skill);
	}
	
	public void removeSkill(Skill skill) {
		skills.remove(skill);
	}
	
	public void addProfile(Profile profile) {
		profiles.add(profile);
	}
	
	public void removeProfile(Profile profile) {
		profiles.remove(profile);
	}
	
	public void addCompetency(Competency competency) {
		competencies.add(competency);
	}
	
	public void removeCompetency(Competency competency) {
		competencies.remove(competency);
	}
}
