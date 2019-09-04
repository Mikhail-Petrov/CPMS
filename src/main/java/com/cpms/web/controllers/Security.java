package com.cpms.web.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.cpms.dao.interfaces.IDAO;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Message;
import com.cpms.data.entities.MessageCenter;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TestConfig;
import com.cpms.exceptions.SessionExpiredException;
import com.cpms.exceptions.WrongUserProfileException;
import com.cpms.security.RegistrationForm;
import com.cpms.security.RoleTypes;
import com.cpms.security.entities.Role;
import com.cpms.security.entities.Users;
import com.cpms.security.entities.UserData;
import com.cpms.web.UserSessionData;

/**
 * Handles user creating, login operations and user viewing.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/security/")
public class Security {

	public static String adminName = "admin", adminPassword = "admin";

	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;

	@Autowired
	@Qualifier(value = "profileDAO")
	private IDAO<Profile> profileDAO;

	@Autowired
	@Qualifier(value = "messageDAO")
	private IDAO<Message> messageDAO;

	@Autowired
	@Qualifier(value = "taskDAO")
	private IDAO<Task> taskDAO;

	@Autowired
	@Qualifier(value = "userSessionData")
	private UserSessionData sessionData;

	public static Users getUser(Principal principal, IUserDAO userDAO) {
		if (principal == null) return null;
		Users user = null;
		String username = ((UsernamePasswordAuthenticationToken) principal).getName();
		if (!username.equals(Security.adminName))
			user = userDAO.getByUsername(username);
		return user;
	}

	@RequestMapping(path = "/register", method = RequestMethod.GET)
	public String register(Model model, @RequestParam(name = "userId", required = false) Long id) {
		boolean isCreate = false;
		if (id == null || id == 0 || userDAO.getByUserID(id) == null)
			isCreate = true;
		if (isCreate)
			model.addAttribute("_VIEW_TITLE", "title.register");
		else
			model.addAttribute("_VIEW_TITLE", "title.edit.user");
		model.addAttribute("_FORCE_CSRF", true);
		RegistrationForm form = new RegistrationForm();
		Profile profile = sessionData.getProfile();
		if (profile != null)
			form.setProfileId(profile.getId());
		if (!isCreate) {
			Users user = userDAO.getByUserID(id);
			form.setId(id);
			form.setRole(user.getRole());
			form.setUsername(user.getUsername());
			if (form.getRole() != null && form.getRole().equals(RoleTypes.EXPERT.toRoleName()))
				form.setProfileId(user.getProfileId());
			form.setEmail(user.getEmail());
		}
		model.addAttribute("registrationForm", form);
		model.addAttribute("isCreate", isCreate);
		List<String> roleList = new ArrayList<>();
		for (RoleTypes role : RoleTypes.values())
			roleList.add(role.toRoleName());
		model.addAttribute("roleList", roleList);
		model.addAttribute("expertRole", RoleTypes.EXPERT.toRoleName());
		// Get profiles which are not attached to user (plus this user's profile)
		List<Profile> profileList = profileDAO.getAll(), removeList = new ArrayList<>();
		for (Profile profileInList : profileList) {
			Users profileUser = userDAO.getByProfile(profileInList);
			if (profileUser != null && (id == null || profileUser.getId() != id))
				removeList.add(profileInList);
		}
		for (Profile profileInList : removeList)
			profileList.remove(profileInList);
		Collections.sort(profileList);
		model.addAttribute("profileList", profileList);
		return "register";
	}

	@RequestMapping(path = "/register", method = RequestMethod.POST)
	public String registerPost(HttpServletRequest request,
			@ModelAttribute("registrationForm") @Valid RegistrationForm registrationForm, BindingResult bindingResult,
			Model model) {
		if (registrationForm == null) {
			throw new SessionExpiredException(null);
		}
		if (bindingResult.hasErrors()) {
			model.addAttribute("_VIEW_TITLE", "title.register");
			model.addAttribute("_FORCE_CSRF", true);
			return "register";
		}
		Users user = new Users();
		boolean isCreate = false;
		Long userId = registrationForm.id;
		if (userId == null)
			userId = 0L;
		if (userId == 0 || userDAO.getByUserID(userId) == null)
			isCreate = true;
		if (!isCreate)
			user = userDAO.getByUserID(userId);
		user.setUsername(registrationForm.getUsername());
		if (registrationForm.getRole().equals(RoleTypes.EXPERT.toRoleName()))
			if (registrationForm.getProfileId() != null)
				user.setProfileId(registrationForm.getProfileId());
			else
				user.setProfileId(null);
		if (isCreate || registrationForm.getPassword() != null && !registrationForm.getPassword().isEmpty())
			user.setPassword(registrationForm.getPassword());
		else
			user.setPassword("123");
		user.setEmail(registrationForm.getEmail());
		Role newRole = new Role();
		newRole.setRolename(registrationForm.role);
		user.addRole(newRole);
		if (isCreate)
			userDAO.insertUser(user);
		else
			userDAO.updateUser(user);
		return "redirect:/security/users";
	}

	private void correctProfileCheck(Profile profile, HttpServletRequest request, long userId) {
		if (profile == null) {
			throw new WrongUserProfileException(UserSessionData.localizeText(
					"Вы не выбрали профиль", "You have no chosen profile"), request.getPathInfo());
		}
		Users profileUser = userDAO.getByProfile(profile);
		if (profileUser != null && profileUser.getId() != userId) {
			throw new WrongUserProfileException(UserSessionData.localizeText(
					"Уже есть пользователь с таким профилем",
					"There is already a user with such profile."), request.getPathInfo());
		}
	}

	@RequestMapping(path = "/users", method = RequestMethod.GET)
	public String users(Model model) {
		model.addAttribute("_VIEW_TITLE", "users.management.title");
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("usersList", getUsersData(userDAO.getAll()));
		return "users";
	}

	private List<UserData> getUsersData(List<Users> users) {
		List<UserData> res = new ArrayList<>();
		Collections.sort(users);
		for (Users user : users) {
			res.add(new UserData(user));
			Long pid = user.getProfileId();
			if (profileDAO != null && pid != null && pid > 0) {
				Profile profile = profileDAO.getOne(pid);
				if (profile != null)
					res.get(res.size() - 1).setProfileName(profile.getPresentationName());
			}
		}
		return res;
	}

	@RequestMapping(path = { "/delete" }, method = RequestMethod.GET)
	public String profileDelete(Model model, @RequestParam(name = "userId", required = true) Long id) {
		Users user = userDAO.getByUserID(id);
		for (MessageCenter messageCenter : user.getInMessages()) {
			Message message = messageCenter.getMessage();
			message.removeRecipient(messageCenter);
			messageDAO.update(message);
		}
		user = userDAO.getByUserID(id);
		userDAO.deleteUser(user);
		return "redirect:/security/users";
	}

	@RequestMapping(path = "/login", method = RequestMethod.GET)
	public String login(Model model,
			@RequestParam(name = "error", required = false) String error) throws Exception {
		model.addAttribute("_VIEW_TITLE", "title.login");
		if (error != null)
			error = UserSessionData.localizeText("Неправильное имя пользователя или пароль",
					"Wrong username or password");
		else error = "";
		model.addAttribute("error", error);
		return "login";
	}

	@RequestMapping(path = "/me")
	public String me(Model model, HttpServletRequest request, Principal principal) {
		model.addAttribute("_VIEW_TITLE", "title.user.information");
		model.addAttribute("residentsCount", profileDAO.count() + Viewer.generatedProfiles.size());
		model.addAttribute("requirementsCount", Viewer.generatedReqs.size());
		model.addAttribute("testConfig", new TestConfig());
		return "me";
	}

	@RequestMapping(path = { "/updateConfigs" }, method = RequestMethod.POST)
	public String updateConfigs(Model model, @ModelAttribute("testConfig") @Valid TestConfig testConfig) {
		testConfig.updateConfigs();
		return "redirect:/security/me";
	}
}
