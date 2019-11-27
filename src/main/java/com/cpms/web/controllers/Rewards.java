package com.cpms.web.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Message;
import com.cpms.data.entities.MessageCenter;
import com.cpms.data.entities.Motivation;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Reward;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.entities.Users;
import com.cpms.web.MotivationUtils;
import com.cpms.web.ProfileRewardPostForm;
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

	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;

    @Autowired
	@Qualifier(value = "mailSender")
    public JavaMailSender emailSender;

    @Autowired
    private MessageSource messageSource;

	private long prevTime = -1;

	private String timeLog = "";


	private void updateTime(String event) {
		long curTime = System.currentTimeMillis();
		timeLog += String.format("\n%s: %d", event, (prevTime < 0) ? 0 : (curTime - prevTime));
		prevTime = curTime;
	}
	
	@RequestMapping(value = {"/", ""},
			method = RequestMethod.GET)
	public String rewards(Model model, Principal principal,
			HttpServletRequest request) {
		//updateTime("start");
		model.addAttribute("_VIEW_TITLE", "navbar.reward");
		model.addAttribute("_FORCE_CSRF", true);
		
		Map<String, List<RewardPostForm>> blocks = new LinkedHashMap<>();
		List<RewardPostForm> rewards = new ArrayList<>();
		String curMounth = "";
		List<Reward> allRewards = facade.getRewardDAO().getAll();
		Collections.sort(allRewards);
		//updateTime("rewards got");
		List<Profile> allProfiles = facade.getProfileDAO().getAll();
		Map<Long, Profile> profilesMap = allProfiles.stream().collect(Collectors.toMap(profile -> profile.getId(), profile -> profile));
		List<Motivation> allMotivations = facade.getMotivationDAO().getAll();
		Map<Long, Motivation> motivationMap = allMotivations.stream().collect(Collectors.toMap(motivation -> motivation.getId(), motivation ->motivation));
		for (Reward reward : allRewards) {
			if (!reward.getPresentationName().equals(curMounth)) {
				if (!rewards.isEmpty())
					blocks.put(curMounth, rewards);
				curMounth = reward.getPresentationName();
				rewards = new ArrayList<>();
			}
			rewards.add(new RewardPostForm(reward, allProfiles, allMotivations, profilesMap, motivationMap));
		}
		if (!rewards.isEmpty())
			blocks.put(curMounth, rewards);
		model.addAttribute("rewards", blocks);
		//updateTime("blocks added");
		Collections.sort(allProfiles);
		List<ProfileRewardPostForm> profiles = new ArrayList<>();
		for (Profile profile : allProfiles)
			profiles.add(new ProfileRewardPostForm(profile, allRewards, motivationMap));
		model.addAttribute("experts", profiles);
		//updateTime("experts added");
		int sumMotiv = 0, minBen = 0;
		if (profiles.size() > 0) minBen = profiles.get(0).getSumBenefit();
		for (ProfileRewardPostForm prof : profiles)
			if (prof.getSumBenefit() < minBen) minBen = prof.getSumBenefit();
		for (Motivation motiv : allMotivations)
			sumMotiv += motiv.getBenefit();
		model.addAttribute("sumMotiv", sumMotiv);
		//updateTime("motivations added");
		model.addAttribute("minBen", minBen);
		model.addAttribute("motivations", MotivationUtils.sortAndAddIndents(allMotivations));
		model.addAttribute("reward", new RewardPostForm());

		//updateTime("finish");
		//model.addAttribute("timeLog", timeLog);
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
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
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
		
		String experts = "", motivations = "", oldExperts = reward.getExperts(), motivationsList = "";
		if (recievedReward.getExperts().isEmpty() || recievedReward.getExperts().get(0) == null)
			recievedReward.setExperts(facade.getProfileDAO().getAll());
		for (Profile expert : recievedReward.getExperts())
			if (experts.isEmpty()) experts += expert.getId();
			else experts += "," + expert.getId();
		reward.setExperts(experts);
		if (recievedReward.getMotivations().isEmpty() || recievedReward.getMotivations().get(0) == null)
			recievedReward.setMotivations(facade.getMotivationDAO().getAll());
		for (Motivation motivation : recievedReward.getMotivations())
			if (motivations.isEmpty()) {
				motivations += motivation.getId();
				motivationsList += motivation.getPresentationName();
			}
			else {
				motivations += "," + motivation.getId();
				motivationsList += ", " + motivation.getPresentationName();
			}
		if (motivations.equals("0")) motivationsList = "all";
		reward.setMotivations(motivations);
		
		if (recievedReward.getId() == 0)
			reward = facade.getRewardDAO().insert(reward);
		else
			reward = facade.getRewardDAO().update(reward);

		// Send messages to new experts
		if (oldExperts == null) oldExperts = "";
		if (!oldExperts.equals("0")) {
			String[] oldIDs = oldExperts.split(",");
			// get new experts
			List<Profile> newProfiles;
			if (recievedReward.getExperts().isEmpty() || recievedReward.getExperts().get(0) == null)
				newProfiles = facade.getProfileDAO().getAll();
			else newProfiles = recievedReward.getExperts();
			// remove old experts
			for (int i = 0; i < oldIDs.length; i++)
				try {
					long oldID = Long.parseLong(oldIDs[i]);
					Profile oldProfile = facade.getProfileDAO().getOne(oldID);
					if (oldProfile != null)
						newProfiles.remove(oldProfile);
				} catch (NumberFormatException e) {}
			// send messages
			if (!newProfiles.isEmpty()) {
				Message newMessage = new Message();
				Users owner = Security.getUser(principal, userDAO);
				newMessage.setOwner(owner);
				if (newMessage.getOwner() == null)
					newMessage.setOwner(userDAO.getAll().get(0));
				newMessage.setTitle(String.format("New reward"));
				newMessage.setText(String.format("You have been rewarded by motivations: %s.", motivationsList));
				newMessage.setType("4");
				for (Profile newProfile : newProfiles) {
					if (newProfile == null) continue;
					Users newRecepient = userDAO.getByProfile(newProfile);
					if (newRecepient != null)
						newMessage.addRecipient(new MessageCenter(newRecepient));
				}

				String url = request.getRequestURL().toString().replace("rewards/async", "messages");
				for (MessageCenter center : newMessage.getRecipients())
					Messages.sendMessageEmail(url, emailSender, center.getUser(), newMessage.getText());
				
				facade.getMessageDAO().insert(newMessage);
			}
		}
		return "redirect:/rewards";
	}
}
