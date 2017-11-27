package com.cpms.web.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Profile;
import com.cpms.exceptions.DependentEntityNotFoundException;
import com.cpms.exceptions.SessionExpiredException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.web.SkillUtils;

/**
 * Handles competency CRUD web application requests.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/editor")
public class EditorCompetency {

	@Autowired
	@Qualifier(value = "facade")
	private ICPMSFacade facade;
	
	@RequestMapping(path = "/{profileId}/competencyAsync",
			method = RequestMethod.POST)
	public String competencyCreateAsync(Model model, HttpServletRequest request,
			@PathVariable("profileId") Long profileId,
			@ModelAttribute("competency") @Valid Competency recievedCompetency,
			BindingResult bindingResult) {
		if (recievedCompetency == null) {
			throw new SessionExpiredException(null);
		}
		if (recievedCompetency.getLevel() > 
			recievedCompetency.getSkill().getMaxLevel()) {
			bindingResult.rejectValue("level", "error.skillLevel",
					"Skill's largest possible level is " + 
							recievedCompetency.getSkill().getMaxLevel());
		}
		boolean create = (recievedCompetency.getId() == 0);
		Profile profile = facade.getProfileDAO().getOne(profileId);
		if (profile
				.getCompetencies()
				.stream()
				.anyMatch(x -> x.getSkill().equals(recievedCompetency.getSkill()))) {
			bindingResult.rejectValue("skill", "error.skill",
					"Such skill is already used.");
		}
		model.addAttribute("skillLevels", EditorRequirement.getSkillLevels(
				facade.getSkillDAO().getAll()));
		if (bindingResult.hasErrors()) {
			model.addAttribute("skillsList", 
					SkillUtils.sortAndAddIndents(facade.getSkillDAO().getAll()));
			model.addAttribute("profile", profile);
			return ("fragments/editCompetencyModal :: competencyModalForm");
		}
		if (create) {
			profile.addCompetency(recievedCompetency);
		} else {
			Competency competency = profile
					.getCompetencies()
					.stream()
					.filter(x -> x.getId() == recievedCompetency.getId())
					.findFirst()
					.orElse(null);
			if (competency == null) {
				throw new DependentEntityNotFoundException(
						Profile.class,
						Competency.class,
						profileId,
						recievedCompetency.getId(),
						request.getPathInfo());
			}
			competency.setSkill(recievedCompetency.getSkill());
			competency.setLevel(recievedCompetency.getLevel());
		}
		facade.getProfileDAO().update(profile);
		return "fragments/editCompetencyModal :: competencyCreationSuccess";
	}
	
	@RequestMapping(path = "/{profileId}/competency",
			method = RequestMethod.GET)
	public String competency(Model model, HttpServletRequest request,
			@RequestParam(name = "id", required = false) Long id,
			@PathVariable("profileId") Long profileId) {
		model.addAttribute("_VIEW_TITLE", "title.edit.competency");
		model.addAttribute("_FORCE_CSRF", true);
		Profile profile = facade.getProfileDAO().getOne(profileId);
		Competency competency;
		boolean create;
		if (id == null) {
			competency = new Competency();
			create = true;
		} else {
			competency = profile
					.getCompetencies()
					.stream()
					.filter(x -> x.getId() == id)
					.findFirst()
					.orElse(null);
			if (competency == null) {
				throw new DependentEntityNotFoundException(
						Profile.class,
						Competency.class,
						profileId,
						id,
						request.getPathInfo());
			}
			create = false;
		}
		model.addAttribute("competency", competency);
		model.addAttribute("create", create);
		model.addAttribute("skillsList", 
				SkillUtils.sortAndAddIndents(facade.getSkillDAO().getAll()));
		model.addAttribute("postAddress", "/editor/" + profileId + "/competency");
		return "editCompetency";
	}
	
	@RequestMapping(path = "/{profileId}/competency",
			method = RequestMethod.POST)
	public String competencyCreate(Model model, HttpServletRequest request,
			@PathVariable("profileId") Long profileId,
			@ModelAttribute("competency") @Valid Competency recievedCompetency,
			BindingResult bindingResult) {
		if (recievedCompetency == null) {
			throw new SessionExpiredException(null);
		}
		if (recievedCompetency.getLevel() > 
			recievedCompetency.getSkill().getMaxLevel()) {
			bindingResult.rejectValue("level", "error.skillLevel",
					"Skill's largest possible level is " + 
							recievedCompetency.getSkill().getMaxLevel());
		}
		boolean create = (recievedCompetency.getId() == 0);
		if (bindingResult.hasErrors()) {
			model.addAttribute("create", create);
			model.addAttribute("skillsList", 
					SkillUtils.sortAndAddIndents(facade.getSkillDAO().getAll()));
			model.addAttribute("postAddress", "/editor/" + profileId + "/competency");
			model.addAttribute("_VIEW_TITLE", "title.edit.competency");
			model.addAttribute("_FORCE_CSRF", true);
			return ("editCompetency");
		}
		Profile profile = facade.getProfileDAO().getOne(profileId);
		if (create) {
			profile.addCompetency(recievedCompetency);
		} else {
			Competency competency = profile
					.getCompetencies()
					.stream()
					.filter(x -> x.getId() == recievedCompetency.getId())
					.findFirst()
					.orElse(null);
			if (competency == null) {
				throw new DependentEntityNotFoundException(
						Profile.class,
						Competency.class,
						profileId,
						recievedCompetency.getId(),
						request.getPathInfo());
			}
			competency.setSkill(recievedCompetency.getSkill());
			competency.setLevel(recievedCompetency.getLevel());
		}
		facade.getProfileDAO().update(profile);
		return "redirect:/viewer/profile?id=" + profile.getId();
	}
	
	@RequestMapping(path = "/competency/delete", 
			method = RequestMethod.GET)
	public String competencyDelete(Model model, HttpServletRequest request,
			@RequestParam(name = "id", required = true) Long id,
			@RequestParam(name = "profileId", required = true) Long profileId) {
		Profile profile = facade.getProfileDAO().getOne(profileId);
		Competency competency = profile
				.getCompetencies()
				.stream()
				.filter(x -> x.getId() == id)
				.findFirst()
				.orElse(null);
		if (competency == null) {
			throw new DependentEntityNotFoundException(
					Profile.class,
					Competency.class,
					profileId,
					id,
					request.getPathInfo());
		}
		profile.removeCompetency(competency);
		facade.getProfileDAO().update(profile);
		return "redirect:/viewer/profile?id=" + profile.getId();
	}
	
}
