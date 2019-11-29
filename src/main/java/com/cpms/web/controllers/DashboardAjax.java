package com.cpms.web.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import com.cpms.exceptions.WrongJsonException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.web.UserSessionData;

/**
 * AJAX operations for working with {@link UserSessionData} entities.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/dashboard")
public class DashboardAjax {
	
	@Autowired
	@Qualifier(value = "userSessionData")
	private UserSessionData sessionData;
	
	@Autowired
	@Qualifier(value = "facade")
	private ICPMSFacade facade;
	
	public static boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i),radix) < 0) return false;
	    }
	    return true;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Object> parseJson(String json, MessageSource messageSource) {
		ObjectMapper mapper = new ObjectMapper();
		List<Object> values = null;
		try {
			values = mapper.readValue(json, ArrayList.class);
		} catch (IOException e) {
			throw new WrongJsonException(json, e, messageSource);
		}
		return values;
	}
	
}
