package com.cpms.web;

import java.util.ArrayList;
import java.util.List;

import com.cpms.data.entities.Motivation;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Reward;
import com.cpms.facade.ICPMSFacade;
import com.cpms.web.ajax.IAjaxAnswer;

/**
 * Alternative reward form
 * 
 * @author Petrov Mikhail
 * @since 1.0
 */
public class RewardPostForm implements IAjaxAnswer {

	private long id;
	
	private List<Profile> experts;
	
	private List<Motivation> motivations;
	
	private String sendedTime;
	
	public RewardPostForm() {
		setId(0);
		getExperts();
		getMotivations();
		setSendedTime("");
	}
	
	public RewardPostForm(Reward reward, ICPMSFacade facade) {
		setId(reward.getId());
		setSendedTime(reward.getSendedTime().toString());
		getExperts();
		String[] expertsIDs = reward.getExperts().split(",");
		if (expertsIDs.length > 0 && expertsIDs[0].equals("0"))
			for (Profile expert : facade.getProfileDAO().getAll())
				experts.add(expert);
		else
			for (int i = 0; i < expertsIDs.length; i++) {
				long expertId = 0;
				try { expertId = Long.parseLong(expertsIDs[i]); }
				catch (NumberFormatException e) {}
				if (expertId <= 0) continue;
				Profile expert = facade.getProfileDAO().getOne(expertId);
				if (expert != null)
					experts.add(expert);
			}
		getMotivations();
		String[] motivationsIDs = reward.getMotivations().split(",");
		if (motivationsIDs.length > 0 && motivationsIDs[0].equals("0")) {
			for (Motivation motivation : facade.getMotivationDAO().getAll())
				if (!motivation.getIsGroup())
					motivations.add(motivation);
		} else
			for (int i = 0; i < motivationsIDs.length; i++) {
				long motivationId = 0;
				try { motivationId = Long.parseLong(motivationsIDs[i]); }
				catch (NumberFormatException e) {}
				if (motivationId <= 0) continue;
				Motivation motivation = facade.getMotivationDAO().getOne(motivationId);
				if (motivation != null)
					motivations.add(motivation);
			}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public List<Profile> getExperts() {
		if (experts == null) experts = new ArrayList<>();
		return experts;
	}

	public void setExperts(List<Profile> experts) {
		this.experts = experts;
	}

	public List<Motivation> getMotivations() {
		if (motivations == null) motivations = new ArrayList<>();
		return motivations;
	}

	public void setMotivations(List<Motivation> motivations) {
		this.motivations = motivations;
	}

	public String getSendedTime() {
		return sendedTime;
	}

	public void setSendedTime(String sendedTime) {
		this.sendedTime = sendedTime;
	}
	
}
