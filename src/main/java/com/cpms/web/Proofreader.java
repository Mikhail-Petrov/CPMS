package com.cpms.web;

import java.util.Date;
import java.util.Set;

import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Motivation;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Reward;
import com.cpms.data.entities.TaskCenter;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.entities.Users;

public class Proofreader {

	private String name, availability;
	private long id, userId;
	private double gsl;
	private int tasks;
	
	public Proofreader(Profile profile, ICPMSFacade facade, IUserDAO userDAO) {
		setName(profile.getName());
		setAvailability(profile.getAvailability());
		setId(profile.getId());
		setGsl(Math.round(getGSL(profile, facade, userDAO) * 100));
		setTasks(calculateTasks(profile, userDAO));
		Users user = userDAO.getByUsername(profile.getName());
		if (user == null)
			setUserId(0);
		else
			setUserId(user.getId());
	}
	
	public static double getGSL(Profile profile, ICPMSFacade facade, IUserDAO userDAO) {

		double gsl1 = 0, gsl2, gsl3, gsl4 = 0, gsl5 = 0;
		
		Users user = userDAO.getByUsername(profile.getName());
		if (user != null) {
			Set<TaskCenter> tasks = user.getTasks();
			for (TaskCenter task : tasks) {
				Date completedDate = task.getTask().getCompletedDate();
				Date dueDate = task.getTask().getDueDate();
				if (completedDate != null && (dueDate == null || !completedDate.after(dueDate)))
					gsl1++;
			}
			if (!tasks.isEmpty())
				gsl1 /= tasks.size();
		}
		String availability = profile.getAvailability();
		if (availability == null || availability.isEmpty())
			gsl2 = 0;
		else gsl2 = availability.equals("1") ? 1 : (
				availability.equals("2") ? 2.0/3 : (
						availability.equals("3") ? 1.0/3 : 0));
		
		Date startDate = profile.getStartDate();
		Date today = new Date();
		if (startDate == null) startDate = today;
		gsl3 = (today.getYear() - startDate.getYear()) * 12 + today.getMonth() - startDate.getMonth();
		if (today.getDate() < startDate.getDate())
			gsl3--;
		//gsl3 = (gsl3 + 1) / 36;
		gsl3 = 1 - 1 / (gsl3 / 12 + 1);
		if (gsl3 < 0) gsl3 = 0;
		
		Set<Competency> comps = profile.getCompetencies();
		for (Competency comp : comps)
			gsl4 += comp.getLevel();
		if (!comps.isEmpty())
			gsl4 /= comps.size();
		gsl4 /= 6;
		
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
				if (motiv != null) gsl5 += motiv.getCost();
			}
		}
		gsl5 = 1 - 1 / (0.3 * gsl5 + 1);
		
		return (gsl1+gsl2+gsl3+gsl4+gsl5)/5.0;
	}
	
	private int calculateTasks(Profile profile, IUserDAO userDAO) {
		Users user = userDAO.getByUsername(profile.getName());
		int res = 0;
		if (user != null) {
			Set<TaskCenter> tasks = user.getTasks();
			for (TaskCenter task : tasks) {
				if (task.getTask().getCompletedDate() != null)
					res++;
			}
		}
		return res;
	}
	
	public double getGsl() {
		return gsl;
	}
	public void setGsl(double gsl) {
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
}
