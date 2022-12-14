package com.cpms.web.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.cpms.dao.interfaces.IDraftableSkillDaoExtension;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.SkillLevel;
import com.cpms.exceptions.DataAccessException;
import com.cpms.exceptions.DependentEntityNotFoundException;
import com.cpms.exceptions.SessionExpiredException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.web.SkillUtils;
import com.cpms.web.UserSessionData;

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

    @Autowired
    private MessageSource messageSource;

	@Autowired
	@Qualifier("draftableSkillDAO")
	private IDraftableSkillDaoExtension skillDao;
	
	@RequestMapping(path = "/{profileId}/competencyAsyncNew",
			method = RequestMethod.POST)
	public String competencyCreateAsyncNew(Model model, HttpServletRequest request,
			@PathVariable("profileId") Long profileId,
			@ModelAttribute("competency") @Valid Competency recievedCompetency,
			BindingResult bindingResult) {
		if (recievedCompetency == null) {
			throw new SessionExpiredException(null, messageSource);
		}
		Profile profile = facade.getProfileDAO().getOne(profileId);
		for (String compID : recievedCompetency.getSkillIDs().split(",")) {
			long skillID = 0;
			try {
				skillID = Long.parseLong(compID);
			} catch(NumberFormatException e) {
				continue;
			}
			Skill skill = facade.getSkillDAO().getOne(skillID);
			if (skill == null) continue;
			if (!profile
					.getCompetencies()
					.stream()
					.anyMatch(x -> x.getSkill().equals(skill))) {
				//int level = skill.getMaxLevel();
				int level = recievedCompetency.getLevel();
				if (level < 1) level = 1;
				if (level > skill.getMaxLevel()) level = skill.getMaxLevel();
				profile.addCompetency(new Competency(skill, level));
			}
		}
		facade.getProfileDAO().update(profile);
		return "fragments/editCompetencyModal :: competencyCreationSuccess";
	}
	
	@RequestMapping(path = "/{profileId}/competencyAsync",
			method = RequestMethod.POST)
	public String competencyCreateAsync(Model model, HttpServletRequest request,
			@PathVariable("profileId") Long profileId,
			@ModelAttribute("competency") @Valid Competency recievedCompetency,
			BindingResult bindingResult) {
		if (recievedCompetency == null) {
			throw new SessionExpiredException(null, messageSource);
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
		if (bindingResult.hasErrors()) {
			model.addAttribute("skillsList", 
					SkillUtils.sortAndAddIndents(Skills.sortSkills(Skills.getAllSkills(facade.getSkillDAO())), skillDao));
			model.addAttribute("skillLevels", SkillLevel.getSkillLevels(Skills.getAllSkills(facade.getSkillDAO())));
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
						request.getPathInfo(),
						messageSource);
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
						request.getPathInfo(),
						messageSource);
			}
			create = false;
		}
		model.addAttribute("competencySkill", create ? 0 : competency.getSkill().getId());
		competency.setSkill(null);
		model.addAttribute("competency", competency);
		model.addAttribute("create", create);
		List<Skill> allSkills = Skills.getAllSkills(facade.getSkillDAO());
		model.addAttribute("skillsList", 
				SkillUtils.sortAndAddIndents(Skills.sortSkills(allSkills), skillDao));
		model.addAttribute("skillLevels", SkillLevel.getSkillLevels(allSkills));
		model.addAttribute("postAddress", "/editor/" + profileId + "/competency");
		model.addAttribute("skillsAndParents", Skills.getSkillsAndParents(allSkills));
		return "editCompetency";
	}
	
	@RequestMapping(path = "/{profileId}/competency",
			method = RequestMethod.POST)
	public String competencyCreate(Model model, HttpServletRequest request,
			@PathVariable("profileId") Long profileId,
			@ModelAttribute("competency") @Valid Competency recievedCompetency,
			BindingResult bindingResult) {
		if (recievedCompetency == null) {
			throw new SessionExpiredException(null, messageSource);
		}
		if (recievedCompetency.getLevel() > 
			recievedCompetency.getSkill().getMaxLevel()) {
			bindingResult.rejectValue("level", "error.skillLevel",
					"Skill's largest possible level is " + 
							recievedCompetency.getSkill().getMaxLevel());
		}
		Profile profile = facade.getProfileDAO().getOne(profileId);
		Competency oldCompetency = profile
				.getCompetencies()
				.stream()
				.filter(x -> x.getId() == recievedCompetency.getId())
				.findFirst()
				.orElse(null);
		Skill oldCompetencySkill = oldCompetency == null ? null : oldCompetency.getSkill();
		if (profile
				.getCompetencies()
				.stream()
				.anyMatch(x -> x.getSkill().equals(recievedCompetency.getSkill())
						&& !x.getSkill().equals(oldCompetencySkill))) {
			//bindingResult.rejectValue("skill", "error.skill",
					//"Such skill is already used.");
			throw new DataAccessException(UserSessionData.localizeText("exception.DataAcces.competency.explanation", messageSource));
		}
		boolean create = (recievedCompetency.getId() == 0);
		if (bindingResult.hasErrors()) {
			model.addAttribute("create", create);
			model.addAttribute("skillLevels", SkillLevel.getSkillLevels(Skills.getAllSkills(facade.getSkillDAO())));
			model.addAttribute("skillsList", 
					SkillUtils.sortAndAddIndents(Skills.sortSkills(Skills.getAllSkills(facade.getSkillDAO())), skillDao));
			model.addAttribute("postAddress", "/editor/" + profileId + "/competency");
			model.addAttribute("_VIEW_TITLE", "title.edit.competency");
			model.addAttribute("_FORCE_CSRF", true);
			return ("editCompetency");
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
						request.getPathInfo(),
						messageSource);
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
					request.getPathInfo(),
					messageSource);
		}
		profile.removeCompetency(competency);
		facade.getProfileDAO().update(profile);
		return "redirect:/viewer/profile?id=" + profile.getId();
	}
	
}
