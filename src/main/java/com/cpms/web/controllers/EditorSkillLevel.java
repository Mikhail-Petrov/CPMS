package com.cpms.web.controllers;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.SkillLevel;
import com.cpms.exceptions.AccessDeniedException;
import com.cpms.exceptions.DependentEntityNotFoundException;
import com.cpms.exceptions.SessionExpiredException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.RoleTypes;
import com.cpms.security.entities.Users;

/**
 * Handles skill level CRUD web application requests.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/editor")
public class EditorSkillLevel {
	
	@Autowired
	@Qualifier(value = "facade")
	private ICPMSFacade facade;
	
	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;
	
	private void checkBelongs(Principal principal, Skill parentSkill,
			HttpServletRequest request) {
		if (parentSkill != null) {
			if (CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT)) {
				Users owner = userDAO.getByUsername((
						(UsernamePasswordAuthenticationToken)principal
						).getName());
				if (parentSkill.getOwner() == null ||
						owner.getId() != parentSkill.getOwner().longValue()) {// ||
						//!parentSkill.isDraft()) {
					throw new AccessDeniedException(
							"You are not allowed to edit this skill.", null);
				}
			}
		}
	}
	
	@RequestMapping(path = "/{skillId}/skillLevel", 
			method = RequestMethod.GET)
	public String skillLevel(Model model,
			@RequestParam(name = "level", required = true) Integer level,
			@PathVariable("skillId") Long skillId,
			Principal principal, HttpServletRequest request) {
		model.addAttribute("_VIEW_TITLE", "title.edit.levelskill");
		Skill skill = facade.getSkillDAO().getOne(skillId);
		checkBelongs(principal, skill, request);
		SkillLevel skillLevel = skill.getLevels().stream()
				.filter(x -> x.getLevel() == level).findFirst().orElse(null);
		boolean create;
		if (skillLevel == null) {
			skillLevel = new SkillLevel();
			skillLevel.setLevel(level);
			skillLevel.setId(0);
			skillLevel.setSkill(skill);
			create = true;
		} else {
			create = false;
		}
		model.addAttribute("skillId", skillId);
		model.addAttribute("skillLevel", skillLevel);
		model.addAttribute("create", create);
		return "editSkillLevel";
	}
	
	@RequestMapping(path = "/{skillId}/skillLevel", 
			method = RequestMethod.POST)
	public String skillLevelCreate(Model model, HttpServletRequest request,
			@PathVariable("skillId") Long skillId, Principal principal,
			@ModelAttribute("skillLevel") @Valid SkillLevel skillLevel,
			BindingResult bindingResult) {
		if (skillLevel == null) {
			throw new SessionExpiredException(null);
		}
		boolean create = (skillLevel.getId() == 0);
		if (bindingResult.hasErrors()) {
			model.addAttribute("skillId", skillId);
			model.addAttribute("create", create);
			model.addAttribute("_VIEW_TITLE", "title.edit.levelskill");
			return ("editSkillLevel");
		}
		Skill skill = facade.getSkillDAO().getOne(skillId);
		checkBelongs(principal, skill, request);
		if (create) {
			skill.addLevel(skillLevel);
		} else {
			SkillLevel skillLevelOld = skill.getLevels().stream()
					.filter(x -> x.getLevel() == skillLevel.getLevel())
					.findFirst().orElse(null);
			if (skillLevelOld == null) {
				throw new DependentEntityNotFoundException(
						Skill.class,
						SkillLevel.class,
						skill.getId(),
						skillLevel.getLevel(),
						request.getPathInfo());
			}
			skillLevelOld.setAbout(skillLevel.getAbout());
		}
		facade.getSkillDAO().update(skill);
		//return "redirect:/viewer/tree";
		return "redirect:/viewer";
	}

}
