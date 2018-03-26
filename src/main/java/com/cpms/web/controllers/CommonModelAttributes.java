package com.cpms.web.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.security.RoleTypes;
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

	@ModelAttribute("isAuthenticated")
	public boolean isAuthenticated(Principal principal) {
		return principal != null;
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
	
	@ModelAttribute("username")
	public String username(Principal principal) {
		if (!isAuthenticated(principal)) {
			return UserSessionData.localizeText("Анонимный пользователь", "Anonymous User");
		}
		UsernamePasswordAuthenticationToken user = (UsernamePasswordAuthenticationToken) principal;
		return user.getName();
	}
	
	@ModelAttribute("companyId")
	public long companyId(Principal principal) {
		if (!isAuthenticated(principal)) {
			return 0;
		}
		String username = ((UsernamePasswordAuthenticationToken) principal).getName();
		if (username.equals(Security.adminName))
			return 0;
		return userDAO.getByUsername(username).getProfileId().longValue();
	}

	@ModelAttribute("isAdmin")
	public boolean isAdmin(HttpServletRequest request) {
		return userHasRole(request, RoleTypes.ADMIN);
	}

	@ModelAttribute("isResident")
	public Boolean isResident(HttpServletRequest request) {
		return userHasRole(request, RoleTypes.RESIDENT);
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
