package com.cpms.web.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.Task;
import com.cpms.exceptions.WrongJsonException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.web.UserSessionData;
import com.cpms.web.ajax.IAjaxAnswer;
import com.cpms.web.ajax.MessageAnswer;
//import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AJAX operations for working with {@link UserSessionData} entities.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/dashboard")
public class DashboardAjax {
	
	@Autowired
	@Qualifier(value = "userSessionData")
	private UserSessionData sessionData;
	
	@Autowired
	@Qualifier(value = "facade")
	private ICPMSFacade facade;
	
	@Autowired
	private MessageSource messageSource;

	@ResponseBody
	@RequestMapping(value = "/rememberProfile",
			method = RequestMethod.POST)
	public IAjaxAnswer rememberProfile(
			@RequestBody String json) {
		List<Object> values = parseJson(json);
		if (values.size() >= 1 && isInteger(values.get(0).toString(), 10)) {
			Profile profile = facade.getProfileDAO()
				.getOne(Integer.parseInt(values.get(0).toString()));
			sessionData.setProfile(profile);
		} else {
			String errorMessage = messageSource.getMessage(
					"popup.message.error",
					null,
					"An unexpected error hac occured!",
					LocaleContextHolder.getLocale());
			return new MessageAnswer(errorMessage);
		}
		String positiveMessage = messageSource.getMessage(
				"popup.message.profile.remembered",
				null,
				"This profile is now remembered!",
				LocaleContextHolder.getLocale());
		return new MessageAnswer(positiveMessage);
	}
	
	@ResponseBody
	@RequestMapping(value = "/rememberSkill",
			method = RequestMethod.POST)
	public IAjaxAnswer rememberSkill(
			@RequestBody String json) {
		List<Object> values = parseJson(json);
		if (values.size() >= 1 && isInteger(values.get(0).toString(), 10)) {
			int id = Integer.parseInt(values.get(0).toString(), 10);
			Skill skill = facade
				.getSkillDAO()
				.getAll()
				.stream()
				.filter(x -> x.getId() == id)
				.findFirst()
				.orElse(null);
			synchronized (sessionData.getSkills()) {
				sessionData.addSkill(skill);
			}
		} else {
			String errorMessage = messageSource.getMessage(
					"popup.message.error",
					null,
					"An unexpected error hac occured!",
					LocaleContextHolder.getLocale());
			return new MessageAnswer(errorMessage);
		}
		String positiveMessage = messageSource.getMessage(
				"popup.message.skill.remembered",
				null,
				"This skill is now added to the list of skills remembered!",
				LocaleContextHolder.getLocale());
		return new MessageAnswer(positiveMessage);
	}
	
	@ResponseBody
	@RequestMapping(value = "/rememberTask",
			method = RequestMethod.POST)
	public IAjaxAnswer rememberTask(
			@RequestBody String json) {
		List<Object> values = parseJson(json);
		if (values.size() >= 1 && isInteger(values.get(0).toString(), 10)) {
			int id = Integer.parseInt(values.get(0).toString(), 10);
			Task task = facade
					.getTaskDAO()
					.getAll()
					.stream()
					.filter(x -> x.getId() == id)
					.findFirst()
					.orElse(null);
			sessionData.setTask(task);
		} else {
			String errorMessage = messageSource.getMessage(
					"popup.message.error",
					null,
					"An unexpected error hac occured!",
					LocaleContextHolder.getLocale());
			return new MessageAnswer(errorMessage);
		}
		String positiveMessage = messageSource.getMessage(
				"popup.message.task.remembered",
				null,
				"This task is now remembered!",
				LocaleContextHolder.getLocale());
		return new MessageAnswer(positiveMessage);
	}
	
	@ResponseBody
	@RequestMapping(value = "/compareProfile",
			method = RequestMethod.POST)
	public IAjaxAnswer compareProfile(
			@RequestBody String json) {
		if (sessionData.getProfile() == null) {
			String errorMessage = messageSource.getMessage(
					"popup.message.no_profile",
					null,
					"Please select a profile to compare with first!"
					+ "\nTo do this, click \"Remember Profile\" on a profile.",
					LocaleContextHolder.getLocale());
			return new MessageAnswer(errorMessage);
		}
		List<Object> values = parseJson(json);
		if (values.size() >= 1 && isInteger(values.get(0).toString(), 10)) {
			Profile profile = facade.getProfileDAO()
				.getOne(Integer.parseInt(values.get(0).toString()));
			double result = facade.getProfileComparator()
				.compareProfiles(profile, sessionData.getProfile());
			String resultMessage = messageSource.getMessage(
					"popup.message.compare.result",
					null,
					"The result of comparing your profiles is:",
					LocaleContextHolder.getLocale());
			String hintMessage = messageSource.getMessage(
					facade.getProfileComparator().getHintMessage(),
					null,
					"",
					LocaleContextHolder.getLocale());
			return new MessageAnswer(resultMessage + " "
					+ result + ".\n" 
					+ hintMessage);
		} else {
			String errorMessage = messageSource.getMessage(
					"popup.message.error",
					null,
					"An unexpected error hac occured!",
					LocaleContextHolder.getLocale());
			return new MessageAnswer(errorMessage);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/compareTask",
			method = RequestMethod.POST)
	public IAjaxAnswer compareTask(
			@RequestBody String json) {
		Profile sessionProfile = sessionData.getProfile(),
				recievedProfile = null;
		Task sessionTask = sessionData.getTask(),
				recievedTask = null;
		List<Object> values = parseJson(json);
		if (values.size() >= 1 && isInteger(values.get(0).toString(), 10)) {
			recievedProfile = facade.getProfileDAO()
				.getOne(Integer.parseInt(values.get(0).toString()));
		}
		if (values.size() >= 2 && isInteger(values.get(1).toString(), 10)) {
			recievedTask = facade.getTaskDAO()
				.getOne(Integer.parseInt(values.get(1).toString()));
		}
		if (sessionProfile == null) {
			if (recievedProfile != null) {
				sessionProfile = recievedProfile;
			} else {
				String errorMessage = messageSource.getMessage(
						"popup.message.no_profile",
						null,
						"Please select a profile to compare with first!"
						+ "\nTo do this, click \"Remember Profile\" on a profile.",
						LocaleContextHolder.getLocale());
				return new MessageAnswer(errorMessage);
			}
		}
		if (sessionTask == null) {
			if (recievedTask != null) {
				sessionTask = recievedTask;
			} else {
				String errorMessage = messageSource.getMessage(
						"popup.message.no_task",
						null,
						"Please select a task to compare with first!"
						+ "\nTo do this, click \"Remember Task\" on a task.",
						LocaleContextHolder.getLocale());
				return new MessageAnswer(errorMessage);
			}
		}
		boolean result = facade.getTaskComparator()
				.taskCompare(sessionProfile, sessionTask);
		String resultMessage = messageSource.getMessage(
				"popup.result",
				null,
				"Result:",
				LocaleContextHolder.getLocale());
		return new MessageAnswer(resultMessage + " " + result + ".");
	}
	
	public static boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i),radix) < 0) return false;
	    }
	    return true;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Object> parseJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		List<Object> values = null;
		try {
			values = mapper.readValue(json, ArrayList.class);
		} catch (IOException e) {
			throw new WrongJsonException(json, e);
		}
		return values;
	}
	
}
