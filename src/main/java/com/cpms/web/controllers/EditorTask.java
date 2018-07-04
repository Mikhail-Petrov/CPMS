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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.cpms.data.entities.Competencies;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Requirements;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TaskRequirement;
import com.cpms.exceptions.DependentEntityNotFoundException;
import com.cpms.exceptions.SessionExpiredException;
import com.cpms.facade.ICPMSFacade;

/**
 * Handles task CRUD web application requests.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/editor")
public class EditorTask {

	@Autowired
	@Qualifier(value = "facade")
	private ICPMSFacade facade;
	
	@RequestMapping(path = "/{taskId}/requirementAsync",
			method = RequestMethod.POST)
	public String competencyCreateAsync(Model model, HttpServletRequest request,
			@PathVariable("taskId") Long taskId,
			@ModelAttribute("requirement") @Valid TaskRequirement recievedRequirement,
			BindingResult bindingResult) {
		if (recievedRequirement == null) {
			throw new SessionExpiredException(null);
		}
		if (recievedRequirement.getLevel() > 
			recievedRequirement.getSkill().getMaxLevel()) {
			bindingResult.rejectValue("level", "error.skillLevel",
					"Skill's largest possible level is " + 
							recievedRequirement.getSkill().getMaxLevel());
		}
		Task task = facade.getTaskDAO().getOne(taskId);
		if (task
				.getRequirements()
				.stream()
				.anyMatch(x -> x.getSkill().equals(recievedRequirement.getSkill()))) {
			bindingResult.rejectValue("skill", "error.skill",
					"Such skill is already used.");
		}
		boolean create = (recievedRequirement.getId() == 0);
		if (bindingResult.hasErrors()) {
			return ("fragments/editCompetencyModal :: competencyModalForm");
		}
		if (create) {
			task.addRequirement(recievedRequirement);
		} else {
			TaskRequirement requirement = task
					.getRequirements()
					.stream()
					.filter(x -> x.getId() == recievedRequirement.getId())
					.findFirst()
					.orElse(null);
			if (requirement == null) {
				throw new DependentEntityNotFoundException(
						Profile.class,
						Competency.class,
						taskId,
						recievedRequirement.getId(),
						request.getPathInfo());
			}
			requirement.setSkill(recievedRequirement.getSkill());
			requirement.setLevel(recievedRequirement.getLevel());
		}
		facade.getTaskDAO().update(task);
		return "fragments/editRequirementModal :: requirementCreationSuccess";
	}
	
	@RequestMapping(path = "/task", 
			method = RequestMethod.GET)
	public String task(Model model,
			@RequestParam(name = "id", required = false) Long id) {
		model.addAttribute("_VIEW_TITLE", "title.edit.task");
		Task task;
		boolean create;
		if (id == null) {
			task = new Task();
			create = true;
		} else {
			task = facade.getTaskDAO().getOne(id);
			create = false;
		}
		model.addAttribute("task", task);
		model.addAttribute("create", create);
		return "editTask";
	}
	
	@RequestMapping(path = "/task", 
			method = RequestMethod.POST)
	public String taskCreate(Model model,
			@ModelAttribute("task") @Valid Task recievedTask,
			BindingResult bindingResult) {
		if (recievedTask == null) {
			throw new SessionExpiredException(null);
		}
		boolean create = (recievedTask.getId() == 0);
		if (bindingResult.hasErrors()) {
			model.addAttribute("create", create);
			model.addAttribute("_VIEW_TITLE", "title.edit.task");
			return ("editTask");
		}
		Task task;
		if (create) {
			task = facade.getTaskDAO().insert(recievedTask);
		} else {
			task = facade.getTaskDAO().getOne(recievedTask.getId());
			task.setAbout(recievedTask.getAbout());
			task.setName(recievedTask.getName());
			task.setAbout_RU(recievedTask.getAbout_RU());
			task.setName_RU(recievedTask.getName_RU());
			task = facade.getTaskDAO().update(task);
		}
		return "redirect:/viewer/task?id=" + task.getId();
	}
	
	@RequestMapping(path = "/taskAsync", 
			method = RequestMethod.POST)
	public String taskCreateAsync(Model model,
			@ModelAttribute("task") @Valid Task recievedTask,
			BindingResult bindingResult) {
		if (recievedTask == null) {
			throw new SessionExpiredException(null);
		}
		boolean create = (recievedTask.getId() == 0);
		if (bindingResult.hasErrors()) {
			return ("fragments/editTaskModal :: taskModalForm");
		}
		Task task;
		if (create) {
			task = facade.getTaskDAO().insert(recievedTask);
		} else {
			task = facade.getTaskDAO().getOne(recievedTask.getId());
			task.setAbout(recievedTask.getAbout());
			task.setName(recievedTask.getName());
			task.setAbout_RU(recievedTask.getAbout_RU());
			task.setName_RU(recievedTask.getName_RU());
			task = facade.getTaskDAO().update(task);
		}
		return "fragments/editTaskModal :: taskCreationSuccess";
	}
	
	@RequestMapping(path = "/task/delete", 
			method = RequestMethod.GET)
	public String taskDelete(Model model,
			@RequestParam(name = "id", required = true) Long id) {
		Task task = facade.getTaskDAO().getOne(id);
		facade.getTaskDAO().delete(task);
		return "redirect:/viewer/tasks";
	}

	@RequestMapping(path = {"/task/saveChanges"}, 
			method = RequestMethod.POST)
	public String profileSave(Model model, HttpServletRequest request,
			@ModelAttribute("requirements") @Valid Requirements requirements,
			BindingResult bindingResult) {
		Task task = facade.getTaskDAO().getOne(requirements.getTaskId());
		boolean change = false;
		
		HashMap<Long,Integer> changes = requirements.getChanges();
		for (Entry<Long, Integer> requirChange : changes.entrySet()) {
			TaskRequirement requirement = task
					.getRequirements()
					.stream()
					.filter(x -> x.getId() == requirChange.getKey())
					.findFirst()
					.orElse(null);
			if (requirement == null) {
				throw new DependentEntityNotFoundException(
						Task.class,
						TaskRequirement.class,
						task.getId(),
						requirChange.getKey(),
						request.getPathInfo());
			}
			if (requirement.getLevel() != requirChange.getValue()) {
				requirement.setLevel(requirChange.getValue());
				change = true;
			}
		}
		
		if (change)
			facade.getTaskDAO().update(task);
		return "redirect:/viewer/task?id=" + task.getId();
	}
}
