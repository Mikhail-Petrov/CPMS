package com.cpms.web.controllers;

import com.jayway.jsonpath.*;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
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
import com.cpms.data.entities.Competencies;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Evidence;
import com.cpms.data.entities.Option;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Requirements;
import com.cpms.data.entities.Resident;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.SkillLevel;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TaskRequirement;
import com.cpms.exceptions.WrongJsonException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.operations.implementations.Porter;
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

	private void addSkillsListToModel(Model model, Principal principal, HttpServletRequest request) {
		if (CommonModelAttributes.userHasRole(request, RoleTypes.RESIDENT)) {
			User owner = userDAO.getByUsername(((UsernamePasswordAuthenticationToken) principal).getName());
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

	@RequestMapping(value = { "/", "" }, method = RequestMethod.GET)
	public String viewer(Model model, HttpServletRequest request, Principal principal) {
		long countProfiles = facade.getProfileDAO().count(), countTasks = facade.getTaskDAO().count();
		model.addAttribute("profilePages",
				countProfiles / PagingUtils.PAGE_SIZE + (countProfiles % PagingUtils.PAGE_SIZE > 0 ? 1 : 0));
		model.addAttribute("taskPages",
				countTasks / PagingUtils.PAGE_SIZE + (countTasks % PagingUtils.PAGE_SIZE > 0 ? 1 : 0));
		model.addAttribute("_VIEW_TITLE", "title.viewer");
		model.addAttribute("_FORCE_CSRF", true);
		if (CommonModelAttributes.userHasRole(request, RoleTypes.ADMIN)) {
			model.addAttribute("company", new Company());
			model.addAttribute("task", new Task());
		}

		String[][] defLevels = { { "Foundation", "Основы" }, { "Intermediate", "Средний уровень" },
				{ "Advanced", "Продвинутый уровень" }, { "Highly specialised", "Высокоспециализированный уровень" } };
		model.addAttribute("defaultLevels", defLevels);

		if (CommonModelAttributes.userHasRole(request, RoleTypes.RESIDENT)) {
			User owner = userDAO.getByUsername(((UsernamePasswordAuthenticationToken) principal).getName());
			List<Skill> skills = facade.getSkillDAO().getAll();
			skills.addAll(skillDao.getDraftsOfUser(owner.getId()));
			model.addAttribute("skills", SkillTree.produceTree(skills));
			Skill newSkill = new Skill();
			newSkill.setMaxLevel(1);
			model.addAttribute("skill", newSkill);
		} else if (CommonModelAttributes.userHasRole(request, RoleTypes.ADMIN)) {
			model.addAttribute("skills", SkillTree.produceTree(skillDao.getAllIncludingDrafts()));
			Skill newSkill = new Skill();
			newSkill.setMaxLevel(1);
			model.addAttribute("skill", newSkill);
		} else {
			model.addAttribute("skills", SkillTree.produceTree(facade.getSkillDAO().getAll()));
		}
		addSkillsListToModel(model, principal, request);
		return "viewer";
	}

	@RequestMapping(value = "/profiles", method = RequestMethod.GET)
	public String profiles(Model model, HttpServletRequest request,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "search", required = false) String search) {
		model.addAttribute("_VIEW_TITLE", "title.profiles");
		if (page == null) {
			page = 1;
		}
		return PagingUtils.preparePageFromDao(page, facade.getProfileDAO(), Company.class, "/viewer/profiles", model,
				request, true, search, "/viewer/profile", "Profiles", "/editor/profile");
	}

	@ResponseBody
	@RequestMapping(value = "/ajaxProfiles", method = RequestMethod.POST)
	public List<Map<String, Object>> listProfiles(@RequestBody String json) {
		List<Object> values = parseJsonObject(json);
		if (values.size() >= 1 && values.get(0).getClass().equals(Integer.class)) {
			int page = (Integer) values.get(0);
			return facade.getProfileDAO().getRange((page - 1) * PagingUtils.PAGE_SIZE, page * PagingUtils.PAGE_SIZE)
					.stream().map(x -> {
						Company localized = x.localize(LocaleContextHolder.getLocale());
						Map<String, Object> map = new HashMap<>();
						map.put(NAME_KEY, localized.getPresentationName());
						map.put(ID_KEY, x.getId());
						return map;
					}).collect(Collectors.toList());
		} else {
			return new ArrayList<>();
		}
	}

	@ResponseBody
	@RequestMapping(value = "/ajaxTasks", method = RequestMethod.POST)
	public List<Map<String, Object>> listTasks(@RequestBody String json) {
		List<Object> values = parseJsonObject(json);
		if (values.size() >= 1 && values.get(0).getClass().equals(Integer.class)) {
			int page = (Integer) values.get(0);
			return facade.getTaskDAO().getRange((page - 1) * PagingUtils.PAGE_SIZE, page * PagingUtils.PAGE_SIZE)
					.stream().map(x -> {
						Task localized = x.localize(LocaleContextHolder.getLocale());
						Map<String, Object> map = new HashMap<>();
						map.put(NAME_KEY, localized.getPresentationName());
						map.put(ID_KEY, x.getId());
						return map;
					}).collect(Collectors.toList());
		} else {
			return new ArrayList<>();
		}
	}

	@RequestMapping(value = "/profile", method = RequestMethod.GET)
	public String profile(Model model, Principal principal, @RequestParam(value = "id", required = true) Long id,
			@RequestParam(value = "returnUrl", required = false) String returnUrl, HttpServletRequest request) {
		Profile profile = facade.getProfileDAO().getOne(id);
		model.addAttribute("profile", profile.localize(LocaleContextHolder.getLocale()));
		List<String> competencies = new ArrayList<String>();
		for (Competency comp : profile.getCompetencies())
			competencies.add(comp.getSkill().getPresentationName());
		model.addAttribute("profileCompetencies", competencies);
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("_NAMED_TITLE", true);
		model.addAttribute("_VIEW_TITLE", profile.getPresentationName());
		model.addAttribute("isOwner", false);
		model.addAttribute("competency", new Competency());
		model.addAttribute("competencies", new Competencies(id));

		if (CommonModelAttributes.userHasRole(request, RoleTypes.ADMIN)) {
			model.addAttribute("skillsList",
					SkillUtils.sortAndAddIndents(Skills.sortSkills(skillDao.getAllIncludingDrafts())));
			model.addAttribute("skillLevels", SkillLevel.getSkillLevels(facade.getSkillDAO().getAll()));
			model.addAttribute("evidence", new Evidence());
			model.addAttribute("types", Arrays.asList(EvidenceType.values()));
		}

		if (CommonModelAttributes.userHasRole(request, RoleTypes.RESIDENT)) {
			Long ownerId = userDAO.getByUsername(((UsernamePasswordAuthenticationToken) principal).getName())
					.getProfileId();
			if (ownerId == profile.getId()) {
				model.addAttribute("userCompetencyApplications",
						applicationsService.retrieveSuggestedCompetenciesOfUser(ownerId.longValue()));
				model.addAttribute("isOwner", true);
			}
		}
		model.addAttribute("skillsAndParents", Skills.getSkillsAndParents(skillDao.getAllIncludingDrafts()));

		model.addAttribute("objectType", "competency");
		return "viewProfile";
	}

	@RequestMapping(value = "/tasks", method = RequestMethod.GET)
	public String tasks(Model model, HttpServletRequest request,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "search", required = false) String search) {
		model.addAttribute("_VIEW_TITLE", "title.tasks");
		if (page == null) {
			page = 1;
		}
		return PagingUtils.preparePageFromDao(page, facade.getTaskDAO(), Task.class, // table-responsive
				"/viewer/tasks", model, request, true, search, "/viewer/task", "Tasks", "/editor/task");
	}

	@RequestMapping(value = "/task", method = RequestMethod.GET)
	public String task(Model model, @RequestParam(value = "id", required = true) Long id,
			@RequestParam(value = "returnUrl", required = false) String returnUrl, HttpServletRequest request) {
		if (returnUrl == null) {
			returnUrl = "/viewer/tasks";
		}
		model.addAttribute("requirements", new Requirements(id));
		if (CommonModelAttributes.userHasRole(request, RoleTypes.ADMIN)) {
			model.addAttribute("requirement", new TaskRequirement());
			model.addAttribute("skillsList",
					SkillUtils.sortAndAddIndents(Skills.sortSkills(skillDao.getAllIncludingDrafts())));
			model.addAttribute("skillLevels", SkillLevel.getSkillLevels(facade.getSkillDAO().getAll()));
		}
		Task task = facade.getTaskDAO().getOne(id);
		model.addAttribute("backPath", returnUrl);
		model.addAttribute("task", task.localize(LocaleContextHolder.getLocale()));
		List<String> requirements = new ArrayList<String>();
		for (TaskRequirement req : task.getRequirements())
			requirements.add(req.getSkill().getPresentationName());
		model.addAttribute("taskRequirements", requirements);
		model.addAttribute("_NAMED_TITLE", true);
		model.addAttribute("_VIEW_TITLE", task.getPresentationName());
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("skillsAndParents", Skills.getSkillsAndParents(skillDao.getAllIncludingDrafts()));
		return "viewTask";
	}

	@RequestMapping(path = { "/task/group" }, method = RequestMethod.GET)
	public String createGroup(Model model, @RequestParam(name = "id", required = true) Long id) {
		model.addAttribute("_VIEW_TITLE", "users.management.title");
		model.addAttribute("_FORCE_CSRF", true);

		Task task = facade.getTaskDAO().getOne(id);
		model.addAttribute("task", task.localize(LocaleContextHolder.getLocale()));
		model.addAttribute("options", findGroups(task));
		return "group";
	}

	private List<Option> findGroups(Task task) {
		List<Option> result = new ArrayList<>();
		List<Profile> profiles = facade.getProfileDAO().getAll();
		// create list of requirements and whether they are filled
		HashMap<Long, Boolean> reqFilled = new HashMap<>();
		HashMap<Long, Integer> reqLevels = new HashMap<>();
		for (TaskRequirement req : task.getRequirements()) {
			reqFilled.put(req.getSkill().getId(), false);
			reqLevels.put(req.getSkill().getId(), req.getLevel());
		}
		int unfilled = reqFilled.size();
		// check all residents
		Option curOption = new Option(task);
		int curIndex = 0, checked = 0;
		while (!profiles.isEmpty()) {
			if (unfilled > 0 && checked < profiles.size()) {
				if (curIndex >= profiles.size())
					curIndex = 0;
				Profile curProfile = profiles.get(curIndex);
				// check if this resident has some unfilled requirements
				boolean hasUnfilled = false;
				for (Competency comp : curProfile.getCompetencies()) {
					long curKey = comp.getSkill().getId();
					if (reqFilled.containsKey(curKey) && !reqFilled.get(curKey)
							&& comp.getLevel() >= reqLevels.get(curKey)) {
						hasUnfilled = true;
						reqFilled.put(curKey, true);
						if (--unfilled <= 0)
							break;
					}
				}
				// if he has then add to the group and remove from 'unchecked' list
				if (hasUnfilled) {
					curOption.addResident(curProfile);
					profiles.remove(curProfile);
				} else {
					// if resident can do nothing then remove him
					boolean isNeeded = false;
					for (Competency comp : curProfile.getCompetencies())
						if (reqFilled.containsKey(comp.getSkill().getId())
								&& comp.getLevel() >= reqLevels.get(comp.getSkill().getId())) {
							isNeeded = true;
							break;
						}
					if (!isNeeded)
						profiles.remove(curProfile);
					else {
						// go to the next resident
						curIndex++;
						checked++;
					}
				}
			} else {
				// if group is complete then save this option and remove someone
				// if all residents are checked, but there is no solution then remove someone
				if (unfilled <= 0)
					result.add(new Option(curOption));
				curOption.removeBadResident();
				checked = 0;
				// find unfilled requirements
				unfilled = 0;
				for (TaskRequirement req : task.getRequirements()) {
					boolean curFilled = false;
					long curKey = req.getSkill().getId();
					// check all residents in the option and their competencies
					for (Resident res : curOption.getResidents()) {
						for (Competency comp : res.getCompetencies())
							if (comp.getSkill().getId() == curKey && comp.getLevel() >= req.getLevel()) {
								curFilled = true;
								break;
							}
						if (curFilled)
							break;
					}
					reqFilled.put(curKey, curFilled);
					if (!curFilled)
						unfilled++;
				}
			}
		}
		if (unfilled == 0)
			result.add(new Option(curOption));
		Collections.sort(result);
		double bestOptimality = result.get(0).getOptimality();
		// set relative optimalities
		if (bestOptimality != 0)
			for (Option option : result)
				option.setOptimality((int) (option.getOptimality() / bestOptimality * 100.0));
		return result;
	}

	@RequestMapping(path = { "/createCompetencyProfile" }, method = RequestMethod.GET)
	public String createCompetencyProfile(Model model, @RequestParam(value = "vkid", required = true) String id) throws ClientProtocolException, IOException {
		String userInfo = getUserInfo(id);

        String userName = JsonPath.read(userInfo, "$.user.response[0].first_name") + " " + JsonPath.read(userInfo, "$.user.response[0].last_name");
        String interests = JsonPath.read(userInfo, "$.user.response[0].interests");
        
		List<Profile> profiles = facade.getProfileDAO().getAll();
		for (Profile profile : profiles)
			if (profile.getPresentationName().equals(userName))
				return "redirect:/viewer/profile?id=" + profile.getId();
		Company company = new Company();
		company.setTitle(userName);
		Profile profile = new Company();
		profile.update(company);
		profile.setCompetencies(getCompetenciesByInterests(interests.toLowerCase()));
		profile = facade.getProfileDAO().insert(profile);
		return "redirect:/viewer/profile?id=" + profile.getId();
	}

	private String getUserInfo(String id) throws ClientProtocolException, IOException {
		String s = "http://85.119.150.9:5000/api/vk?id=" + id;
			Content content = Request.Get(s).execute().returnContent();
			String res = content.asString();
			return res;
	}

	private Set<Competency> getCompetenciesByInterests(String interests) {
		Set<Competency> result = new HashSet<>();
		List<Skill> skills = facade.getSkillDAO().getAll();

		Porter porter = new Porter();
		// interests = getTaggedData(interests);
		for (Skill skill : skills) {
			String skillName = skill.getName_RU();
			if (skillName == null || skillName == "")
				skillName = skill.getName();
			if (skillName == null || skillName == "") continue;
			String[] words = skillName.split(" ");
			double equals = 0, wordslength = words.length;
			for (String word : words)
				if (word.length() > 2 || !word.equals(word.toLowerCase())) {
					word = porter.stem(word);
					if (interests.contains(word.toLowerCase()))
					// good word
						equals++;
				} else wordslength--;
			if (equals > 0 && wordslength > 0) {
				int level = (int) Math.round(skill.getMaxLevel() * (equals / wordslength));
				if (level < 1) level = 1;
				if (level == 1 && skill.getMaxLevel() > 3) continue;
				if (level > skill.getMaxLevel()) level = skill.getMaxLevel();
				Competency newComp = new Competency(skill, level);
				result.add(newComp);
			}
		}
		return result;
	}

	private String getTaggedData(String data) {
		String model = "russian-syntagrus-ud-2.0-conll17-170315";
		String link = "http://lindat.mff.cuni.cz/services/udpipe/api/process?model=" + model
				+ "&tokenizer&tagger&data=";
		try {
			byte[] bb = data.replace(" ", "%20").getBytes("UTF-8");
			String s = link + bb;
			Content content = Request.Get(s).execute().returnContent();
			String res = content.asString();
			bb = res.getBytes("UTF-16");
			return res;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
}
