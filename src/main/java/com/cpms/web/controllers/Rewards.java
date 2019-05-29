package com.cpms.web.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cpms.data.entities.Motivation;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Reward;
import com.cpms.facade.ICPMSFacade;
import com.cpms.web.RewardPostForm;
import com.cpms.web.ajax.IAjaxAnswer;
import com.cpms.web.ajax.RewardAnswer;

/**
 * Handles skill CRUD web application requests.
 * Almost completely deprecated because of {@link SkillTree}.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/rewards")
public class Rewards {

	@Autowired
	@Qualifier(value = "facade")
	private ICPMSFacade facade;
	
	@RequestMapping(value = {"/", ""},
			method = RequestMethod.GET)
	public String rewards(Model model, Principal principal,
			HttpServletRequest request) {
		model.addAttribute("_VIEW_TITLE", "navbar.reward");
		model.addAttribute("_FORCE_CSRF", true);
		
		Map<String, List<RewardPostForm>> blocks = new LinkedHashMap<>();
		List<RewardPostForm> rewards = new ArrayList<>();
		String curMounth = "";
		List<Reward> allRewards = facade.getRewardDAO().getAll();
		Collections.sort(allRewards);
		for (Reward reward : allRewards) {
			if (!reward.getPresentationName().equals(curMounth)) {
				if (!rewards.isEmpty())
					blocks.put(curMounth, rewards);
				curMounth = reward.getPresentationName();
				rewards = new ArrayList<>();
			}
			rewards.add(new RewardPostForm(reward, facade));
		}
		if (!rewards.isEmpty())
			blocks.put(curMounth, rewards);
		model.addAttribute("rewards", blocks);
		model.addAttribute("experts", facade.getProfileDAO().getAll());
		model.addAttribute("motivations", facade.getMotivationDAO().getAll());
		model.addAttribute("reward", new RewardPostForm());
		
		return "rewards";
	}
	
	@RequestMapping(path = {"/delete"}, 
			method = RequestMethod.GET)
	public String rewardDelete(Model model,
			@RequestParam(name = "id", required = true) Long id) {
		Reward reward = facade.getRewardDAO().getOne(id);
		facade.getRewardDAO().delete(reward);
		return "redirect:/rewards";
	}
	
	@ResponseBody
	@RequestMapping(value = "/ajaxReward",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxReward(
			@RequestBody String json, Principal principal) {
		List<Object> values = DashboardAjax.parseJson(json);
		if (values.size() >= 1 && DashboardAjax.isInteger(values.get(0).toString(), 10)) {
			long id = Long.parseLong(values.get(0).toString());
			if (id < 0) {
				// Change/view reward
				return new RewardAnswer(facade.getRewardDAO().getOne(-id));
			} else {
				// New reward
				return new RewardAnswer();
			}
		} else {
			return new RewardAnswer();
		}
	}
	
	@RequestMapping(path = "/async", 
			method = RequestMethod.POST)
	public String rewardCreateAsync(Model model,
			@ModelAttribute RewardPostForm recievedReward,
			HttpServletRequest request,
			Principal principal) {
		Reward reward = new Reward();
		if (recievedReward.getId() > 0) 
			reward = facade.getRewardDAO().getOne(recievedReward.getId());
		
		String experts = "", motivations = "";
		if (recievedReward.getExperts().isEmpty())
			experts = "0";
		else for (Profile expert : recievedReward.getExperts())
			if (expert == null) {
				experts = "0";
				break;
			}
			else if (experts.isEmpty()) experts += expert.getId();
			else experts += "," + expert.getId();
		reward.setExperts(experts);
		if (recievedReward.getMotivations().isEmpty())
			motivations = "0";
		else for (Motivation motivation : recievedReward.getMotivations())
			if (motivation == null) {
				motivations = "0";
				break;
			}
			else if (motivations.isEmpty()) motivations += motivation.getId();
			else motivations += "," + motivation.getId();
		reward.setMotivations(motivations);
		
		if (recievedReward.getId() == 0)
			reward = facade.getRewardDAO().insert(reward);
		else
			reward = facade.getRewardDAO().update(reward);

		return "redirect:/rewards";
	}
}
