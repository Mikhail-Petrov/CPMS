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
	
	private String name;
	
	private String description;
	
	private List<Profile> experts;
	
	private List<Motivation> motivations;
	
	public RewardPostForm() {
		setId(0);
		setName("");
		setDescription("");
	}
	
	public RewardPostForm(Reward reward, ICPMSFacade facade) {
		setName(reward.getName());
		setDescription(reward.getDescription());
		setId(reward.getId());
		getExperts();
		String[] expertsIDs = reward.getExperts().split(",");
		if (expertsIDs.length > 0 && expertsIDs[0] == "0")
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
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
	
}
