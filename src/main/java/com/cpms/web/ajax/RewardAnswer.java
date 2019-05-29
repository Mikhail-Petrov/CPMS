package com.cpms.web.ajax;

import java.util.ArrayList;
import java.util.List;

import com.cpms.data.entities.Reward;
import com.cpms.web.ajax.IAjaxAnswer;

/**
 * Alternative reward form
 * 
 * @author Petrov Mikhail
 * @since 1.0
 */
public class RewardAnswer implements IAjaxAnswer {

	private long id;
	
	private List<String> expertsIDs;
	
	private List<String> motivationsIDs;
	
	public RewardAnswer() {
		setId(0);
		getExpertsIDs();
		getMotivationsIDs();
	}
	
	public RewardAnswer(Reward reward) {
		setId(reward.getId());
		getExpertsIDs();
		if (reward.getExperts() != null) {
			String[] experts = reward.getExperts().split(",");
			for (int i = 0; i < experts.length; i++)
				if (!experts[i].isEmpty())
					expertsIDs.add(experts[i]);
		}
		getMotivationsIDs();
		if (reward.getMotivations() != null) {
			String[] motivations = reward.getMotivations().split(",");
			for (int i = 0; i < motivations.length; i++)
				if (!motivations[i].isEmpty())
					motivationsIDs.add(motivations[i]);
		}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public List<String> getExpertsIDs() {
		if (expertsIDs == null) expertsIDs = new ArrayList<>();
		return expertsIDs;
	}

	public void setExpertsIDs(List<String> expertsIDs) {
		this.expertsIDs = expertsIDs;
	}

	public List<String> getMotivationsIDs() {
		if (motivationsIDs == null) motivationsIDs = new ArrayList<>();
		return motivationsIDs;
	}

	public void setMotivationsIDs(List<String> motivationsIDs) {
		this.motivationsIDs = motivationsIDs;
	}
	
}
