package com.cpms.web.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import java.util.ArrayList;
import java.util.List;
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
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.SkillLevel;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TaskRequirement;
import com.cpms.exceptions.DataAccessException;
import com.cpms.exceptions.DependentEntityNotFoundException;
import com.cpms.exceptions.SessionExpiredException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.web.SkillUtils;
import com.cpms.web.UserSessionData;

/**
 * Handles task requirement CRUD web application requests.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/editor")
public class EditorRequirement {
	
	@Autowired
	@Qualifier(value = "facade")
	private ICPMSFacade facade;

    @Autowired
    private MessageSource messageSource;

	@Autowired
	@Qualifier("draftableSkillDAO")
	private IDraftableSkillDaoExtension skillDao;

	@RequestMapping(path = "/{taskId}/requirement", 
			method = RequestMethod.GET)
	public String requirement(Model model, HttpServletRequest request,
			@PathVariable("taskId") Long taskId,
			@RequestParam(name = "id", required = false) Long id) {
		model.addAttribute("_VIEW_TITLE", "title.edit.requirement");
		model.addAttribute("_FORCE_CSRF", true);
		Task task = facade.getTaskDAO().getOne(taskId);
		List<String> requirements = new ArrayList<String>();
		for (TaskRequirement req : task.getRequirements())
			if (req.getSkill().getDelDate() == null)
				requirements.add(req.getSkill().getPresentationName());
		model.addAttribute("profileCompetencies", requirements);
		TaskRequirement requirement;
		boolean create;
		if (id == null) {
			requirement = new TaskRequirement();
			create = true;
		} else {
			requirement = task
					.getRequirements()
					.stream()
					.filter(x -> x.getId() == id)
					.findFirst()
					.orElse(null);
			if (requirement == null) {
				throw new DependentEntityNotFoundException(
						Task.class,
						TaskRequirement.class,
						taskId,
						id,
						request.getPathInfo(),
						messageSource);
			}
			create = false;
		}
		model.addAttribute("taskId", taskId);
		model.addAttribute("requirementSkill", create ? 0 : requirement.getSkill().getId());
		requirement.setSkill(null);
		model.addAttribute("requirement", requirement);
		List<Skill> allSkills = Skills.getAllSkills(facade.getSkillDAO());
		model.addAttribute("skillsList", 
				SkillUtils.sortAndAddIndents(Skills.sortSkills(allSkills), skillDao));
		model.addAttribute("create", create);
		model.addAttribute("skillLevels", SkillLevel.getSkillLevels(allSkills));
		model.addAttribute("skillsAndParents", Skills.getSkillsAndParents(allSkills));
		return "editRequirement";
	}
	
	@RequestMapping(path = "/{taskId}/requirement",
			method = RequestMethod.POST)
	public String requirementCreate(Model model, HttpServletRequest request,
			@PathVariable("taskId") Long taskId,
			@ModelAttribute("requirement") @Valid TaskRequirement recievedRequirement,
			BindingResult bindingResult) {
		if (recievedRequirement == null) {
			throw new SessionExpiredException(null, messageSource);
		}
		if (recievedRequirement.getLevel() >
				recievedRequirement.getSkill().getMaxLevel()) {
			bindingResult.rejectValue("level", "error.requirement",
					"Skill's largest possible level is " + 
							recievedRequirement.getSkill().getMaxLevel());
		}
		Task task = facade.getTaskDAO().getOne(taskId);
		TaskRequirement oldRequirement = task
				.getRequirements()
				.stream()
				.filter(x -> x.getId() == recievedRequirement.getId())
				.findFirst()
				.orElse(null);
		Skill oldRequirementSkill = oldRequirement == null ? null : oldRequirement.getSkill();
		if (task
				.getRequirements()
				.stream()
				.anyMatch(x -> x.getSkill().equals(recievedRequirement.getSkill())
						&& !x.getSkill().equals(oldRequirementSkill))) {
	//		bindingResult.rejectValue("skill", "error.skill",
//					"Such skill is already used.");
			throw new DataAccessException(UserSessionData.localizeText("exception.DataAcces.requirement.explanation", messageSource));
		}
		boolean create = (recievedRequirement.getId() == 0);
		if (bindingResult.hasErrors()) {
			model.addAttribute("_VIEW_TITLE", "title.edit.requirement");
			model.addAttribute("_FORCE_CSRF", true);
			model.addAttribute("taskId", taskId);
			model.addAttribute("create", create);
			model.addAttribute("skillsList", Skills.getAllSkills(facade.getSkillDAO()));
			return ("editRequirement");
		}
		TaskRequirement requirement;
		if (create) {
			task.addRequirement(recievedRequirement);
		} else {
			requirement = task
					.getRequirements()
					.stream()
					.filter(x -> x.getId() == recievedRequirement.getId())
					.findFirst()
					.orElse(null);
			if (requirement == null) {
				throw new DependentEntityNotFoundException(
						Task.class,
						TaskRequirement.class,
						taskId,
						recievedRequirement.getId(),
						request.getPathInfo(),
						messageSource);
			}
			requirement.setSkill(recievedRequirement.getSkill());
			requirement.setLevel(recievedRequirement.getLevel());
		}
		task = facade.getTaskDAO().update(task);
		return "redirect:/viewer/task?id=" + task.getId();
	}
	
	@RequestMapping(path = "/requirement/delete", 
			method = RequestMethod.GET)
	public String requirementDelete(Model model, HttpServletRequest request,
			@RequestParam(name = "id", required = true) Long id,
			@RequestParam(name = "taskId", required = true) Long taskId) {
		Task task = facade.getTaskDAO().getOne(taskId);
		TaskRequirement requirement = task
				.getRequirements()
				.stream()
				.filter(x -> x.getId() == id)
				.findFirst()
				.orElse(null);
		if (requirement == null) {
			throw new DependentEntityNotFoundException(
					Task.class,
					TaskRequirement.class,
					taskId,
					id,
					request.getPathInfo(),
					messageSource);
		}
		task.removerRequirement(requirement);
		facade.getTaskDAO().update(task);
		return "redirect:/viewer/task?id=" + task.getId();
	}
	
}
