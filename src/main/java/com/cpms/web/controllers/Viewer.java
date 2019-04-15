package com.cpms.web.controllers;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import com.cpms.data.entities.Competencies;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Language;
import com.cpms.data.entities.Message;
import com.cpms.data.entities.MessageCenter;
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
import com.cpms.web.UserSessionData;
//import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpms.web.ajax.IAjaxAnswer;
import com.cpms.web.ajax.MessageAnswer;
import com.jayway.jsonpath.JsonPath;

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

	// Current status of the group finding
	private String curStatus = "";
	private String timeLog = "";
	public static List<Resident> generatedProfiles = new ArrayList<>();
	public static List<TaskRequirement> generatedReqs = new ArrayList<>();
	private long prevTime = -1;
	public static int testIteration = 1;
	public static int testLength = 1;
	public static int genReqAmount = 10;
	public static int genResAmount = 5000;
	
	public static Random rand = new Random();

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
		if (CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT)) {
			User owner = userDAO.getByUsername(((UsernamePasswordAuthenticationToken) principal).getName());
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

	@RequestMapping(value = { "/", "" }, method = RequestMethod.GET)
	public String viewer(Model model, HttpServletRequest request, Principal principal) {
		long countProfiles = facade.getProfileDAO().count(), countTasks = facade.getTaskDAO().count();
		model.addAttribute("profilePages",
				countProfiles / PagingUtils.PAGE_SIZE + (countProfiles % PagingUtils.PAGE_SIZE > 0 ? 1 : 0));
		model.addAttribute("taskPages",
				countTasks / PagingUtils.PAGE_SIZE + (countTasks % PagingUtils.PAGE_SIZE > 0 ? 1 : 0));
		model.addAttribute("_VIEW_TITLE", "title.profiles");
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("profile", new Profile());
		model.addAttribute("task", new Task());

		String[][] defLevels = { { "Foundation", "Основы" }, { "Intermediate", "Средний уровень" },
				{ "Advanced", "Продвинутый уровень" }, { "Highly specialised", "Высокоспециализированный уровень" } };
		model.addAttribute("defaultLevels", defLevels);

		if (CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT)) {
			User owner = userDAO.getByUsername(((UsernamePasswordAuthenticationToken) principal).getName());
			List<Skill> skills = facade.getSkillDAO().getAll();
			skills.addAll(skillDao.getDraftsOfUser(owner.getId()));
			model.addAttribute("skills", SkillTree.produceTree(skills));
			Skill newSkill = new Skill();
			newSkill.setMaxLevel(1);
			model.addAttribute("skill", newSkill);
		} else {
			if (CommonModelAttributes.userHasRole(request, RoleTypes.MANAGER))
				model.addAttribute("skills", SkillTree.produceTree(skillDao.getAllIncludingDrafts()));
			else
				model.addAttribute("skills", SkillTree.produceTree(facade.getSkillDAO().getAll()));
			Skill newSkill = new Skill();
			newSkill.setMaxLevel(1);
			model.addAttribute("skill", newSkill);
		}
		addSkillsListToModel(model, principal, request);
		List<Language> langs = facade.getLanguageDAO().getAll();
		model.addAttribute("languages", langs);
		return "viewer";
	}

	@RequestMapping(value = "/tasks", method = RequestMethod.GET)
	public String task(Model model, HttpServletRequest request, Principal principal) {
		long countTasks = facade.getTaskDAO().count();
		model.addAttribute("taskPages",
				countTasks / PagingUtils.PAGE_SIZE + (countTasks % PagingUtils.PAGE_SIZE > 0 ? 1 : 0));
		model.addAttribute("_VIEW_TITLE", "navbar.task");
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("task", new Task());
		List<Language> langs = facade.getLanguageDAO().getAll();
		model.addAttribute("languages", langs);
		model.addAttribute("users", userDAO.getAll());
		return "tasks";
	}

	@RequestMapping(value = "/profiles", method = RequestMethod.GET)
	public String profiles(Model model, HttpServletRequest request,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "search", required = false) String search) {
		model.addAttribute("_VIEW_TITLE", "title.profiles");
		if (page == null) {
			page = 1;
		}
		return PagingUtils.preparePageFromDao(page, facade.getProfileDAO(), Profile.class, "/viewer/profiles", model,
				request, true, search, "/viewer/profile", "Profiles", "/editor/profile");
	}

	@ResponseBody
	@RequestMapping(value = "/ajaxProfiles", method = RequestMethod.POST)
	public List<Map<String, Object>> listProfiles(@RequestBody String json) {
		List<Object> values = parseJsonObject(json);
		if (values.size() >= 1 && values.get(0).getClass().equals(Integer.class)) {
			int page = (Integer) values.get(0);
			List<Profile> profiles = facade.getProfileDAO().getAll();
			Collections.sort(profiles);
			int fromIndex = (page - 1) * PagingUtils.PAGE_SIZE, toIndex = page * PagingUtils.PAGE_SIZE - 1;
			if (toIndex >= profiles.size())
				toIndex = profiles.size();
			return profiles.subList(fromIndex, toIndex).stream().map(x -> {
				Profile localized = x.localize(LocaleContextHolder.getLocale());
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
						Map<String, Object> map = new HashMap<>();
						map.put(NAME_KEY, x.getPresentationName());
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
		Profile attrProfile = profile.localize(LocaleContextHolder.getLocale());
		attrProfile.setAbout(profile.getTextFromProofs());
		model.addAttribute("profile", attrProfile);
		List<String> competencies = new ArrayList<String>();
		for (Competency comp : profile.getCompetencies())
			competencies.add(comp.getSkill().getPresentationName());
		model.addAttribute("presentationProofs", profile.getPresentationProofs());
		model.addAttribute("profileCompetencies", competencies);
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("_NAMED_TITLE", true);
		model.addAttribute("_VIEW_TITLE", profile.getPresentationName());
		model.addAttribute("isOwner", false);
		model.addAttribute("competency", new Competency());
		model.addAttribute("competencies", new Competencies(id));

		model.addAttribute("skillsList",
				SkillUtils.sortAndAddIndents(Skills.sortSkills(skillDao.getAllIncludingDrafts())));
		model.addAttribute("skillLevels", SkillLevel.getSkillLevels(facade.getSkillDAO().getAll()));

		if (CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT)) {
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

	@RequestMapping(value = "/task", method = RequestMethod.GET)
	public String task(Model model, @RequestParam(value = "id", required = true) Long id,
			@RequestParam(value = "returnUrl", required = false) String returnUrl, HttpServletRequest request, Principal principal) {
		if (returnUrl == null) {
			returnUrl = "/viewer/tasks";
		}
		model.addAttribute("requirements", new Requirements(id));
		model.addAttribute("requirement", new TaskRequirement());
		model.addAttribute("skillsList",
				SkillUtils.sortAndAddIndents(Skills.sortSkills(skillDao.getAllIncludingDrafts())));
		model.addAttribute("skillLevels", SkillLevel.getSkillLevels(facade.getSkillDAO().getAll()));
		Task task = facade.getTaskDAO().getOne(id);
		model.addAttribute("backPath", returnUrl);
		// add generated requirements
		for (TaskRequirement req : generatedReqs)
			task.addRequirement(req);
		model.addAttribute("task", task);
		List<String> requirements = new ArrayList<String>();
		for (TaskRequirement req : task.getRequirements())
			requirements.add(req.getSkill().getPresentationName());
		model.addAttribute("taskRequirements", requirements);
		model.addAttribute("residentsCount", facade.getProfileDAO().count() + generatedProfiles.size());
		model.addAttribute("requirementsCount", task.getRequirements().size());
		model.addAttribute("testIteration", task.getRequirements().size());
		model.addAttribute("_NAMED_TITLE", true);
		model.addAttribute("_VIEW_TITLE", task.getPresentationName());
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("skillsAndParents", Skills.getSkillsAndParents(skillDao.getAllIncludingDrafts()));
		// Add performers and task manager
		String managerName = "";
		ArrayList<String> performerNames = new ArrayList<>();
		Message taskMessage = EditorTask.createTaskMessage(task, principal, userDAO);
		for (Message message : facade.getMessageDAO().getAll())
			if (message.getTitle().equals(taskMessage.getTitle())) {
				managerName = message.getOwner().getUsername();
				message.getRecipients().stream().forEach(x -> performerNames.add(x.getUser().getUsername()));
				break;
			}
		model.addAttribute("managerName", managerName);
		model.addAttribute("performerNames", performerNames);
		return "viewTask";
	}

	private void updateTime(String event) {
		long curTime = System.currentTimeMillis();
		//timeLog += String.format("\n%s: %d", event, (prevTime < 0) ? 0 : (curTime - prevTime));
		prevTime = curTime;
	}
	
	@RequestMapping(path = { "/task/group" }, method = RequestMethod.GET)
	public String createGroup(Model model, @RequestParam(name = "id", required = true) Long id) {
		curStatus = UserSessionData.localizeText("Формирование шаблона страницы", "Page model forming");
		Task task = facade.getTaskDAO().getOne(id);
		model.addAttribute("_NAMED_TITLE", true);
		model.addAttribute("_VIEW_TITLE", task.getPresentationName());
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("task", task);
		timeLog = "";
		model.addAttribute("options", findGroups(task));
		model.addAttribute("timeLog", timeLog);
		return "group";
	}

	@RequestMapping(path = { "/task/group/generate/residents" }, method = RequestMethod.GET)
	public String generateResidents(Model model, @RequestParam(name = "id", required = true) Long id) {
		generateResidentsFun(genResAmount);
		return "redirect:/viewer/task?id=" + id;
	}

	private void generateResidentsFun(int resAmount) {
		curStatus = UserSessionData.localizeText("Генерирование резидентов", "Residents generating");
		Resident toClone = new Resident(facade.getProfileDAO().getAll().get(0));
		toClone.setId(0);
		List<Skill> skills = facade.getSkillDAO().getAll();
		for (int i = 0; i < resAmount; i++) {
			curStatus = String.format("%s: %d/%d", UserSessionData.localizeText("Генерирование резидентов", "Residents generating"),
					i + 1, resAmount);
			Resident newProfile = new Resident(toClone);
			newProfile.setName(String.format("generated №%d", generatedProfiles.size() + i));
			newProfile.setCost(nextRand(20) + 1);
			// add 5-10 competencies
			long compAmount = nextRand(5) + 5;
			Set<Skill> newSkills= new HashSet<>();
			Set<Competency> newCompetencies = new HashSet<>();
			for (int j = 0; j < compAmount; j++) {
				Skill skill = null;
				do {
					skill = skills.get((int) nextRand(skills.size()));
				} while (newSkills.contains(skill));
				newSkills.add(skill);
				Competency newComp = new Competency(skill, (int) nextRand(skill.getMaxLevel()) + 1);
				newCompetencies.add(newComp);
			}
			newProfile.setCompetencies(newCompetencies);
			generatedProfiles.add(newProfile);
		}
		curStatus = "";
	}
	
	@RequestMapping(path = { "/task/group/generate/requirements" }, method = RequestMethod.GET)
	public String generateRequirements(Model model, @RequestParam(name = "id", required = true) Long id) {
		generateRequirementsFun(facade.getTaskDAO().getOne(id), genReqAmount);
		return "redirect:/viewer/task?id=" + id;
	}
	
	@RequestMapping(path = { "/task/group/clear/requirements" }, method = RequestMethod.GET)
	public String clearRequirements(Model model) {
		generatedReqs.clear();
		return "redirect:/security/me";
	}
	
	@RequestMapping(path = { "/task/group/clear/residents" }, method = RequestMethod.GET)
	public String clearResidents(Model model) {
		generatedProfiles.clear();
		return "redirect:/security/me";
	}
	
	@RequestMapping(path = { "/task/group/clear/all" }, method = RequestMethod.GET)
	public String clearAll(Model model) {
		generatedProfiles.clear();
		generatedReqs.clear();
		return "redirect:/security/me";
	}

	private void generateRequirementsFun(Task task, int reqAmount) {
		curStatus = UserSessionData.localizeText("Генерирование требований", "Requirements generating");
		List<Skill> skills = facade.getSkillDAO().getAll();
		List<Profile> profiles = facade.getProfileDAO().getAll();
		List<Resident> residents = new ArrayList<>();
		for (Profile profile : profiles)
			residents.add(new Resident(profile));
		for (Resident resident : generatedProfiles)
			residents.add(resident);
		for (TaskRequirement req : task.getRequirements())
			skills.remove(req.getSkill());
		for (TaskRequirement req : generatedReqs)
			skills.remove(req.getSkill());
		for (int i = 0; i < reqAmount; i++) {
			Skill skill = null;
			int maxLevel = 0;
			while (skill == null) {
				if (skills.isEmpty())
					break;
				skill = skills.get((int) nextRand(skills.size()));
				skills.remove(skill);
				maxLevel = getBestLevel(skill, residents);
				if (maxLevel == 0)
					skill = null;
			}
			if (skill == null)
				break;
			// generate level
			int level = (int) nextRand((int) (maxLevel * 0.75)) + 1;
			if (level < 1) level = 1;
			if (level > skill.getMaxLevel()) level = skill.getMaxLevel();
			TaskRequirement newReq = new TaskRequirement(skill, level);
			generatedReqs.add(newReq);
		}
		curStatus = "";
	}
	private int getBestLevel(Skill skill, List<Resident> residents) {
		int bestLevel = 0, maxLevel = skill.getMaxLevel();
		for (Resident resident : residents) {
			for (Competency competence : resident.getCompetencies()) {
				if (competence.getSkill().equals(skill)) {
					if (competence.getLevel() > bestLevel)
						bestLevel = competence.getLevel();
					if (bestLevel == maxLevel)
						return bestLevel;
					break;
				}
			}
		}
		return bestLevel;
	}
	
	public static long nextRand(long bound) {
		long nextRand = rand.nextLong();
		nextRand = Math.abs(nextRand);
		if (bound < 1) bound = 1;
		nextRand %= bound;
		return nextRand;
	}
	private List<Option> findGroups(Task task) {
		List<Option> result = new ArrayList<>();
		for (int ll = 0; ll < testLength; ll++) {
		updateTime("Preparing");
		Set<TaskRequirement> originalReqs = task.getRequirements();
		// adding generated requirements
		if (generatedReqs.size() > 0)
			curStatus = UserSessionData.localizeText("Добавление сгенерированных требований", "Adding generated requirements");
		for (TaskRequirement req : generatedReqs)
			if (!task.getRequirements().contains(req))
			task.addRequirement(req);
		// create list of requirements and whether they are filled
		/*curStatus = UserSessionData.localizeText("Создание списка требований", "Creating requirements list");
		HashMap<Long, Boolean> reqFilled = new HashMap<>();
		HashMap<Long, Integer> reqLevels = new HashMap<>();
		for (TaskRequirement req : task.getRequirements()) {
			reqFilled.put(req.getSkill().getId(), false);
			reqLevels.put(req.getSkill().getId(), req.getLevel());
		}
		int unfilled = reqFilled.size();
		curStatus = UserSessionData.localizeText("Получение списка резидентов", "Getting residents list");
		List<Option> result = new ArrayList<>();*/
		
		long sumTime = 0;
		String statusUpdate = "";
		for (int k = 0; k < testIteration; k++) {
			statusUpdate = UserSessionData.localizeText("Создание списка требований", "Creating requirements list");
			if (testIteration <= 1)
				curStatus = statusUpdate;
			else
				curStatus = String.format("Iteration %d.%d of %d. %s", ll+1, k+1, testIteration, statusUpdate);
			HashMap<Long, Boolean> reqFilled = new HashMap<>();
			HashMap<Long, Integer> reqLevels = new HashMap<>();
			for (TaskRequirement req : task.getRequirements()) {
				reqFilled.put(req.getSkill().getId(), false);
				reqLevels.put(req.getSkill().getId(), req.getLevel());
			}
			int unfilled = reqFilled.size();
			statusUpdate = UserSessionData.localizeText("Получение списка резидентов", "Getting residents list");
			if (testIteration <= 1)
				curStatus = statusUpdate;
			else
				curStatus = String.format("Iteration %d.%d of %d. %s", ll+1, k+1, testIteration, statusUpdate);
			result = new ArrayList<>();
		List<Profile> profiles = facade.getProfileDAO().getAll();
		List<Resident> residents = new ArrayList<>();
		for (Profile profile : profiles)
			residents.add(new Resident(profile));
		Collections.sort(residents);
		// adding generated residents
		if (generatedProfiles.size() > 0) {
			statusUpdate = UserSessionData.localizeText("Добавление сгенерированных резидентов", "Adding generated residents");
			if (testIteration <= 1)
				curStatus = statusUpdate;
			else
				curStatus = String.format("Iteration %d.%d of %d. %s", ll+1, k+1, testIteration, statusUpdate);
			for (Resident res : generatedProfiles)
				residents.add(res);
		}
		if (k==0) timeLog += String.format("\nresidents: %d; requirements: %d", residents.size(), task.getRequirements().size());
		updateTime("Start searching");
		sumTime -= prevTime;
		// check all residents
		statusUpdate = UserSessionData.localizeText("Проверка резидентов", "Checking residents");
		if (testIteration <= 1)
			curStatus = statusUpdate;
		else
			curStatus = String.format("Iteration %d.%d of %d. %s", ll+1, k+1, testIteration, statusUpdate);
		Option curOption = new Option(task);
		int curIndex = 0, checked = 0;
		while (!residents.isEmpty()) {
			if (unfilled > 0 && checked < residents.size()) {
				if (curIndex >= residents.size())
					curIndex = 0;
				Resident curProfile = residents.get(curIndex);
				statusUpdate = UserSessionData.localizeText("Проверка резидента " + curProfile.getName(),
						"Checking resident " + curProfile.getName());
				if (testIteration <= 1)
					curStatus = statusUpdate;
				else
					curStatus = String.format("Iteration %d.%d of %d. %s", ll+1, k+1, testIteration, statusUpdate);
				// check if this resident has some unfilled requirements
				boolean hasUnfilled = false;
				for (Competency comp : curProfile.getCompetencies()) {
					long curKey = comp.getSkill().getId();
					if (reqFilled.containsKey(curKey) && !reqFilled.get(curKey)
							&& comp.getLevel() >= reqLevels.get(curKey)) {
						hasUnfilled = true;
						if (!reqFilled.get(curKey)) {
						reqFilled.put(curKey, true);
						if (--unfilled <= 0)
							break;
						}
					}
				}
				// if he has then add to the group and remove from 'unchecked' list
				if (hasUnfilled) {
					statusUpdate = UserSessionData.localizeText("Добавление резидента " + curProfile.getName(),
							"Adding resident " + curProfile.getName());
					if (testIteration <= 1)
						curStatus = statusUpdate;
					else
						curStatus = String.format("Iteration %d.%d of %d. %s", ll+1, k+1, testIteration, statusUpdate);
					curOption.addResident(curProfile);
					residents.remove(curProfile);
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
						residents.remove(curProfile);
					else {
						// go to the next resident
						curIndex++;
						checked++;
					}
				}
			} else {
				// if group is complete then save this option and remove someone
				// if all residents are checked, but there is no solution then break
				if (unfilled <= 0)
					result.add(new Option(curOption));
				else
					break;
				statusUpdate = UserSessionData.localizeText("Оптимизация группы", "Group optimisation");
				if (testIteration <= 1)
					curStatus = statusUpdate;
				else
					curStatus = String.format("Iteration %d.%d of %d. %s", ll+1, k+1, testIteration, statusUpdate);
				curOption.removeBadResident();
				checked = 0;
				// find unfilled requirements
				statusUpdate = UserSessionData.localizeText("Обнаружение невыполненных требований",
						"Finding unfilled requirements");
				if (testIteration <= 1)
					curStatus = statusUpdate;
				else
					curStatus = String.format("Iteration %d.%d of %d. %s", ll+1, k+1, testIteration, statusUpdate);
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
		if (unfilled <= 0)
			result.add(new Option(curOption));
		updateTime("Results processing");
		if (result.size() > 0) {
			statusUpdate = UserSessionData.localizeText("Сортировка вариантов", "Options sorting");
			if (testIteration <= 1)
				curStatus = statusUpdate;
			else
				curStatus = String.format("Iteration %d.%d of %d. %s", ll+1, k+1, testIteration, statusUpdate);
			Collections.sort(result);
			statusUpdate = UserSessionData.localizeText("Нормализация оптимальности", "Optimality normalisation");
			if (testIteration <= 1)
				curStatus = statusUpdate;
			else
				curStatus = String.format("Iteration %d.%d of %d. %s", ll+1, k+1, testIteration, statusUpdate);
			double bestOptimality = result.get(0).getOptimality();
			// set relative optimalities
			if (bestOptimality != 0)
				for (Option option : result)
					option.setOptimality((int) (option.getOptimality() / bestOptimality * 100.0));
		}
		updateTime("finish searching");
		sumTime += prevTime;
		}
		timeLog = String.format("%s\n\navg time: %d\n", timeLog, Math.round(sumTime/testIteration));
		if (testLength > 1) {
			curStatus = UserSessionData.localizeText("Генерация новых условий", "New conditions generation");
			//generatedProfiles.clear();
			generatedReqs.clear();
			//generateResidentsFun((ll+1)*genResAmount);
			generateRequirementsFun(task, (ll+1)*genReqAmount);
		}
		task.setRequirements(originalReqs);
		
		}
		generatedProfiles.clear();
		generatedReqs.clear();
		return result;
	}

	@ResponseBody
	@RequestMapping(value = "/task/getStatus", method = RequestMethod.POST)
	public IAjaxAnswer getGroupFormingStatus() {
		return new MessageAnswer(curStatus + "...");
	}

	@RequestMapping(path = { "/createCompetencyProfile" }, method = RequestMethod.GET)
	public String createCompetencyProfile(Model model, @RequestParam(value = "vkid", required = true) String id)
			throws ClientProtocolException, IOException {
		String userInfo = getUserInfo(id);

		String userName = JsonPath.read(userInfo, "$.user.response[0].first_name") + " "
				+ JsonPath.read(userInfo, "$.user.response[0].last_name");
		String interests = JsonPath.read(userInfo, "$.user.response[0].interests");

		List<Profile> profiles = facade.getProfileDAO().getAll();
		for (Profile profile : profiles)
			if (profile.getPresentationName().equals(userName))
				return "redirect:/viewer/profile?id=" + profile.getId();
		Profile company = new Profile();
		company.setName(userName);
		Profile profile = new Profile();
		profile.update(company);
		profile.setCompetencies(getCompetenciesByInterests(interests.toLowerCase()));
		profile = facade.getProfileDAO().insert(profile);
		return "redirect:/viewer/profile?id=" + profile.getId();
	}

	private String getUserInfo(String id) throws ClientProtocolException, IOException {
		String s = "http://192.168.0.109:5000/api/vk?id=" + id;
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
			String skillName = skill.getName();
			if (skillName == null || skillName == "")
				continue;
			String[] words = skillName.split(" ");
			double equals = 0, wordslength = words.length;
			for (String word : words)
				if (word.length() > 2 || !word.equals(word.toLowerCase())) {
					word = porter.stem(word);
					if (interests.contains(word.toLowerCase()))
						// good word
						equals++;
				} else
					wordslength--;
			if (equals > 0 && wordslength > 0) {
				int level = (int) Math.round(skill.getMaxLevel() * (equals / wordslength));
				if (level < 1)
					level = 1;
				if (level == 1 && skill.getMaxLevel() > 3)
					continue;
				if (level > skill.getMaxLevel())
					level = skill.getMaxLevel();
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
