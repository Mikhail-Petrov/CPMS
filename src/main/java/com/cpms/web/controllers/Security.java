package com.cpms.web.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import com.cpms.data.entities.Company;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.ProfileData;
import com.cpms.exceptions.DataAccessException;
import com.cpms.exceptions.SessionExpiredException;
import com.cpms.exceptions.WrongUserProfileException;
import com.cpms.security.RegistrationForm;
import com.cpms.security.RoleTypes;
import com.cpms.security.entities.Role;
import com.cpms.security.entities.User;
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
	@Qualifier(value = "userSessionData")
	private UserSessionData sessionData;

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
			form.profileId = profile.getId();
		if (!isCreate) {
			User user = userDAO.getByUserID(id);
			form.setId(id);
			form.setAdminRole(user.checkRole(RoleTypes.ADMIN));
			form.setResidentRole(user.checkRole(RoleTypes.RESIDENT));
			form.setUsername(user.getUsername());
			if (form.isResidentRole())
				form.setProfileId(user.getProfileId());
		}
		model.addAttribute("registrationForm", form);
		model.addAttribute("isCreate", isCreate);
		List<ProfileData> profileList = new ArrayList<>();
		for (Profile profileData : profileDAO.getAll())
			profileList.add(new ProfileData(profileData,
					form.profileId != null && profileData.getId() == form.profileId));
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
		User user = new User();
		boolean isCreate = false;
		Long userId = registrationForm.id;
		if (userId == null)
			userId = 0L;
		if (userId == 0 || userDAO.getByUserID(userId) == null)
			isCreate = true;
		if (!isCreate)
			user = userDAO.getByUserID(userId);
		user.setUsername(registrationForm.getUsername());
		if (isCreate || registrationForm.getPassword() != null && !registrationForm.getPassword().isEmpty())
			user.setPassword(registrationForm.getPassword());
		else
			user.setHashed(true);
		if (registrationForm.isAdminRole()) {
			if (!user.checkRole(RoleTypes.ADMIN)) {
				Role newRole = new Role();
				newRole.setRolename(RoleTypes.ADMIN.toString());
				user.addRole(newRole);
			}
		} else {
			Role role = new Role();
			role.setRolename(RoleTypes.ADMIN.toString());
			user.removeRole(role);
		}
		if (registrationForm.isResidentRole()) {
			if (!user.checkRole(RoleTypes.RESIDENT)) {
				Role newRole = new Role();
				newRole.setRolename(RoleTypes.RESIDENT.toString());
				user.addRole(newRole);
			}
			correctProfileCheck(profileDAO.getOne(registrationForm.getProfileId()), request, userId);
			user.setProfileId(registrationForm.getProfileId());
		} else {
			Role role = new Role();
			role.setRolename(RoleTypes.RESIDENT.toString());
			user.removeRole(role);
			user.setProfileId(null);
		}
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
		User profileUser = userDAO.getByProfile(profile);
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

	private List<UserData> getUsersData(List<User> users) {
		List<UserData> res = new ArrayList<>();
		for (User user : users) {
			res.add(new UserData(user));
			Long pid = user.getProfileId();
			if (profileDAO != null && pid != null) {
				Profile profile = profileDAO.getOne(pid);
				if (profile != null)
					res.get(res.size() - 1).setProfileName(profile.getPresentationName());
			}
		}
		return res;
	}

	@RequestMapping(path = { "/delete" }, method = RequestMethod.GET)
	public String profileDelete(Model model, @RequestParam(name = "userId", required = true) Long id) {
		User user = userDAO.getByUserID(id);
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
		if (CommonModelAttributes.userHasRole(request, RoleTypes.RESIDENT)) {
			Long ownerId = userDAO.getByUsername(((UsernamePasswordAuthenticationToken) principal).getName())
					.getProfileId();
			model.addAttribute("profileId", ownerId.longValue());
		}
		return "me";
	}

}
