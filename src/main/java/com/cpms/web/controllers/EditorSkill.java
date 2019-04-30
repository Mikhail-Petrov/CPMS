package com.cpms.web.controllers;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cpms.dao.interfaces.IDraftableSkillDaoExtension;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.SkillLevel;
import com.cpms.data.entities.Task;
import com.cpms.exceptions.AccessDeniedException;
import com.cpms.exceptions.ManualValidationException;
import com.cpms.exceptions.SessionExpiredException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.RoleTypes;
import com.cpms.security.entities.Users;
import com.cpms.web.SkillPostForm;
import com.cpms.web.SkillUtils;
import com.cpms.web.UserSessionData;
import com.cpms.web.ajax.IAjaxAnswer;
import com.cpms.web.ajax.SkillAnswer;

/**
 * Handles skill CRUD web application requests.
 * Almost completely deprecated because of {@link SkillTree}.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/editor")
public class EditorSkill {

	@Autowired
	@Qualifier(value = "facade")
	private ICPMSFacade facade;
	
	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;
	
	@Autowired
	@Qualifier("draftableSkillDAO")
	private IDraftableSkillDaoExtension skillDao;
	
	private void addSkillsListToModel(Model model, Principal principal,
			HttpServletRequest request, boolean create) {
		model.addAttribute("create", create);
		if (CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT)) {
			Users owner = userDAO.getByUsername((
					(UsernamePasswordAuthenticationToken)principal
					).getName());
			List<Skill> skills = facade.getSkillDAO().getAll();
			skills.addAll(skillDao.getDraftsOfUser(owner.getId()));
			model.addAttribute("skillsList", SkillUtils.sortAndAddIndents(Skills.sortSkills(skills)));
		} else if (CommonModelAttributes.userHasRole(request, RoleTypes.MANAGER)) {
			List<Skill> skills = skillDao.getAllIncludingDrafts();
			model.addAttribute("skillsList", SkillUtils.sortAndAddIndents(Skills.sortSkills(skills)));
		} else {
			model.addAttribute("skillsList", 
					SkillUtils.sortAndAddIndents(Skills.sortSkills(facade.getSkillDAO().getAll())));
		}
	}
	
	private void checkBelongs(Principal principal, Skill recievedSkill,
			HttpServletRequest request) {
		if (recievedSkill.getId() != 0) {
			if (CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT)) {
				Skill oldSkill = facade.getSkillDAO().getOne(recievedSkill.getId());
				Users owner = userDAO.getByUsername((
						(UsernamePasswordAuthenticationToken)principal
						).getName());
				if (oldSkill.getOwner() == null ||
						owner.getId() != oldSkill.getOwner().longValue()) {// ||
						//!oldSkill.isDraft()) {
					throw new AccessDeniedException(UserSessionData.localizeText(
							"У вас недостаточно прав для редактирования этого умения.",
							"You are not allowed to edit this skill."), null);
				}
			}
		}
	}
	
	private void checkNotChildrenOfDraft(
			BindingResult bindingResult,
			Skill recievedSkill,
			HttpServletRequest request) {
		checkNotChildrenOfDraft(request, recievedSkill);
	}
	
	private void checkNotChildrenOfDraft(
			HttpServletRequest request,
			Skill recievedSkill) {
		if (recievedSkill.getParent() != null && 
				//recievedSkill.getParent().isDraft() &&
				CommonModelAttributes.userHasRole(request, RoleTypes.MANAGER)) {
			throw new ManualValidationException(UserSessionData.localizeText(
					"Отправлено некорректное умение", "Invalid skill submitted!"),
					UserSessionData.localizeText(
							"Неподтверждённое умение не может иметь дочерние умения, сперва необходимо подтвердить его.",
							"You can't create children of a draft skill, approve it's parent first."),
					null);
		}
	}
	
	@RequestMapping(path = "/skill", 
			method = RequestMethod.GET)
	public String skill(Model model, Principal principal,
			HttpServletRequest request,
			@RequestParam(name = "id", required = false) Long id) {
		model.addAttribute("_VIEW_TITLE", "title.edit.skill");
		Skill skill;
		boolean create;
		if (id == null) {
			skill = new Skill();
			skill.setId(0);
			create = true;
		} else {
			skill = facade.getSkillDAO().getOne(id);
			checkBelongs(principal, skill, request);
			create = false;
		}
		model.addAttribute("skill", skill);
		addSkillsListToModel(model, principal, request, create);
		return "editSkill";
	}
	
	@RequestMapping(path = "/skill", 
			method = RequestMethod.POST)
	public String skillCreate(Model model, Principal principal,
			@ModelAttribute("skill") @Valid Skill recievedSkill,
			HttpServletRequest request,
			BindingResult bindingResult) {
		if (recievedSkill == null) {
			throw new SessionExpiredException(null);
		}
		if (CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT)) {
			//if (!CommonModelAttributes.userHasRole(request, RoleTypes.MANAGER))
				//recievedSkill.setDraft(true);
			recievedSkill.setOwner(userDAO.getByUsername((
					(UsernamePasswordAuthenticationToken)principal
					).getName()).getId());
		}
		boolean create = (recievedSkill.getId() == 0);
		Skill skill;
		checkBelongs(principal, recievedSkill, request);
		checkNotChildrenOfDraft(bindingResult, recievedSkill, request);
		if (bindingResult.hasErrors()) {
			model.addAttribute("create", create);
			if (create) {
				recievedSkill.setParent(null);
			}
			addSkillsListToModel(model, principal, request, create);
			return ("editSkill");
		}
		if (create) {
			skill = facade.getSkillDAO().insert(recievedSkill);
		} else {
			skill = facade.getSkillDAO().getOne(recievedSkill.getId());
			skill.setAbout(recievedSkill.getAbout());
			skill.setParent(recievedSkill.getParent());
			skill.setName(recievedSkill.getName());
			skill.setMaxLevel(recievedSkill.getMaxLevel());
			skill.setType(recievedSkill.getType());
			skill = facade.getSkillDAO().update(skill);
		}
		//return "redirect:/viewer/tree";
		return "redirect:/skills";
	}
	
	@RequestMapping(path = {"/skill/delete"}, 
			method = RequestMethod.GET)
	public String skillDelete(Model model, Principal principal,
			HttpServletRequest request,
			@RequestParam(name = "id", required = true) Long id) {
		Skill skill = facade.getSkillDAO().getOne(id);
		checkBelongs(principal, skill, request);
		if (skill.getImplementers().size() > 0 || 
				skill.getImplementersTask().size() > 0) {
			List<Profile> profiles = skill
					.getImplementers()
					.stream()
					.map(x -> x.getOwner())
					.collect(Collectors.toList());
			List<Task> tasks = skill
					.getImplementersTask()
					.stream()
					.map(x -> x.getTask())
					.collect(Collectors.toList());
			model.addAttribute("_VIEW_TITLE", "title.delete.skill");
			model.addAttribute("skill", skill);
			model.addAttribute("profilesList", profiles);
			model.addAttribute("tasksList", tasks);
			return "skillDelete";
		}
		facade.getSkillDAO().delete(skill);
		//return "redirect:/viewer/tree";
		return "redirect:/skills";
	}
	
	@RequestMapping(path = {"/skill/delete/force"}, 
			method = RequestMethod.GET)
	public String skillDeleteForce(Principal principal,
			HttpServletRequest request,
			@RequestParam(name = "id", required = true) Long id) {
		Skill skill = facade.getSkillDAO().getOne(id);
		checkBelongs(principal, skill, request);
		skill
			.getImplementers()
			.forEach(x -> {
				Profile owner = x.getOwner();
				owner.removeCompetency(x);
				x.setSkill(null);
				facade.getProfileDAO().update(owner);
			});
		skill
			.getImplementersTask()
			.forEach(x -> {
				Task owner = x.getTask();
				owner.removerRequirement(x);
				x.setSkill(null);
				facade.getTaskDAO().update(owner);
			});
		facade.getSkillDAO().delete(skill);
		//return "redirect:/viewer/tree";
		return "redirect:/skills";
	}
	
	@RequestMapping(path = "/skill/alternative", 
			method = RequestMethod.POST)
	public String skillCreateAlternative(Model model,
			@ModelAttribute SkillPostForm recievedSkill,
			HttpServletRequest request,
			Principal principal) {
		boolean longEnough = true;
		for(SkillLevel level : recievedSkill.getLevels()) {
			longEnough = longEnough 
					&& (level.getAbout().length() >= 0 && level.getAbout().length() <= 1000);
		}
		if (!longEnough) {
			throw new ManualValidationException("Invalid skill submitted!",
					"One of skill levels is shorter than 10 or longer than 1000 symbols.",
					request.getPathInfo());
		}
		if (recievedSkill.getName() != null && recievedSkill.getName().length() < 5) {
			recievedSkill.setName(null);
		}
		if (recievedSkill.getName() == null) {
			throw new ManualValidationException("Invalid skill submitted!",
					"Please fill in at least one name for a skill.",
					request.getPathInfo()); 
		}
		if ((recievedSkill.getName() != null && recievedSkill.getName().length() > 100)) {
			throw new ManualValidationException("Invalid skill submitted!",
					"Skill's name should not be longer than 100 symbols.",
					request.getPathInfo()); 
		}
		if (!((recievedSkill.getAbout().length() >= 0 && 
				recievedSkill.getAbout().length() <= 1000))) {
			throw new ManualValidationException("Invalid skill submitted!",
					"Description is longer than 1000 symbols.",
					request.getPathInfo()); 
		}
		Skill newSkill = new Skill();
		Long parentId = 0L;
		if (recievedSkill.getParent() != null && recievedSkill.getParent() != "")
			try {
				parentId = Long.parseLong(recievedSkill.getParent());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		Skill parent = facade.getSkillDAO().getOne(parentId);
		newSkill.setParent(parent);
		newSkill.setMaxLevel(recievedSkill.getMaxLevel());
		newSkill.setName(recievedSkill.getName());
		newSkill.setAbout(recievedSkill.getAbout());
		newSkill.setType(recievedSkill.getType());
		if (CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT)) {
			//if (!CommonModelAttributes.userHasRole(request, RoleTypes.MANAGER))
				//newSkill.setDraft(true);
			newSkill.setOwner(userDAO.getByUsername((
					(UsernamePasswordAuthenticationToken)principal
					).getName()).getId());
		}
		int levelIndex = 1;
		checkNotChildrenOfDraft(request, newSkill);
		for(SkillLevel level : recievedSkill.getLevels()) {
			SkillLevel newLevel = new SkillLevel();
			newLevel.setAbout(level.getAbout());
			newLevel.setLevel(levelIndex);
			levelIndex++;
			newSkill.addLevel(newLevel);
		}
		facade.getSkillDAO().insert(newSkill);
		//return "redirect:/viewer/tree";
		return "redirect:/skills";
	}
	
	@RequestMapping(path = "/skill/alternativeAsync", 
			method = RequestMethod.POST)
	public String skillCreateAlternativeAsync(Model model,
			@ModelAttribute SkillPostForm recievedSkill,
			HttpServletRequest request,
			Principal principal) {
		boolean longEnough = true;
		for(SkillLevel level : recievedSkill.getLevels()) {
			longEnough = longEnough 
					&& (level.getAbout().length() >= 0 && level.getAbout().length() <= 1000);
		}
		String invSkill = UserSessionData.localizeText(
				"Отправлен некорректный навык!", "Invalid skill submitted!");
		if (!longEnough) {
			throw new ManualValidationException(invSkill, UserSessionData.localizeText(
							"One of skill levels is shorter than 10 or longer than 1000 symbols."),
					request.getPathInfo());
		}
		if (recievedSkill.getName() != null && recievedSkill.getName().length() < 3) {
			recievedSkill.setName(null);
		}
		if (recievedSkill.getName() == null) {
			throw new ManualValidationException(invSkill, UserSessionData.localizeText(
							"Please fill in at least one name for a skill."),
					request.getPathInfo()); 
		}
		if ((recievedSkill.getName() != null && recievedSkill.getName().length() > 100)) {
			throw new ManualValidationException(invSkill, UserSessionData.localizeText(
							"Skill's name should not be longer than 100 symbols."),
					request.getPathInfo()); 
		}
		if (!((recievedSkill.getAbout().length() >= 0 && 
				recievedSkill.getAbout().length() <= 1000))) {
			throw new ManualValidationException(invSkill, UserSessionData.localizeText(
							"Description is longer than 1000 symbols."),
					request.getPathInfo()); 
		}
		Skill newSkill = new Skill();
		if (recievedSkill.getId() > 0) 
			newSkill = facade.getSkillDAO().getOne(recievedSkill.getId());
		Long parentId = 0L;
		if (recievedSkill.getParent() != null && recievedSkill.getParent() != "")
			try {
				parentId = Long.parseLong(recievedSkill.getParent());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		Skill parent = facade.getSkillDAO().getOne(parentId);
		newSkill.setParent(parent);
		newSkill.setMaxLevel(recievedSkill.getMaxLevel());
		newSkill.setName(recievedSkill.getName());
		newSkill.setAbout(recievedSkill.getAbout());
		newSkill.setType(recievedSkill.getType());
		if (CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT)) {
			//if (!CommonModelAttributes.userHasRole(request, RoleTypes.MANAGER))
				//newSkill.setDraft(true);
			newSkill.setOwner(userDAO.getByUsername((
					(UsernamePasswordAuthenticationToken)principal
					).getName()).getId());
		}
		checkNotChildrenOfDraft(request, newSkill);
		for(SkillLevel level : newSkill.getLevels()) {
			if (recievedSkill.getLevels().size() < level.getLevel())
				newSkill.removeLevel(level);
			else
				level.setAbout(recievedSkill.getLevels().get(level.getLevel()-1).getAbout());
		}
		for(int levelIndex = newSkill.getLevels().size() + 1; levelIndex <= recievedSkill.getLevels().size(); levelIndex++) {
			SkillLevel newLevel = new SkillLevel();
			newLevel.setAbout(recievedSkill.getLevels().get(levelIndex-1).getAbout());
			newLevel.setLevel(levelIndex);
			newSkill.addLevel(newLevel);
		}
		if (recievedSkill.getId() == 0)
			facade.getSkillDAO().insert(newSkill);
		else
			facade.getSkillDAO().update(newSkill);
		return "redirect:/skills";
	}
	
	@RequestMapping(path = "/skill/approve", 
			method = RequestMethod.GET)
	public String skillApprove(Model model, HttpServletRequest request,
			@RequestParam(name = "id", required = false) Long id) {
		Skill skill = facade.getSkillDAO().getOne(id);
		checkNotChildrenOfDraft(request, skill);
		//skill.setDraft(false);
		facade.getSkillDAO().update(skill);
		//return "redirect:/viewer/tree";
		return "redirect:/skills";
	}
	

	@ResponseBody
	@RequestMapping(value = "/ajaxSkill",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxSkill(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json);
		if (values.size() >= 1 && DashboardAjax.isInteger(values.get(0).toString(), 10)) {
			long id = Long.parseLong(values.get(0).toString());
			if (id > 0) {
				Skill skill = facade.getSkillDAO().getOne(id)
						.localize(LocaleContextHolder.getLocale());
				return new SkillAnswer(skill, true);
			} else {
				SkillAnswer answer = new SkillAnswer();
				answer.setName("Skill Tree Root");
				answer.setAbout("Skill Tree Root");
				answer.setId(0);
				answer.setSuccessful(true);
				answer.setMaxLevel(1);
				return answer;
			}
		} else {
			return new SkillAnswer();
		}
	}
}
