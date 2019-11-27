package com.cpms.web.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.cpms.data.entities.Competency;
import com.cpms.exceptions.SessionExpiredException;
import com.cpms.exceptions.WrongIndexException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.web.SkillUtils;
import com.cpms.web.UserSessionData;

/**
 * Handles competency editing in dashboard.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/dashboard/competency")
public class DashboardCompetencyEditor {
	
	@Autowired
	@Qualifier(value = "facade")
	private ICPMSFacade facade;
	
	@Autowired
	@Qualifier(value = "userSessionData")
	private UserSessionData sessionData;

    @Autowired
    private MessageSource messageSource;
	
	@RequestMapping(path = "/add", 
			method = RequestMethod.GET)
	public String competency(Model model) {
		model.addAttribute("_VIEW_TITLE", "title.edit.competency");
		model.addAttribute("_FORCE_CSRF", true);
		Competency competency = new Competency();
		model.addAttribute("competency", competency);
		model.addAttribute("create", true);
		model.addAttribute("skillsList", 
				SkillUtils.sortAndAddIndents(Skills.sortSkills(facade.getSkillDAO().getAll())));
		model.addAttribute("postAddress", "/dashboard/competency/add");
		return "editCompetency";
	}
	
	@RequestMapping(path = "/add", 
			method = RequestMethod.POST)
	public String competencyCreate(Model model,
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
		synchronized (sessionData.getCompetencies()) {
			if (sessionData
					.getCompetencies()
					.stream()
					.anyMatch(x -> x.getSkill().equals(recievedCompetency.getSkill()))) {
				bindingResult.rejectValue("skill", "error.skill",
						"Such skill is already used.");
			}
			if (bindingResult.hasErrors()) {
				model.addAttribute("create", true);
				model.addAttribute("skillsList", 
						SkillUtils.sortAndAddIndents(Skills.sortSkills(facade.getSkillDAO().getAll())));
				model.addAttribute("postAddress", "/dashboard/competency/add");
				model.addAttribute("_VIEW_TITLE", "title.edit.competency");
				model.addAttribute("_FORCE_CSRF", true);
				return ("editCompetency");
			}
			sessionData.addCompetency(recievedCompetency);
		}
		return "redirect:/dashboard";
	}	
	
	@RequestMapping(path = "/addAsync", 
			method = RequestMethod.POST)
	public String competencyCreateAsync(Model model, HttpServletRequest request,
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
		synchronized (sessionData.getCompetencies()) {
			if (sessionData
					.getCompetencies()
					.stream()
					.anyMatch(x -> x.getSkill().equals(recievedCompetency.getSkill()))) {
				bindingResult.rejectValue("skill", "error.skill",
						"Such skill is already used.");
			}
			if (bindingResult.hasErrors()) {
				model.addAttribute("skillsList", 
						SkillUtils.sortAndAddIndents(Skills.sortSkills(facade.getSkillDAO().getAll())));
				return ("fragments/editCompetencyModal :: competencyDashboardModalForm");
			}
			sessionData.addCompetency(recievedCompetency);
		}
		return ("fragments/editCompetencyModal :: competencyCreationSuccess");
	}	
	
	@RequestMapping(path = "/remove", 
			method = RequestMethod.GET)
	public String competencyDelete(HttpServletRequest request,
			@RequestParam(name = "index", required = true) int index) {
		synchronized (sessionData.getCompetencies()) {
			if (index < 0 || index >= sessionData.getCompetencies().size()) {
				throw new WrongIndexException(sessionData.getCompetencies().size(),
						index + 1,
						request.getPathInfo(), messageSource);
			}
			sessionData.getCompetencies().remove(index);
		}
		return "redirect:/dashboard";
	}
	
}
