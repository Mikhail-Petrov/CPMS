package com.cpms.web.controllers;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.cpms.data.entities.Company;
import com.cpms.data.entities.Competencies;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Profile;
import com.cpms.exceptions.DataAccessException;
import com.cpms.exceptions.DependentEntityNotFoundException;
import com.cpms.exceptions.SessionExpiredException;
import com.cpms.facade.ICPMSFacade;

/**
 * Handles profile CRUD web application requests.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/editor")
public class EditorProfile {
	
	@Autowired
	@Qualifier(value = "facade")
	private ICPMSFacade facade;

	@RequestMapping(path = "/profile", 
			method = RequestMethod.GET)
	public String profile(Model model,
			@RequestParam(name = "id", required = false) Integer id) {
		model.addAttribute("_VIEW_TITLE", "title.edit.profile");
		Profile profile;
		boolean create;
		if (id == null) {
			profile = new Company();
			create = true;
		} else {
			profile = facade.getProfileDAO().getOne(id);
			if (!profile.getEntityClass().equals(Profile.class)) {
				throw new DataAccessException("Wrong subject type!", null);
			}
			create = false;
		}
		model.addAttribute("company", profile.clone());
		model.addAttribute("create", create);
		return "editProfile";
	}
	
	@RequestMapping(path = {"/profile"}, 
			method = RequestMethod.POST)
	public String profileCreate(Model model,
			@ModelAttribute("company") @Valid Company company,
			BindingResult bindingResult) {
		if (company == null) {
			throw new SessionExpiredException(null);
		}
		boolean create = (company.getId() == 0);
		if (bindingResult.hasErrors()) {
			model.addAttribute("create", create);
			model.addAttribute("_VIEW_TITLE", "title.edit.profile");
			return ("editProfile");
		}
		Profile profile;
		if (create) {
			profile = new Company();
			profile.update(company);
			profile = facade.getProfileDAO().insert(profile);
		} else {
			profile = facade.getProfileDAO().getOne(company.getId());
			profile.update(company);
			profile = facade.getProfileDAO().update(profile);
		}
		return "redirect:/viewer/profile?id=" + profile.getId();
	}
	
	@RequestMapping(path = {"/profileAsync"}, 
			method = RequestMethod.POST)
	public String profileCreateAsync(Model model,
			@ModelAttribute("company") @Valid Company company,
			BindingResult bindingResult) {
		if (company == null) {
			throw new SessionExpiredException(null);
		}
		boolean create = (company.getId() == 0);
		if (bindingResult.hasErrors()) {
			return ("fragments/editProfileModal :: profileModalForm");
		}
		Profile profile;
		if (create) {
			profile = new Company();
			profile.update(company);
			profile = facade.getProfileDAO().insert(profile);
		} else {
			profile = facade.getProfileDAO().getOne(company.getId());
			profile.update(company);
			profile = facade.getProfileDAO().update(profile);
		}
		return "fragments/editProfileModal :: profileCreationSuccess";
	}
	
	@RequestMapping(path = {"/profile/delete"}, 
			method = RequestMethod.GET)
	public String profileDelete(Model model,
			@RequestParam(name = "id", required = true) Long id) {
		Profile profile = facade.getProfileDAO().getOne(id);
		facade.getProfileDAO().delete(profile);
		return "redirect:/viewer";
	}

	@RequestMapping(path = {"/profile/saveChanges"}, 
			method = RequestMethod.POST)
	public String profileSave(Model model, HttpServletRequest request,
			@ModelAttribute("competencies") @Valid Competencies competencies,
			BindingResult bindingResult) {
		Profile profile = facade.getProfileDAO().getOne(competencies.getProfileId());
		boolean change = false;
		
		HashMap<Long,Integer> changes = competencies.getChanges();
		for (Entry<Long, Integer> compChange : changes.entrySet()) {
			Competency competency = profile
					.getCompetencies()
					.stream()
					.filter(x -> x.getId() == compChange.getKey())
					.findFirst()
					.orElse(null);
			if (competency == null) {
				throw new DependentEntityNotFoundException(
						Profile.class,
						Competency.class,
						profile.getId(),
						compChange.getKey(),
						request.getPathInfo());
			}
			if (competency.getLevel() != compChange.getValue()) {
				competency.setLevel(compChange.getValue());
				change = true;
			}
		}
		
		if (change)
			facade.getProfileDAO().update(profile);
		return "redirect:/viewer/profile?id=" + profile.getId();
	}
}
