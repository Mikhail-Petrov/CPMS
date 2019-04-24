package com.cpms.web.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.cpms.dao.interfaces.IApplicationsService;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.SkillLevel;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TaskCenter;
import com.cpms.exceptions.NoSessionProfileException;
import com.cpms.exceptions.SessionExpiredException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.operations.interfaces.ITaskComparator;
import com.cpms.security.RoleTypes;
import com.cpms.security.entities.User;
import com.cpms.web.ApplicationsPostForm;
import com.cpms.web.PagingUtils;
import com.cpms.web.SkillUtils;
import com.cpms.web.UserSessionData;

/**
 * Handles dashboard request and competency operations, such as subprofiling,
 * ranging and competency searching.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/dashboard")
public class Dashboard {
	
	//TODO javadoc everything
	
	public static double ACCEPTABLE_DIFFERENCE = 0.2;
	
	@Autowired
	@Qualifier("userSessionData")
	private UserSessionData sessionData;
	
	@Autowired
	@Qualifier("applicationsService")
	private IApplicationsService applicationsService;
	
	@Autowired
	@Qualifier("facade")
	private ICPMSFacade facade;
	
	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;

	@RequestMapping(value = {"/", ""})
	public String viewDashboard(Model model, Principal principal, HttpServletRequest request) {
		model.addAttribute("_VIEW_TITLE", "title.dashboard");
		model.addAttribute("_FORCE_CSRF", true);
		User user = Security.getUser(principal, userDAO);
		List<Task> tasks;
		if (user == null) {
			tasks = facade.getTaskDAO().getAll();
		} else {
			Set<TaskCenter> taskCenters = user.getTasks();
			tasks = new ArrayList<Task>();
			for (TaskCenter center : taskCenters)
				tasks.add(center.getTask());
		}
		model.addAttribute("totalTasks", tasks.size());
		int doneTasks = 0, processTasks = 0, deadlineTasks = 0;
		final String doneStatus = "3";
		Date today = new Date();
		for (Task task : tasks) {
			if (task.getStatus().equals(doneStatus))
				doneTasks++;
			else
				if (task.getDueDate().after(today))
					deadlineTasks++;
				else
					processTasks++;
		}
		model.addAttribute("doneTasks", doneTasks);
		model.addAttribute("processTasks", processTasks);
		model.addAttribute("deadlineTasks", deadlineTasks);
		return "dashboard";
	}
	
	@RequestMapping(value = "/profilesRange")
	public String rangeProfiles(Model model, HttpServletRequest request,
			@RequestParam(value = "page", required = false) Integer page) {//TODO search here (3 methods)?
		model.addAttribute("_VIEW_TITLE", "title.range");
		if (page == null) {
			page = 1;
		}
		List<Profile> result = facade.getProfileRanger().rangeProfiles(
				facade.getProfileDAO().getAll(),
				new LinkedHashSet<Competency>(sessionData.getCompetencies()),
				true);
		return PagingUtils.preparePageFromList(
				page,
				result.size(),
				result
					.stream().skip(PagingUtils.PAGE_SIZE * (page-1))
					.limit(PagingUtils.PAGE_SIZE)
					.collect(Collectors.toList()),
				"/dashboard/profilesRange",
				model,
				request,
				"/viewer/profile",
				"Profiles Ranged");
	}
	
	@RequestMapping(value = "/aggregatePossibilities")
	public String aggregatePossibilities(Model model, HttpServletRequest request,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "profile", required = false) Long profileId) {
		model.addAttribute("_VIEW_TITLE", "title.aggregate");
		if (page == null) {
			page = 1;
		}
		if (sessionData.getProfile() == null && profileId == null) {
			throw new NoSessionProfileException(request.getPathInfo());
		}
		final Profile profile = (profileId == null ? 
				sessionData.getProfile() : 
					facade.getProfileDAO().getOne(profileId));
		List<Task> result = facade
				.getPossibilityAggregator()
				.aggregatePossibilities(profile, 
						facade
						.getTaskDAO()
						.getAll());
		if (profileId != null) {
			model.addAttribute("manual", "&profile=" + profileId);
		}
		return PagingUtils.preparePageFromList(
				page,
				result.size(),
				result
					.stream().skip(PagingUtils.PAGE_SIZE * (page-1))
					.limit(PagingUtils.PAGE_SIZE)
					.collect(Collectors.toList()),
				"/dashboard/aggregatePossibilities",
				model,
				request,
				"/viewer/task",
				"Possibilities of " + profile.getPresentationName());
	}
	
	@RequestMapping(path = "/subprofile")
	public String subprofile(Model model, HttpServletRequest request) {
		model.addAttribute("_VIEW_TITLE", "title.subprofile");
		Profile subprofile = null;
		synchronized (sessionData.getProfile()) { //TODO synchronize user session data within getters/setters
			if (sessionData.getProfile() == null) {
				throw new NoSessionProfileException(request.getPathInfo());
			}
			subprofile = facade.getSubprofiler().subprofile(
					sessionData.getProfile(),
					sessionData.getSkills());
			subprofile.setId(0);
		}
		model.addAttribute("profile", 
				subprofile.localize(LocaleContextHolder.getLocale()));
		return "viewSubprofile";
	}
	
	@RequestMapping(path = "/skill/remove")
	public String skillRemove(
			@RequestParam(name = "id", required = true) int id) {
		synchronized (sessionData.getSkills()) {
			sessionData.getSkills().removeIf(x -> x.getId() == id);
		}
		return "redirect:/dashboard";
	}
	
	@RequestMapping(value = "/competencySearch")
	public String searchProfileByCompetencies(Model model, 
			HttpServletRequest request,
			@RequestParam(value = "page", required = false) Integer page) {
		model.addAttribute("_VIEW_TITLE", "title.search.competency");
		if (page == null) {
			page = 1;
		}
		List<Profile> result = searchProfileByCompetenciesPage(page.intValue());
		return PagingUtils.preparePageFromList(page,
				result.size(),
				result
					.stream().skip(PagingUtils.PAGE_SIZE * (page-1))
					.limit(PagingUtils.PAGE_SIZE)
					.collect(Collectors.toList()),
				"/dashboard/competencySearch",
				model,
				request,
				"/viewer/profile",
				"Profiles Ranged");
	}
	
	public List<Profile> searchProfileByCompetenciesPage(int page) {
		List<Profile> profiles = facade.getProfileCompetencySearcher().searchForProfiles(
				facade.getProfileDAO().getAll(),
				new LinkedHashSet<Competency>(sessionData.getCompetencies()),
				ACCEPTABLE_DIFFERENCE);
		return profiles
				.stream()
				.skip(PagingUtils.PAGE_SIZE * (page-1))
				.limit(PagingUtils.PAGE_SIZE)
				.collect(Collectors.toList());
	}
	
	@RequestMapping(value = "/taskSearch")
	public String searchProfilesByTask(Model model, 
			HttpServletRequest request,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "task", required = false) Long taskId) {
		model.addAttribute("_VIEW_TITLE", "title.search.profiles");
		if (page == null) {
			page = 1;
		}
		if (sessionData.getTask() == null && taskId == null) {
			throw new SessionExpiredException(request.getPathInfo());
		}
		final Task searchTask = (taskId != null ? 
				facade.getTaskDAO().getOne(taskId) : 
					sessionData.getTask());
		ITaskComparator comparator = facade.getTaskComparator();
		List<Profile> result = facade.getProfileDAO().getAll()
				.stream().filter(x -> comparator.taskCompare(x, searchTask))
				.collect(Collectors.toList());
		if (taskId != null) {
			model.addAttribute("manual", "&task=" + taskId);
		}
		return PagingUtils.preparePageFromList(page,
				result.size(),
				result
					.stream().skip(PagingUtils.PAGE_SIZE * (page-1))
					.limit(PagingUtils.PAGE_SIZE)
					.collect(Collectors.toList()),
				"/dashboard/taskSearch",
				model,
				request,
				"/viewer/profile",
				"Task Search");
	}
	
	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String index(Model model){
		model.addAttribute("_VIEW_TITLE", "title.rebuild");
		model.addAttribute("_FORCE_CSRF", true);
		return "index";
	}
	
	@RequestMapping(value = "/index", method = RequestMethod.POST)
	public String indexPost(){
		facade.getProfileDAO().rebuildIndex();
		facade.getTaskDAO().rebuildIndex();
		facade.getSkillDAO().rebuildIndex();
		return "redirect:/dashboard/";
	}
}
