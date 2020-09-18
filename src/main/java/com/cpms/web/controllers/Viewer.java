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
import com.cpms.data.entities.ProjectTermvariant;
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
import com.cpms.security.entities.Role;
import com.cpms.security.entities.Users;
import com.cpms.web.PagingUtils;
import com.cpms.web.ProfileActualization;
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

	@RequestMapping(value = "/createProfile", method = RequestMethod.POST)
	public String createProfile(Model model, @RequestParam(value = "data", required = true) String data) {
		String badRet = "redirect:/viewer";
		data += " ";	// for empty skills
		String[] split = data.split("\n");
		if (split.length < 2) return badRet;

		Profile profile;
		profile = new Profile();
		profile.setName(split[0]);
		
		// add competencies
		String[] comps = split[1].split(" ");
		for (int i = 0; i < comps.length; i++) {
			String[] comp = comps[i].split(":");
			if (comp.length < 2) continue;
			Skill skill = facade.getSkillDAO().getOne(Long.parseLong(comp[0]));
			int level = Integer.parseInt(comp[1]);
			profile.addCompetencySmart(new Competency(skill, level));
		}
		
		profile = facade.getProfileDAO().insert(profile);
		Users user = new Users();
		if (userDAO.getByUsername(profile.getName()) == null) {
			user.setUsername(profile.getName());
			user.setProfileId(profile.getId());
			user.setPassword(profile.getName());
			Role newRole = new Role();
			newRole.setRolename(RoleTypes.EXPERT.toRoleName());
			user.addRole(newRole);
			userDAO.insertUser(user);
		}
		return "redirect:/viewer/profile?id=" + profile.getId();
	}
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

		//addSkillsListToModel(model, principal, request);
		List<Language> langs = facade.getLanguageDAO().getAll();
		Collections.sort(langs);
		model.addAttribute("languages", langs);
		
		model.addAttribute("error", Statistic.matchErr);
		Statistic.matchErr = "";
		return "viewer";
	}

	@RequestMapping(value = "/tasks", method = RequestMethod.GET)
	public String task(Model model, HttpServletRequest request, Principal principal
			, @RequestParam(value = "type", required = false) Integer projectType) {
		model.addAttribute("_VIEW_TITLE", "navbar.task");
		model.addAttribute("_FORCE_CSRF", true);
		List<Task> tasks = new ArrayList<>();
		Users owner = Security.getUser(principal, userDAO);
		if (owner == null || !CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT))
			tasks = facade.getTaskDAO().getAll();
		else for (TaskCenter center : owner.getTasks())
			if (center.getTask().getDelDate() == null)
				tasks.add(center.getTask());
		if (projectType != null)
			for (int i = tasks.size() - 1; i >= 0; i--)
				if (tasks.get(i).getProjectType() != projectType)
					tasks.remove(i);
		Collections.sort(tasks);
		model.addAttribute("tasks", tasks);
		return "tasks";
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
			if (comp.getSkill().getDelDate() == null)
				competencies.add(comp.getSkill().getPresentationName());
		model.addAttribute("presentationProofs", profile.getPresentationProofs());
		model.addAttribute("profileCompetencies", competencies);
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("_NAMED_TITLE", true);
		model.addAttribute("_VIEW_TITLE", profile.getPresentationName());
		model.addAttribute("isOwner", false);
		model.addAttribute("competency", new Competency());
		model.addAttribute("competencies", new Competencies(id));
		
		double gsl1 = 0, gsl2 = 0, gsl3 = 0, gsl4 = 0, gsl5 = 0;
		
		/*Users user = userDAO.getByUsername(attrProfile.getName());
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
		gsl5 = 1 - 1 / (0.3 * gsl5 + 1);*/

		model.addAttribute("globalLevel", (gsl1+gsl2+gsl3+gsl4+gsl5)/5.0);
		//model.addAttribute("globalLevel", gsl1*gsl2*gsl3*gsl4);
		model.addAttribute("globalLevel1", gsl1);
		model.addAttribute("globalLevel2", gsl2);
		model.addAttribute("globalLevel3", gsl3);
		model.addAttribute("globalLevel4", gsl4);
		model.addAttribute("globalLevel5", gsl5);

		//model.addAttribute("report", Statistic.report);

		if (CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT)) {
			Long ownerId = userDAO.getByUsername(((UsernamePasswordAuthenticationToken) principal).getName())
					.getProfileId();
			if (ownerId != null && ownerId == profile.getId()) {
				model.addAttribute("userCompetencyApplications",
						applicationsService.retrieveSuggestedCompetenciesOfUser(ownerId.longValue()));
				model.addAttribute("isOwner", true);
			}
		}

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
		Task task = facade.getTaskDAO().getOne(id);
		model.addAttribute("backPath", returnUrl);
		// add generated requirements
		for (TaskRequirement req : generatedReqs)
			task.addRequirement(req);
		model.addAttribute("task", task);
		List<String> requirements = new ArrayList<String>();
		for (TaskRequirement req : task.getRequirements())
			if (req.getSkill().getDelDate() == null)
				requirements.add(req.getSkill().getPresentationName());
		model.addAttribute("taskRequirements", requirements);
		model.addAttribute("residentsCount", facade.getProfileDAO().count() + generatedProfiles.size());
		model.addAttribute("requirementsCount", task.getRequirements().size());
		model.addAttribute("testIteration", task.getRequirements().size());
		model.addAttribute("_NAMED_TITLE", true);
		model.addAttribute("_VIEW_TITLE", task.getPresentationName());
		model.addAttribute("_FORCE_CSRF", true);
		//model.addAttribute("skillsAndParents", Skills.getSkillsAndParents(skillDao.getAllIncludingDrafts()));
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
		String terms = "";
		for (ProjectTermvariant tvar : task.getVariants())
			terms += (terms.isEmpty() ? "" : ", ") + tvar.getVariant().getText();
		model.addAttribute("terms", terms);
		return "viewTask";
	}

	@RequestMapping(value = "/task/analize", method = RequestMethod.GET)
	public String analize(Model model, @RequestParam(value = "id", required = true) Long id) {
		Task task = facade.getTaskDAO().getOne(id);
		model.addAttribute("_NAMED_TITLE", true);
		model.addAttribute("_VIEW_TITLE", "Task analyse");
		// TODO: генерация участников, требований
		// TODO: добавить проверку выполненности, оценку результата, учёт изменений
		// evaluate the project
		Integer s = -1, cost = task.getCost();
		if (cost != null && cost > 0)
			s = 1;
		
		ArrayList<ProfileActualization> experts = new ArrayList<>();
		// for each participant
		for (TaskCenter center : task.getRecipients()) {
			Users user = center.getUser();
			if (user == null || user.getProfileId() == null) continue;
			Profile profile = facade.getProfileDAO().getOne(user.getProfileId());
			if (profile == null) continue;
			ProfileActualization pa = new ProfileActualization(profile);
			// add each competency that was required
			for (TaskRequirement curReq : task.getRequirements())
				if (curReq.getSkill().getDelDate() == null)
					for (Competency comp : profile.getCompetencies())
						if (curReq.getSkill().equals(comp.getSkill()))
							pa.addCompetency(comp, comp.getLevel());
			if (!pa.getCompetencies().isEmpty())
				experts.add(pa);
		}
		// calculate variables for the task
		int sumReq = 0;
		Map<Long, Integer> reqs = new HashMap<>(), sumProf = new HashMap<>();
		for (TaskRequirement curReq : task.getRequirements()) {
			sumReq += curReq.getLevel();
			reqs.put(curReq.getSkill().getId(), curReq.getLevel());
		}
		for (ProfileActualization expert : experts)
			for (Competency comp : expert.getCompetencies())
				if (!sumProf.containsKey(comp.getSkill().getId()))
					sumProf.put(comp.getSkill().getId(), comp.getLevel());
				else
					sumProf.put(comp.getSkill().getId(), sumProf.get(comp.getSkill().getId()) + comp.getLevel());
		// calculate competencies' impacts
		for (ProfileActualization expert : experts)
			expert.calculateImpacts(sumProf, sumReq, s, reqs);
		model.addAttribute("expertsList", experts);
		return "actualization";
	}

	@RequestMapping(value = "/generate", method = RequestMethod.GET)
	public String generate(Model model, @RequestParam(value = "amount", required = true) Integer amount, @RequestParam(value = "perfMin", required = true) long perfMin,
			@RequestParam(value = "perfMax", required = true) long perfMax) {
		List<Users> allUsers = userDAO.getAll();
		List<Skill> allSkills = Skills.getAllSkills(facade.getSkillDAO());
		// remember users' profiles and its changes
		List<Profile> allProfiles = new ArrayList<>();
		List<Boolean> profileChanges = new ArrayList<>();
		for (Users user : userDAO.getAll()) {
			if (user.getProfileId() != null) {
				Profile profile = facade.getProfileDAO().getOne(user.getProfileId());
				if (profile == null) {
					allUsers.remove(user);
					continue;
				}
				allProfiles.add(profile);
				profileChanges.add(false);
			}
			else
				allUsers.remove(user);
		}

		for (int i = 0; i < amount; i++) {
			List<Users> users = new ArrayList<>(allUsers);
			List<Skill> skills = new ArrayList<>(allSkills);
			Task task = new Task();
			
			task.setStatus("1");
			task.setCreatedDate(new Date(System.currentTimeMillis()));
			task.setDueDate(task.getCreatedDate());
			task.setCost((int) nextRand(2));
			task.setName("Task №" + (i + 1) + " generated at " + task.getCreatedDate());
			Users owner = users.get((int) nextRand(users.size()));
			task.setUser(owner);
			task = facade.getTaskDAO().insert(task);
	
			// add performers in the task
			long perfAmount = nextRand(perfMin, perfMax);
			for (int j = 0; j < perfAmount && !users.isEmpty(); j++) {
				int index = (int) nextRand(users.size());
				Users newRecipient = users.get(index);
				users.remove(newRecipient);
				task.addRecipient(new TaskCenter(newRecipient));
				CommonModelAttributes.newTask.put(newRecipient.getId(), -1);
			}
			
			// add requirements
			long reqAmount = nextRand(5, 10);
			for (int j = 0; j < reqAmount && !skills.isEmpty(); j++) {
				Skill newSkill = skills.get((int) nextRand(skills.size()));
				skills.remove(newSkill);
				task.addRequirement(new TaskRequirement(newSkill, (int) nextRand(1, newSkill.getMaxLevel()/2)));
			}
			
			// identify performers' profiles in the list
			List<Integer> perfIndexes = new ArrayList<>();
			for (TaskCenter center: task.getRecipients()) {
				for (int j = 0; j < allProfiles.size(); j++)
					if (allProfiles.get(j).getId() == center.getUser().getProfileId()) {
						perfIndexes.add(j);
						break;
					}
			}
			// add competencies to the performers
			for (TaskRequirement req : task.getRequirements()) {
				perfAmount = nextRand(1, perfIndexes.size()/2);	// how many of performers will have this competency
				List<Integer> toaddIndexes = new ArrayList<>(perfIndexes);	// indexes of profiles without this competency
				// check how many of performers already has it
				for (int j = 0; j < perfIndexes.size() && perfAmount > 0; j++) {
					Competency comp = null;
					for (Competency curComp : allProfiles.get(perfIndexes.get(j)).getCompetencies())
						if (curComp.getSkill().equals(req.getSkill()))
							comp = curComp;
					if (comp != null) {
						// check/change competency level
						if (comp.getLevel() < req.getLevel()) {
							comp.setLevel((int) nextRand(req.getLevel(), req.getSkill().getMaxLevel()));
							profileChanges.set(perfIndexes.get(j), true);
						}
						// remove it from the list without the competency
						--perfAmount;
						toaddIndexes.remove(j);
						break;
					}
				}
				// add competency to random performers
				for (int j = 0; j < perfAmount; j++) {
					int perfIndex = (int) nextRand(toaddIndexes.size());
					allProfiles.get(toaddIndexes.get(perfIndex)).addCompetency(new Competency(
							req.getSkill(), (int) nextRand(req.getLevel(), req.getSkill().getMaxLevel())));
					profileChanges.set(toaddIndexes.get(perfIndex), true);
					toaddIndexes.remove(perfIndex);
				}
			}
			facade.getTaskDAO().update(task);
		}
		
		// update changed profiles
		for (int i = 0; i < allProfiles.size(); i++)
			if (profileChanges.get(i))
				facade.getProfileDAO().update(allProfiles.get(i));
		
		return "redirect:/viewer/tasks";
	}

	@RequestMapping(value = "/task/apply", method = RequestMethod.GET)
	public String apply(Model model, @RequestParam(value = "expertsList", required = false) ArrayList<ProfileActualization> id,
			HttpServletRequest request, Principal principal) {
		if (id != null && !id.isEmpty()) {
		ArrayList<Competency> competencies = id.get(0).getCompetencies();
		int size = competencies.size();
		
		}
		return "actualization";
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

	public static long nextRand(long minVal, long maxVal) {
		return minVal + nextRand(maxVal - minVal + 1);
	}
	
	public static long nextRand(long bound) {
		if (bound <= 1) return 0;
		long nextRand = rand.nextLong();
		nextRand = Math.abs(nextRand);
		nextRand %= bound;
		return nextRand;
	}
	@ResponseBody
	@RequestMapping(value = "/task/getStatus", method = RequestMethod.POST)
	public IAjaxAnswer getGroupFormingStatus() {
		return new MessageAnswer(curStatus + "...");
	}

	@RequestMapping(path = { "/api" }, method = RequestMethod.GET)
	public String API(Model model, @RequestParam(value = "vkid", required = false) String id) {
		return "test";
	}

	@RequestMapping(path = { "/testAPI" }, method = RequestMethod.GET)
	public String testAPI(Model model, @RequestParam(value = "vkid", required = false) String id) {
		String s = "http://localhost:8080/api/test";
		Content content;
		try {
			content = Request.Get(s).execute().returnContent();
			String res = content.asString();
			res += "hvgfyhudf bh  ";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "redirect:/viewer";
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
		List<Skill> skills = Skills.getAllSkills(facade.getSkillDAO());

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
