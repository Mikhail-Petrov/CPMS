package com.cpms.web;

import java.util.List;
import java.util.Map;

import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.Motivation;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Reward;
import com.cpms.web.ajax.IAjaxAnswer;

/**
 * Alternative reward form
 * 
 * @author Petrov Mikhail
 * @since 1.0
 */
public class ProfileRewardPostForm implements IAjaxAnswer {

	private long id;
	
	private String name;
	
	private int sumBenefit;
	
	public ProfileRewardPostForm() {
		setId(0);
		setName("");
		setSumBenefit(0);
	}
	
	public ProfileRewardPostForm(Profile profile, List<Reward> rewards, Map<Long, Motivation> allMotivations) {
		setId(profile.getId());
		setName(profile.getName());
		
		sumBenefit = 0;
		for (Reward reward : rewards) {
			String[] expertIDs = reward.getExperts().split(",");
			boolean myReward = expertIDs[0].equals("0");
			for (int i = 0; i < expertIDs.length && !myReward; i++)
				if (expertIDs[i].equals(getId() + ""))
					myReward = true;
			if (myReward) {
				int sumMotBen = 0;
				String[] motivIDs = reward.getMotivations().split(",");
				if (motivIDs[0].equals("0"))
					for (Map.Entry<Long, Motivation> motiv : allMotivations.entrySet())
						sumMotBen += motiv.getValue().getBenefit();
				else
					for (int i = 0; i < motivIDs.length; i++) {
						long motivID = 0;
						try { motivID = Long.parseLong(motivIDs[i]); }
						catch (NumberFormatException e) {}
						if (motivID <= 0) continue;
						Motivation motiv = allMotivations.get(motivID);
						if (motiv != null)
							sumMotBen += motiv.getBenefit();
					}
				sumBenefit += sumMotBen;
			}
		}
		sumBenefit = 49 - sumBenefit;
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

	public int getSumBenefit() {
		return sumBenefit;
	}

	public void setSumBenefit(int sumBenefit) {
		this.sumBenefit = sumBenefit;
	}
	
}
