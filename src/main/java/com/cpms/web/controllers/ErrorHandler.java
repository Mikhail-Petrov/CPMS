package com.cpms.web.controllers;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.cpms.exceptions.DataAccessException;
import com.cpms.exceptions.WebException;
import com.cpms.security.RoleTypes;
import com.cpms.security.SecurityUser;
import com.cpms.web.UserSessionData;

/**
 * Handles different exceptions.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@ControllerAdvice
public class ErrorHandler {

	private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);
	private static final String DEFAULT_ERROR_TEMPLATE = "error";

    @Autowired
    private MessageSource messageSource;

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView authenticationExceptionHandler(AuthenticationException exception) {
		String message = UserSessionData.localizeText("exception.DataAcces", messageSource);
		logger.error(message, exception);
		return handle(message, exception.getMessage());
	}

	@ExceptionHandler(DataAccessException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView dataAccessExceptionHandler(DataAccessException exception) {
		String message = UserSessionData.localizeText("exception.DataAcces", messageSource);
		logger.error(message, exception);
		return handle(message, exception.getMessage());
	}

	@ExceptionHandler(EntityNotFoundException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ModelAndView EntityNotFoundExceptionHandler(EntityNotFoundException exception) {
		String message = UserSessionData.localizeText("exception.EntityNotFound", messageSource);
		logger.warn(message, exception);
		return handle(message, exception.getMessage());
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView defaultHandler(Exception exception) {
		String message = UserSessionData.localizeText("exception.default", messageSource);
		logger.error(message, exception);
		return handle(message, exception.getMessage());
	}

	@ExceptionHandler(WebException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ModelAndView webExceptionHandler(WebException exception) {
		String message = UserSessionData.localizeText("exception.web", messageSource);
		logger.info(message, exception);
		return handle(exception.getPublicMessage(), exception.getMessage());
	}

	private ModelAndView handle(String message, String description) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName(DEFAULT_ERROR_TEMPLATE);
		mav.addObject("message", message);
		mav.addObject("description", description);
		mav.addObject("_VIEW_TITLE", "title.error");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String username = null;
		boolean isAdmin = false, isResident = false;
		if (auth.isAuthenticated()) {
			if (auth.getPrincipal() instanceof User) {
				User user = (User) auth.getPrincipal();
				for (GrantedAuthority authority : user.getAuthorities()) {
					if (authority.getAuthority().contains(RoleTypes.MANAGER.name()))
						isAdmin = true;
					if (authority.getAuthority().contains(RoleTypes.EXPERT.name()))
						isResident = true;
				}
				username = user.getUsername();
			} else if (auth.getPrincipal() instanceof SecurityUser) {
				SecurityUser secUser = (SecurityUser) auth.getPrincipal();
				isAdmin = secUser.checkRole(RoleTypes.MANAGER);
				isResident = secUser.checkRole(RoleTypes.EXPERT);
				username = secUser.getPresentationName();
			}
		}
		mav.addObject("isAuthenticated", auth.isAuthenticated());
		mav.addObject("isAdmin", isAdmin);
		mav.addObject("isResident", isResident);
		mav.addObject("username", username);
		return mav;
	}

}
