package com.cpms.web.controllers;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.cpms.dao.interfaces.IApplicationsService;
import com.cpms.dao.interfaces.IDraftableSkillDaoExtension;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Company;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.Task;
import com.cpms.exceptions.WrongJsonException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.RoleTypes;
import com.cpms.security.entities.User;
import com.cpms.web.PagingUtils;
import com.cpms.web.SkillUtils;
//import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Viewer for profile and task entities.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/skills")
public class Skills {
	
	@Autowired
	@Qualifier("facade")
	private ICPMSFacade facade;
	
	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;
	
	@Autowired
	@Qualifier("applicationsService")
	private IApplicationsService applicationsService;
	
	@Autowired
	@Qualifier("draftableSkillDAO")
	private IDraftableSkillDaoExtension skillDao;
	
	@SuppressWarnings("unchecked")
	public static List<Object> parseJsonObject(String json) {
		ObjectMapper mapper = new ObjectMapper();
		List<Object> values = null;
		try {
			values = mapper.readValue(json, ArrayList.class);
		} catch (IOException e) {
			throw new WrongJsonException(json, e);
		}
		return values;
	}

	private List<Skill> addSkillsListToModel(Principal principal, HttpServletRequest request) {
		List<Skill> skills;
		if (CommonModelAttributes.userHasRole(request, RoleTypes.RESIDENT)) {
			User owner = userDAO.getByUsername((
					(UsernamePasswordAuthenticationToken)principal
					).getName());
			skills = facade.getSkillDAO().getAll();
			skills.addAll(skillDao.getDraftsOfUser(owner.getId()));
		} else if (CommonModelAttributes.userHasRole(request, RoleTypes.ADMIN)) {
			skills = skillDao.getAllIncludingDrafts();
		} else {
			skills = facade.getSkillDAO().getAll();
		}
		return skills;
	}
	
	@RequestMapping(value = {"/", ""},
			method = RequestMethod.GET)
	public String skills(Model model, HttpServletRequest request, Principal principal) {
		long countProfiles = facade.getProfileDAO().count(),
				countTasks = facade.getTaskDAO().count();
		model.addAttribute("profilePages", countProfiles / PagingUtils.PAGE_SIZE 
				+ (countProfiles % PagingUtils.PAGE_SIZE > 0 ? 1 : 0));
		model.addAttribute("taskPages", countTasks / PagingUtils.PAGE_SIZE 
				+ (countTasks % PagingUtils.PAGE_SIZE > 0 ? 1 : 0));
		model.addAttribute("_VIEW_TITLE", "title.viewer");
		model.addAttribute("_FORCE_CSRF", true);
		if (CommonModelAttributes.userHasRole(request, RoleTypes.ADMIN)) {
			model.addAttribute("company", new Company());
			model.addAttribute("task", new Task());
		}
		
		String[][] defLevels = {{"Foundation", "Основы"}, {"Intermediate", "Средний уровень"},
				{"Advanced", "Продвинутый уровень"},
				{"Highly specialised", "Высокоспециализированный уровень"}};
		model.addAttribute("defaultLevels", defLevels);
		
		if (CommonModelAttributes.userHasRole(request, RoleTypes.RESIDENT)) {
			User owner = userDAO.getByUsername((
					(UsernamePasswordAuthenticationToken)principal
					).getName());
			List<Skill> skills = facade.getSkillDAO().getAll();
			skills.addAll(skillDao.getDraftsOfUser(owner.getId()));
			model.addAttribute("skills", SkillTree.produceTree(sortSkills(skills)));
			Skill newSkill = new Skill();
			newSkill.setMaxLevel(1);
			model.addAttribute("skill", newSkill);
		} else if (CommonModelAttributes.userHasRole(request, RoleTypes.ADMIN)) {
			model.addAttribute("skills", 
					SkillTree.produceTree(sortSkills(skillDao.getAllIncludingDrafts())));
			Skill newSkill = new Skill();
			newSkill.setMaxLevel(1);
			model.addAttribute("skill", newSkill);
		} else {
			model.addAttribute("skills", SkillTree.produceTree(sortSkills(facade.getSkillDAO().getAll())));
		}
		List<Skill> allSkills = addSkillsListToModel(principal, request);
		model.addAttribute("skillsList", SkillUtils.sortAndAddIndents(
				Skills.sortSkills(allSkills)));
		model.addAttribute("skillsAndParents", getSkillsAndParents(allSkills));
		return "skills";
	}
	
	public static List<Skill> sortSkills(List<Skill> skills) {
		Collections.sort(skills);
		return skills;
	}
	
	public static Map<Long, ArrayList<Long>> getSkillsAndParents(List<Skill> allSkills) {
		Map<Long, ArrayList<Long>> skillsAndParents = new HashMap<>();
		for (Skill skill : allSkills) {
			ArrayList<Long> parents = new ArrayList<>();
			Skill curSkill = skill;
			do {
				parents.add(curSkill.getId());
				curSkill = curSkill.getParent();
				if (parents.size() > 20)
					break;
			} while (curSkill != null);
			skillsAndParents.put(skill.getId(), parents);
		}
		return skillsAndParents;
	}

}
