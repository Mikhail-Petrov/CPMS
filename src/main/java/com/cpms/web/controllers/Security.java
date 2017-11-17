package com.cpms.web.controllers;

import java.security.Principal;

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

import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Profile;
import com.cpms.exceptions.SessionExpiredException;
import com.cpms.exceptions.WrongUserProfileException;
import com.cpms.security.RegistrationForm;
import com.cpms.security.RoleTypes;
import com.cpms.security.entities.Role;
import com.cpms.security.entities.User;
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
	
	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;
	
	@Autowired
	@Qualifier(value = "userSessionData")
	private UserSessionData sessionData;
	
	@RequestMapping(path = "/register", method = RequestMethod.GET)
	public String register(Model model) {
		model.addAttribute("_VIEW_TITLE", "title.register");
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("registrationForm", new RegistrationForm());
		return "register";
	}
	
	@RequestMapping(path = "/register", method = RequestMethod.POST)
	public String registerPost( HttpServletRequest request,
			@ModelAttribute("registrationForm") @Valid 
			RegistrationForm registrationForm,
			BindingResult bindingResult,
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
		user.setUsername(registrationForm.getUsername());
		user.setPassword(registrationForm.getPassword());
		if (registrationForm.isAdminRole()) {
			Role newRole = new Role();
			newRole.setRolename(RoleTypes.ADMIN.toString());
			user.addRole(newRole);
		}
		if (registrationForm.isResidentRole()) {
			Role newRole = new Role();
			newRole.setRolename(RoleTypes.RESIDENT.toString());
			user.addRole(newRole);
			correctProfileCheck(sessionData.getProfile(), request);
			user.setProfileId(sessionData.getProfile().getId());
		}
		userDAO.insertUser(user);
		return "redirect:/";
	}
	
	private void correctProfileCheck(Profile profile, HttpServletRequest request) {
		if (profile == null) {
			throw new WrongUserProfileException("You have no remembered profile",
					request.getPathInfo());
		}
		if (userDAO.getByProfile(profile) != null) {
			throw new WrongUserProfileException("There is already a user with such profile.",
					request.getPathInfo());
		}
	}
	
	@RequestMapping(path = "/login", method = RequestMethod.GET)
	public String login(Model model) {
		model.addAttribute("_VIEW_TITLE", "title.login");
		return "login";
	}
	
	@RequestMapping(path = "/me")
	public String me(Model model, HttpServletRequest request,
			Principal principal) {
		model.addAttribute("_VIEW_TITLE", "title.user.information");
        if (CommonModelAttributes.userHasRole(request, RoleTypes.RESIDENT)) {
        	Long ownerId = userDAO.getByUsername((
    				(UsernamePasswordAuthenticationToken)principal
    				).getName())
    			.getProfileId();
        	model.addAttribute("profileId", ownerId.longValue());
        }
		return "me";
	}

}
