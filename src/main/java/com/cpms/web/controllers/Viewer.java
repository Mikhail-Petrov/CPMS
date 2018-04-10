package com.cpms.web.controllers;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cpms.dao.interfaces.IApplicationsService;
import com.cpms.dao.interfaces.IDraftableSkillDaoExtension;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.EvidenceType;
import com.cpms.data.entities.Company;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Evidence;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.SkillLevel;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TaskRequirement;
import com.cpms.exceptions.WrongJsonException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.RoleTypes;
import com.cpms.security.entities.User;
import com.cpms.web.PagingUtils;
import com.cpms.web.SkillUtils;
//import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Viewer for profile and task entities.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/viewer")
public class Viewer {
	
	private static final String NAME_KEY = "name";
	private static final String ID_KEY = "id";
	
	@Autowired
	@Qualifier("facade")
	private ICPMSFacade facade;
	
	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;
	
	@Autowired
	@Qualifier("applicationsService")
	private IApplicationsService applicationsService;
	
	@Autowired
	@Qualifier("draftableSkillDAO")
	private IDraftableSkillDaoExtension skillDao;
	
	@SuppressWarnings("unchecked")
	public static List<Object> parseJsonObject(String json) {
		ObjectMapper mapper = new ObjectMapper();
		List<Object> values = null;
		try {
			values = mapper.readValue(json, ArrayList.class);
		} catch (IOException e) {
			throw new WrongJsonException(json, e);
		}
		return values;
	}

	private void addSkillsListToModel(Model model, Principal principal,
			HttpServletRequest request) {
		if (CommonModelAttributes.userHasRole(request, RoleTypes.RESIDENT)) {
			User owner = userDAO.getByUsername((
					(UsernamePasswordAuthenticationToken)principal
					).getName());
			List<Skill> skills = facade.getSkillDAO().getAll();
			skills.addAll(skillDao.getDraftsOfUser(owner.getId()));
			model.addAttribute("skillsList", SkillUtils.sortAndAddIndents(Skills.sortSkills(skills)));
		} else if (CommonModelAttributes.userHasRole(request, RoleTypes.ADMIN)) {
			List<Skill> skills = skillDao.getAllIncludingDrafts();
			model.addAttribute("skillsList", SkillUtils.sortAndAddIndents(Skills.sortSkills(skills)));
		} else {
			model.addAttribute("skillsList", 
					SkillUtils.sortAndAddIndents(Skills.sortSkills(facade.getSkillDAO().getAll())));
		}
	}
	
	@RequestMapping(value = {"/", ""},
			method = RequestMethod.GET)
	public String viewer(Model model, HttpServletRequest request, Principal principal) {
		long countProfiles = facade.getProfileDAO().count(),
				countTasks = facade.getTaskDAO().count();
		model.addAttribute("profilePages", countProfiles / PagingUtils.PAGE_SIZE 
				+ (countProfiles % PagingUtils.PAGE_SIZE > 0 ? 1 : 0));
		model.addAttribute("taskPages", countTasks / PagingUtils.PAGE_SIZE 
				+ (countTasks % PagingUtils.PAGE_SIZE > 0 ? 1 : 0));
		model.addAttribute("_VIEW_TITLE", "title.viewer");
		model.addAttribute("_FORCE_CSRF", true);
		if (CommonModelAttributes.userHasRole(request, RoleTypes.ADMIN)) {
			model.addAttribute("company", new Company());
			model.addAttribute("task", new Task());
		}
		
		String[][] defLevels = {{"Foundation", "Основы"}, {"Intermediate", "Средний уровень"},
				{"Advanced", "Продвинутый уровень"},
				{"Highly specialised", "Высокоспециализированный уровень"}};
		model.addAttribute("defaultLevels", defLevels);
		
		if (CommonModelAttributes.userHasRole(request, RoleTypes.RESIDENT)) {
			User owner = userDAO.getByUsername((
					(UsernamePasswordAuthenticationToken)principal
					).getName());
			List<Skill> skills = facade.getSkillDAO().getAll();
			skills.addAll(skillDao.getDraftsOfUser(owner.getId()));
			model.addAttribute("skills", SkillTree.produceTree(skills));
			Skill newSkill = new Skill();
			newSkill.setMaxLevel(1);
			model.addAttribute("skill", newSkill);
		} else if (CommonModelAttributes.userHasRole(request, RoleTypes.ADMIN)) {
			model.addAttribute("skills", 
					SkillTree.produceTree(skillDao.getAllIncludingDrafts()));
			Skill newSkill = new Skill();
			newSkill.setMaxLevel(1);
			model.addAttribute("skill", newSkill);
		} else {
			model.addAttribute("skills", SkillTree.produceTree(facade.getSkillDAO().getAll()));
		}
		addSkillsListToModel(model, principal, request);
		return "viewer";
	}

	@RequestMapping(value = "/profiles",
			method = RequestMethod.GET)
	public String profiles(Model model, HttpServletRequest request,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "search", required = false) String search) {
		model.addAttribute("_VIEW_TITLE", "title.profiles");
		if (page == null) {
			page = 1;
		}
		return PagingUtils.preparePageFromDao(page,
				facade.getProfileDAO(),
				Company.class,
				"/viewer/profiles",
				model,
				request,
				true,
				search,
				"/viewer/profile",
				"Profiles",
				"/editor/profile");
	}
	
	@ResponseBody
	@RequestMapping(value = "/ajaxProfiles",
			method = RequestMethod.POST)
	public List<Map<String, Object>> listProfiles(@RequestBody String json) {
		List<Object> values = parseJsonObject(json);
		if (values.size() >= 1 && values.get(0).getClass().equals(Integer.class)) {
			int page = (Integer)values.get(0);
			return facade.getProfileDAO()
					.getRange((page - 1) * PagingUtils.PAGE_SIZE, 
							page * PagingUtils.PAGE_SIZE)
					.stream()
					.map(x -> {
						Company localized = x.localize(LocaleContextHolder.getLocale());
						Map<String, Object> map = new HashMap<>();
						map.put(NAME_KEY, localized.getPresentationName());
						map.put(ID_KEY, x.getId());
						return map;
					})
					.collect(Collectors.toList()); 
		} else {
			return new ArrayList<>();
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/ajaxTasks",
			method = RequestMethod.POST)
	public List<Map<String, Object>> listTasks(@RequestBody String json) {
		List<Object> values = parseJsonObject(json);
		if (values.size() >= 1 && values.get(0).getClass().equals(Integer.class)) {
			int page = (Integer)values.get(0);
			return facade.getTaskDAO()
					.getRange((page - 1) * PagingUtils.PAGE_SIZE, 
							page * PagingUtils.PAGE_SIZE)
					.stream()
					.map(x -> {
						Task localized = x.localize(LocaleContextHolder.getLocale());
						Map<String, Object> map = new HashMap<>();
						map.put(NAME_KEY, localized.getPresentationName());
						map.put(ID_KEY, x.getId());
						return map;
					})
					.collect(Collectors.toList()); 
		} else {
			return new ArrayList<>();
		}
	}
	
	@RequestMapping(value = "/profile",
			method = RequestMethod.GET)
	public String profile(Model model, Principal principal,
			@RequestParam(value = "id", required = true) Long id,
			@RequestParam(value = "returnUrl", required = false) String returnUrl,
			HttpServletRequest request) {
		Profile profile = facade.getProfileDAO().getOne(id);
		model.addAttribute("profile", profile.localize(LocaleContextHolder.getLocale()));
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("_NAMED_TITLE", true);
		model.addAttribute("_VIEW_TITLE", profile.getPresentationName());
		model.addAttribute("isOwner", false);
		
		if (CommonModelAttributes.userHasRole(request, RoleTypes.ADMIN)) {
			model.addAttribute("competency", new Competency());
			model.addAttribute("skillsList", 
					SkillUtils.sortAndAddIndents(Skills.sortSkills(skillDao.getAllIncludingDrafts())));
			model.addAttribute("skillLevels", SkillLevel.getSkillLevels(facade.getSkillDAO().getAll()));
			model.addAttribute("evidence", new Evidence());
			model.addAttribute("types", Arrays.asList(EvidenceType.values()));
		}
		
		if (CommonModelAttributes.userHasRole(request, RoleTypes.RESIDENT)) {
			Long ownerId = userDAO.getByUsername((
					(UsernamePasswordAuthenticationToken)principal
					).getName())
				.getProfileId();
			if (ownerId == profile.getId()) {
					model.addAttribute("userCompetencyApplications",
							applicationsService.
								retrieveSuggestedCompetenciesOfUser(ownerId.longValue()));
					model.addAttribute("isOwner", true);
			}
		}
		
		return "viewProfile";
	}
	
	@RequestMapping(value = "/tasks",
			method = RequestMethod.GET)
	public String tasks(Model model, HttpServletRequest request,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "search", required = false) String search) {
		model.addAttribute("_VIEW_TITLE", "title.tasks");
		if (page == null) {
			page = 1;
		}
		return PagingUtils.preparePageFromDao(page,
				facade.getTaskDAO(),
				Task.class,//table-responsive
				"/viewer/tasks",
				model,
				request,
				true,
				search,
				"/viewer/task",
				"Tasks",
				"/editor/task");
	}
	
	@RequestMapping(value = "/task",
			method = RequestMethod.GET)
	public String task(Model model,
			@RequestParam(value = "id", required = true) Long id,
			@RequestParam(value = "returnUrl", required = false) String returnUrl,
			HttpServletRequest request) {
		if (returnUrl == null) {
			returnUrl = "/viewer/tasks";
		}
		if (CommonModelAttributes.userHasRole(request, RoleTypes.ADMIN)) {
			model.addAttribute("requirement", new TaskRequirement());
			model.addAttribute("skillsList", 
					SkillUtils.sortAndAddIndents(Skills.sortSkills(skillDao.getAllIncludingDrafts())));
			model.addAttribute("skillLevels", SkillLevel.getSkillLevels(facade.getSkillDAO().getAll()));
		}
		Task task = facade.getTaskDAO().getOne(id);
		model.addAttribute("backPath", returnUrl);
		model.addAttribute("task", task.localize(LocaleContextHolder.getLocale()));
		model.addAttribute("_NAMED_TITLE", true);
		model.addAttribute("_VIEW_TITLE", task.getPresentationName());
		model.addAttribute("_FORCE_CSRF", true);
		return "viewTask";
	}

}
