package com.cpms.web.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

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

	@ModelAttribute("isAuthenticated")
	public boolean isAuthenticated(Principal principal) {
		return principal != null;
	}

	// Gets actual state of navigation menu from cookies and returns its class
	@ModelAttribute("navClass")
	public String navClass(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		boolean menuHidden = false;
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

	@ModelAttribute("isAdmin")
	public boolean isAdmin(HttpServletRequest request) {
		return userHasRole(request, RoleTypes.ADMIN);
	}

	/*@ModelAttribute("remember")
	public static void remember(HttpServletRequest request) {
		// dropdowned = !dropdowned;
	}

	@ModelAttribute("isDropdowned")
	public boolean isDropdowned(HttpServletRequest request) {
		return dropdowned;
	}*/

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
