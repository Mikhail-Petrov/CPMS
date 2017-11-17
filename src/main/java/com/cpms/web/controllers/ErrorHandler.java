package com.cpms.web.controllers;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.cpms.exceptions.DataAccessException;
import com.cpms.exceptions.WebException;

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
	
	@ExceptionHandler(DataAccessException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView dataAccessExceptionHandler(DataAccessException exception) {
		logger.error("Data access exception has happened.", exception);
		return handle("Data access error has occured!", null);
	}
	
	@ExceptionHandler(EntityNotFoundException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ModelAndView EntityNotFoundExceptionHandler(EntityNotFoundException exception) {
		logger.warn("Nonexistant entity was requested.", exception);
		return handle("You have requested object that doesn't exist!", null);
	}
	
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView defaultHandler(Exception exception) {
		logger.error("An unknown error has happened.", exception);
		return handle("An unknown error has occured!", null);
	}
	
	@ExceptionHandler(WebException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ModelAndView webExceptionHandler(WebException exception) {
		logger.info("Web error has happened.", exception);
		return handle(exception.getPublicMessage(), exception.getMessage());
	}
	
	private ModelAndView handle(String message, String description) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName(DEFAULT_ERROR_TEMPLATE);
		mav.addObject("message", message);
		mav.addObject("description", description);
		mav.addObject("_VIEW_TITLE", "title.error");
		return mav;
	}

}
