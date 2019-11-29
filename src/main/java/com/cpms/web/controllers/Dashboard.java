package com.cpms.web.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cpms.dao.interfaces.IApplicationsService;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TaskCenter;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.entities.Users;
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
		Users user = Security.getUser(principal, userDAO);
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
		int doneTasks = 0;
		List<Task> deadlineTasks = new ArrayList<Task>();
		List<Task> processTasks = new ArrayList<Task>();
		final String doneStatus = "3";
		Date today = new Date();
		for (Task task : tasks) {
			if (task.getStatus().equals(doneStatus))
				doneTasks++;
			else
				if (task.getDueDate() != null && task.getDueDate().before(today))
					deadlineTasks.add(task);
				else
					processTasks.add(task);
		}
		model.addAttribute("doneTasks", doneTasks);
		model.addAttribute("processTasks", processTasks);
		model.addAttribute("deadlineTasks", deadlineTasks);
		return "dashboard";
	}
	
	@RequestMapping(path = "/skill/remove")
	public String skillRemove(
			@RequestParam(name = "id", required = true) int id) {
		synchronized (sessionData.getSkills()) {
			sessionData.getSkills().removeIf(x -> x.getId() == id);
		}
		return "redirect:/dashboard";
	}
}
