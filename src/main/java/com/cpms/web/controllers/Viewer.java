package com.cpms.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
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
import com.cpms.data.entities.Motivation;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Requirements;
import com.cpms.data.entities.Resident;
import com.cpms.data.entities.Reward;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.SkillLevel;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TaskCenter;
import com.cpms.data.entities.TaskRequirement;
import com.cpms.exceptions.WrongJsonException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.operations.implementations.Porter;
import com.cpms.security.RoleTypes;
import com.cpms.security.entities.Users;
import com.cpms.web.PagingUtils;
import com.cpms.web.SkillUtils;
//import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpms.web.ajax.IAjaxAnswer;
import com.cpms.web.ajax.MessageAnswer;
import com.jayway.jsonpath.JsonPath;

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

    @Autowired
    private MessageSource messageSource;

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
	public static List<Object> parseJsonObject(String json, MessageSource messageSource) {
		ObjectMapper mapper = new ObjectMapper();
		List<Object> values = null;
		try {
			values = mapper.readValue(json, ArrayList.class);
		} catch (IOException e) {
			throw new WrongJsonException(json, e, messageSource);
		}
		return values;
	}

	private void addSkillsListToModel(Model model, Principal principal, HttpServletRequest request) {
		if (CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT)) {
			Users owner = userDAO.getByUsername(((UsernamePasswordAuthenticationToken) principal).getName());
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
		List<String> names = new ArrayList<>();
		for (Profile profile : facade.getProfileDAO().getAll())
			names.add(profile.getName());
		model.addAttribute("names", names);

		String[][] defLevels = { { "Foundation", "Основы" }, { "Intermediate", "Средний уровень" },
				{ "Advanced", "Продвинутый уровень" }, { "Highly specialised", "Высокоспециализированный уровень" } };
		model.addAttribute("defaultLevels", defLevels);

		if (CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT)) {
			Users owner = userDAO.getByUsername(((UsernamePasswordAuthenticationToken) principal).getName());
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
		Collections.sort(langs);
		model.addAttribute("languages", langs);
		return "viewer";
	}

	@RequestMapping(value = "/tasks", method = RequestMethod.GET)
	public String task(Model model, HttpServletRequest request, Principal principal) {
		model.addAttribute("_VIEW_TITLE", "navbar.task");
		model.addAttribute("_FORCE_CSRF", true);
		List<Task> tasks = new ArrayList<>();
		Users owner = Security.getUser(principal, userDAO);
		if (owner == null || !CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT))
			tasks = facade.getTaskDAO().getAll();
		else for (TaskCenter center : owner.getTasks())
			tasks.add(center.getTask());
		Collections.sort(tasks);
		model.addAttribute("tasks", tasks);
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
				request, true, search, "/viewer/profile", "Profiles", "/editor/profile", messageSource);
	}

	@ResponseBody
	@RequestMapping(value = "/ajaxProfiles", method = RequestMethod.POST)
	public List<Map<String, Object>> listProfiles(@RequestBody String json) {
		List<Object> values = parseJsonObject(json, messageSource);
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

	@SuppressWarnings("deprecation")
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
		
		double gsl1 = 0, gsl2, gsl3, gsl4 = 0, gsl5 = 0;
		
		Users user = userDAO.getByUsername(attrProfile.getName());
		if (user != null) {
			Set<TaskCenter> tasks = user.getTasks();
			for (TaskCenter task : tasks) {
				Date completedDate = task.getTask().getCompletedDate();
				Date dueDate = task.getTask().getDueDate();
				if (completedDate != null && (dueDate == null || !completedDate.after(dueDate)))
					gsl1++;
			}
			if (!tasks.isEmpty())
				gsl1 /= tasks.size();
		}
		String availability = attrProfile.getAvailability();
		if (availability == null || availability.isEmpty())
			gsl2 = 0;
		else gsl2 = availability.equals("1") ? 1 : (
				availability.equals("2") ? 2.0/3 : (
						availability.equals("3") ? 1.0/3 : 0));
		
		Date startDate = attrProfile.getStartDate();
		Date today = new Date();
		if (startDate == null) startDate = today;
		gsl3 = (today.getYear() - startDate.getYear()) * 12 + today.getMonth() - startDate.getMonth();
		if (today.getDate() < startDate.getDate())
			gsl3--;
		//gsl3 = (gsl3 + 1) / 36;
		gsl3 = 1 - 1 / (gsl3 / 12 + 1);
		if (gsl3 < 0) gsl3 = 0;
		
		Set<Competency> comps = attrProfile.getCompetencies();
		for (Competency comp : comps)
			gsl4 += comp.getLevel();
		if (!comps.isEmpty())
			gsl4 /= comps.size();
		gsl4 /= 6;
		
		for (Reward reward : facade.getRewardDAO().getAll()) {
			String[] expertIDs = reward.getExperts().split(",");
			boolean myReward = false;
			for (int i = 0; i < expertIDs.length; i++)
				if (expertIDs[i].equals(attrProfile.getId() + ""))
					myReward = true;
			if (!myReward) continue;
			String[] motivIDs = reward.getMotivations().split(",");
			for (int i = 0; i < motivIDs.length; i++) {
				long motivID = 0;
				try { motivID = Long.parseLong(motivIDs[i]); }
				catch (NumberFormatException e) {}
				if (motivID <= 0) continue;
				Motivation motiv = facade.getMotivationDAO().getOne(motivID);
				if (motiv != null) gsl5 += motiv.getCost();
			}
		}
		gsl5 = 1 - 1 / (0.3 * gsl5 + 1);

		model.addAttribute("globalLevel", (gsl1+gsl2+gsl3+gsl4+gsl5)/5.0);
		//model.addAttribute("globalLevel", gsl1*gsl2*gsl3*gsl4);
		model.addAttribute("globalLevel1", gsl1);
		model.addAttribute("globalLevel2", gsl2);
		model.addAttribute("globalLevel3", gsl3);
		model.addAttribute("globalLevel4", gsl4);
		model.addAttribute("globalLevel5", gsl5);

		model.addAttribute("skillsList",
				SkillUtils.sortAndAddIndents(Skills.sortSkills(skillDao.getAllIncludingDrafts())));
		model.addAttribute("skillLevels", SkillLevel.getSkillLevels(facade.getSkillDAO().getAll()));

		if (CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT)) {
			Long ownerId = userDAO.getByUsername(((UsernamePasswordAuthenticationToken) principal).getName())
					.getProfileId();
			if (ownerId != null && ownerId == profile.getId()) {
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
		managerName = task.getUser().getUsername();
		task.getRecipients().stream().forEach(x -> performerNames.add(x.getUser().getUsername()));
		model.addAttribute("managerName", managerName);
		model.addAttribute("performerNames", performerNames);
		boolean noFinal = true;
		for (Message mes : task.getMessages())
			if (mes.getType().equals("f")) {
				noFinal = false;
				break;
			}
		model.addAttribute("noFinal", noFinal);
		return "viewTask";
	}

	@RequestMapping(path = "/viewRef", method = RequestMethod.GET)
	public String viewRef(Model model, @RequestParam(name = "id", required = true) Long id, HttpServletResponse response) {
		model.addAttribute("_VIEW_TITLE", "title.edit.task");
		Task task = facade.getTaskDAO().getOne(id);
		byte[] bytes = task.getImage();
		if (bytes != null) {
			response.setContentType(task.getImageType());
			try (
					OutputStream outputStream = response.getOutputStream();
					InputStream input = new ByteArrayInputStream(bytes); ){
				IOUtils.copy(input, outputStream);
				outputStream.flush();
			} catch (IOException e) {}
		}
		return "";
	}

	public static long nextRand(long bound) {
		long nextRand = rand.nextLong();
		nextRand = Math.abs(nextRand);
		if (bound < 1) bound = 1;
		nextRand %= bound;
		return nextRand;
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
}
