package com.cpms.web.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cpms.dao.interfaces.IDraftableSkillDaoExtension;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Motivation;
import com.cpms.data.entities.Skill;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.RoleTypes;
import com.cpms.security.entities.User;
import com.cpms.web.MotivationNameIdTuple;
import com.cpms.web.SkillNameIdTuple;
import com.cpms.web.UserSessionData;
import com.cpms.web.ajax.IAjaxAnswer;
import com.cpms.web.ajax.SkillAnswer;

/**
 * Alternative viewer for {@link Skill} entity, built on top of AJAX.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/viewer")
public class MotivationTree {
	
	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;
	
	@Autowired
	@Qualifier("facade")
	private ICPMSFacade facade;
	
	public static Map<Long, List<MotivationNameIdTuple>> produceTree(List<Motivation> motivations) {
		Map<Long, List<MotivationNameIdTuple>> result = new LinkedHashMap<Long, List<MotivationNameIdTuple>>();
		for(Motivation motivation : motivations) {
			if (motivation.getParent() == null) {
				addToTree(result, (long)0, motivation.getId(), 
						motivation.getPresentationName(), motivation.getIsGroup());
			} else {
				addToTree(result, motivation.getParent().getId(), motivation.getId(), 
						motivation.getPresentationName(), motivation.getIsGroup());
			}
		}
		return result;
	}
	
	public static void addToTree(Map<Long, List<MotivationNameIdTuple>> tree, 
			Long parent, Long child, String childName, boolean isGroup) {
		if (!tree.containsKey(parent)) {
			tree.put(parent, new ArrayList<MotivationNameIdTuple>());
		}
		MotivationNameIdTuple tuple = new MotivationNameIdTuple();
		tuple.setId(child);
		tuple.setName(childName);
		tuple.setGroup(isGroup);
		tree.get(parent).add(tuple);
	}

}
