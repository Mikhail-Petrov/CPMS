package com.cpms.web.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.cpms.dao.interfaces.IDAO;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Message;
import com.cpms.data.entities.MessageCenter;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TaskCenter;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.RoleTypes;
import com.cpms.security.entities.Users;
import com.cpms.web.UserSessionData;

/**
 * Provides attributes that are often used in views.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@ControllerAdvice
public class CommonModelAttributes {
	static boolean dropdowned = true;

	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;

	@Autowired
	@Qualifier(value = "messageDAO")
	private IDAO<Message> messageDAO;

	@Autowired
	@Qualifier(value = "taskDAO")
	private IDAO<Task> taskDAO;

	@ModelAttribute("isAuthenticated")
	public boolean isAuthenticated(Principal principal) {
		return principal != null;
	}
	
	public static Map<Long, Integer> newMes, newTask;

	static String testVal = "";
	@ModelAttribute("test")
	public static String test(String val) {
		if (val != null && !val.isEmpty()) testVal = val;
		return testVal;
	}
	
	// Gets actual state of navigation menu from cookies and returns its class
	@ModelAttribute("navClass")
	public String navClass(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		boolean menuHidden = false;
		if (cookies != null)
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("menuHidden"))
					menuHidden = cookie.getValue().equals("true");
			}
		return menuHidden ? "nav-sm" : "nav-md";
	}

	// Gets current page in Profiles view
	@ModelAttribute("profilePage")
	public int profilePage(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		int page = 1;
		if (cookies != null)
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("profilePage"))
					try {
						page = Integer.parseInt(cookie.getValue());
					} catch (NumberFormatException e) {
					}
			}
		return page > 0 ? page : 1;
	}

	// Gets current page in Tasks view
	@ModelAttribute("taskPage")
	public int taskPage(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		int page = 1;
		if (cookies != null)
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("taskPage"))
					try {
						page = Integer.parseInt(cookie.getValue());
					} catch (NumberFormatException e) {
					}
			}
		return page > 0 ? page : 1;
	}
	
	@ModelAttribute("username")
	public String username(Principal principal) {
		if (!isAuthenticated(principal)) {
			return UserSessionData.localizeText("Анонимный пользователь", "Anonymous User");
		}
		UsernamePasswordAuthenticationToken user = (UsernamePasswordAuthenticationToken) principal;
		return user.getName();
	}
	
	@ModelAttribute("newTasks")
	public int newTasks(Principal principal) {
		if (principal == null) return 0;
		if (newTask == null) newTask = new HashMap<>();
		Users user = Security.getUser(principal, userDAO);
		long userId = user == null ? 0 : user.getId();
		int result = newTask.containsKey(userId) ? newTask.get(userId) : -1;
		if (result >= 0) return result;
		result = 0;
		final String assignedStatus = "1";
		if (user == null) {
			for (Task task : taskDAO.getAll())
				if (task.getStatus().equals(assignedStatus))
					result++;
		} else for (TaskCenter center : user.getTasks())
			if (center.getTask().getStatus().equals(assignedStatus))
				result++;
		newTask.put(userId, result);
		return result;
	}
	
	public static void setNewTasks(Principal principal) {
		
	}
	
	@ModelAttribute("newMessages")
	public int newMessages(Principal principal) {
		if (principal == null) return 0;
		if (newMes == null) newMes = new HashMap<>();
		Users user = Security.getUser(principal, userDAO);
		long userId = user == null ? 0 : user.getId();
		int result = newMes.containsKey(userId) ? newMes.get(userId) : -1;
		if (result >= 0) return result;
		Set<MessageCenter> centers = null;
		if (user == null) {
			List<Message> messages = messageDAO.getAll();
			for (Message mes : messages)
				if (centers == null)
					centers = mes.getRecipients();
				else
					centers.addAll(mes.getRecipients());
		} else centers = user.getInMessages();
		if (centers == null) {
			newMes.put(userId, 0);
			return 0;
		}
		result = centers.size();
		for (MessageCenter center : centers)
			if (center.isRed()) result--;
		newMes.put(userId, result);
		return result;
	}
	
	@ModelAttribute("companyId")
	public long companyId(Principal principal) {
		if (!isAuthenticated(principal)) {
			return 0;
		}
		String username = ((UsernamePasswordAuthenticationToken) principal).getName();
		Users byUsername = userDAO.getByUsername(username);
		if (username.equals(Security.adminName) || byUsername == null || byUsername.getProfileId() == null || byUsername.getProfileId() <= 0)
			return 0;
		return userDAO.getByUsername(username).getProfileId().longValue();
	}

	@ModelAttribute("isAdmin")
	public boolean isAdmin(HttpServletRequest request) {
		return userHasRole(request, RoleTypes.MANAGER);
	}

	@ModelAttribute("isResident")
	public Boolean isResident(HttpServletRequest request) {
		return userHasRole(request, RoleTypes.EXPERT);
	}

	@ModelAttribute("isBoss")
	public Boolean isBoss(HttpServletRequest request) {
		return userHasRole(request, RoleTypes.BOSS);
	}

	@ModelAttribute("isHR")
	public Boolean isHR(HttpServletRequest request) {
		return userHasRole(request, RoleTypes.HR);
	}

	@ModelAttribute("getRole")
	public String getRole(HttpServletRequest request) {
		for (RoleTypes role : RoleTypes.values())
			if (userHasRole(request, role))
				return role.toRoleName();
		return "";
	}

	@ModelAttribute("authorities")
	public List<String> authorities(Principal principal) {
		if (!isAuthenticated(principal)) {
			return new ArrayList<String>();
		}
		UsernamePasswordAuthenticationToken user = (UsernamePasswordAuthenticationToken) principal;
		return user.getAuthorities().stream().map(x -> x.getAuthority().substring(5).toLowerCase())
				.collect(Collectors.toList());
	}

	public static boolean userHasRole(HttpServletRequest request, RoleTypes role) {
		SecurityContextHolderAwareRequestWrapper holder = new SecurityContextHolderAwareRequestWrapper(request,
				"ROLE_");
		return holder.isUserInRole(role.toString());
	}
}
