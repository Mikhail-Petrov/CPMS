package com.cpms.web;

import java.util.Date;
import java.util.Set;

import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Motivation;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Proofreading;
import com.cpms.data.entities.Reward;
import com.cpms.data.entities.TaskCenter;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.entities.Users;

public class Proofreader {

	private String name, availability;
	private long id, userId;
	private double[] gsl;
	private int tasks;
	private String targets;
	
	public Proofreader(Profile profile, ICPMSFacade facade, IUserDAO userDAO) {
		setName(profile.getName());
		setAvailability(profile.getAvailability());
		setId(profile.getId());
		setGsl(getGSL(profile, facade, userDAO));
		setTasks(calculateTasks(profile, userDAO));
		Users user = userDAO.getByUsername(profile.getName());
		if (user == null)
			setUserId(0);
		else
			setUserId(user.getId());
		setTargets("");
		for (Proofreading pr : profile.getProofs())
			setTargets(getTargets() + pr.getTo().getCode() + ",");
	}
	
	public static double[] getGSL(Profile profile, ICPMSFacade facade, IUserDAO userDAO) {

		double perfomance = 0, avail, experience, knowledge = 0, motivs = 0;
		
		Users user = userDAO.getByProfile(profile);
		if (user != null) {
			Set<TaskCenter> tasks = user.getTasks();
			for (TaskCenter task : tasks) {
				Date completedDate = task.getTask().getCompletedDate();
				Date dueDate = task.getTask().getDueDate();
				if (completedDate != null && (dueDate == null || !completedDate.after(dueDate)))
					perfomance++;
			}
			if (!tasks.isEmpty())
				perfomance /= tasks.size();
		}
		String availability = profile.getAvailability();
		if (availability == null || availability.isEmpty())
			avail = 0;
		else avail = availability.equals("1") ? 1 : (
				availability.equals("2") ? 2.0/3 : (
						availability.equals("3") ? 1.0/3 : 0));
		
		Date startDate = profile.getStartDate();
		Date today = new Date();
		if (startDate == null) startDate = today;
		experience = (today.getYear() - startDate.getYear()) * 12 + today.getMonth() - startDate.getMonth();
		if (today.getDate() < startDate.getDate())
			experience--;
		//gsl3 = (gsl3 + 1) / 36;
		experience = 1 - 1 / (experience / 12 + 1);
		if (experience < 0) experience = 0;
		
		Set<Competency> comps = profile.getCompetencies();
		for (Competency comp : comps)
			knowledge += comp.getLevel();
		if (!comps.isEmpty())
			knowledge /= comps.size();
		knowledge /= 6;
		
		for (Reward reward : facade.getRewardDAO().getAll()) {
			String[] expertIDs = reward.getExperts().split(",");
			boolean myReward = false;
			for (int i = 0; i < expertIDs.length; i++)
				if (expertIDs[i].equals(profile.getId() + ""))
					myReward = true;
			if (!myReward) continue;
			String[] motivIDs = reward.getMotivations().split(",");
			for (int i = 0; i < motivIDs.length; i++) {
				long motivID = 0;
				try { motivID = Long.parseLong(motivIDs[i]); }
				catch (NumberFormatException e) {}
				if (motivID <= 0) continue;
				Motivation motiv = facade.getMotivationDAO().getOne(motivID);
				if (motiv != null) motivs += motiv.getCost();
			}
		}
		motivs = 1 - 1 / (0.3 * motivs + 1);
		
		double res[] = {(perfomance+avail+experience+knowledge+motivs)/5.0, avail, perfomance, experience, knowledge, motivs};
		return res;
	}
	
	private int calculateTasks(Profile profile, IUserDAO userDAO) {
		Users user = userDAO.getByProfile(profile);
		int res = 0;
		if (user != null) {
			Set<TaskCenter> tasks = user.getTasks();
			for (TaskCenter task : tasks) {
				if (task.getTask().getCompletedDate() == null)
					res++;
			}
		}
		return res;
	}
	
	public double getOptimality(double[] coefs) {
		if (coefs == null || coefs.length <= 0) return 0;
		if (gsl == null || gsl.length <= 0) return 0;
		double res = 1 / Math.sqrt(tasks + 1) * coefs[0];
		for (int i = 1; i < gsl.length; i++)
			if (i < coefs.length)
				res += gsl[i] * coefs[i];
		return res;
	}
	
	public double[] getGsl() {
		return gsl;
	}
	public void setGsl(double[] gsl) {
		this.gsl = gsl;
	}
	public int getTasks() {
		return tasks;
	}
	public void setTasks(int tasks) {
		this.tasks = tasks;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAvailability() {
		return availability;
	}

	public void setAvailability(String availability) {
		this.availability = availability;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getTargets() {
		return targets;
	}

	public void setTargets(String targets) {
		this.targets = targets;
	}
}
