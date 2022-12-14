package com.cpms.web.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
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
import com.cpms.data.entities.Skill;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.RoleTypes;
import com.cpms.security.entities.Users;
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
public class SkillTree {
	
	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;
	
	@Autowired
	@Qualifier("facade")
	private ICPMSFacade facade;
	
	@Autowired
	@Qualifier("draftableSkillDAO")
	private IDraftableSkillDaoExtension skillDao;

    @Autowired
    private MessageSource messageSource;

	@RequestMapping(value = "/tree",
			method = RequestMethod.GET)
	public String tree(Model model, Principal principal, 
			HttpServletRequest request,
		@RequestParam(name = "search", required = false) String search) {
		model.addAttribute("_VIEW_TITLE", "title.tree");
		model.addAttribute("_FORCE_CSRF", true);
		if (CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT)) {
			Users owner = userDAO.getByUsername((
					(UsernamePasswordAuthenticationToken)principal
					).getName());
			List<Skill> skills = Skills.getAllSkills(facade.getSkillDAO());
			skills.addAll(skillDao.getDraftsOfUser(owner.getId()));
			model.addAttribute("skills", produceTree(skills));
			Skill newSkill = new Skill();
			newSkill.setMaxLevel(1);
			model.addAttribute("skill", newSkill);
		} else if (CommonModelAttributes.userHasRole(request, RoleTypes.MANAGER)) {
			model.addAttribute("skills", 
					produceTree(Skills.getAllSkills(facade.getSkillDAO())));
					//produceTree(skillDao.getAllIncludingDrafts()));
			Skill newSkill = new Skill();
			newSkill.setMaxLevel(1);
			model.addAttribute("skill", newSkill);
		} else {
			model.addAttribute("skills", produceTree(Skills.getAllSkills(facade.getSkillDAO())));
		}
		model.addAttribute("useSearch", true);
		model.addAttribute("search", search);
		model.addAttribute("skillTitle", UserSessionData.localizeText("skill.level", messageSource));
		if (search != null && search != "") {
			List<Skill> found =
					facade.getSkillDAO().searchRange(search, Skill.class, 0, 1);
			if (found.size() > 0) {
				model.addAttribute("searchId", found.get(0).getId());
			} else {
				model.addAttribute("searchId", 0);
			}
		} else {
			model.addAttribute("searchId", 0);
		}
		model.addAttribute("address", "/viewer/tree");
		return "skillTree";
	}
	
	@ResponseBody
	@RequestMapping(value = "/ajaxSkill",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxSkill(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		if (values.size() >= 1 && DashboardAjax.isInteger(values.get(0).toString(), 10)) {
			long id = Long.parseLong(values.get(0).toString());
			if (id > 0) {
				Skill skill = facade.getSkillDAO().getOne(id)
						;//.localize(LocaleContextHolder.getLocale());
				return new SkillAnswer(skill, true);
			} else {
				SkillAnswer answer = new SkillAnswer();
				answer.setName("Skill Tree Root");
				answer.setAbout("Skill Tree Root");
				answer.setId(0);
				answer.setSuccessful(true);
				answer.setMaxLevel(1);
				answer.setParentId(null);
				return answer;
			}
		} else {
			return new SkillAnswer();
		}
	}
	
	public static Map<Long, List<SkillNameIdTuple>> produceTree(List<Skill> skills) {
		Map<Long, List<SkillNameIdTuple>> result = new LinkedHashMap<Long, List<SkillNameIdTuple>>();
		for(Skill skill : skills) {
			if (skill.getParent() == null) {
				addToTree(result, (long)0, skill.getId(), 
						skill.getPresentationName(), false, skill.getType());
			} else {
				addToTree(result, skill.getParent().getId(), skill.getId(), 
						skill.getPresentationName(), false, skill.getType());
			}
		}
		return result;
	}
	
	public static void addToTree(Map<Long, List<SkillNameIdTuple>> tree, 
			Long parent, Long child, String childName, boolean isDraft, String type) {
		if (!tree.containsKey(parent)) {
			tree.put(parent, new ArrayList<SkillNameIdTuple>());
		}
		SkillNameIdTuple tuple = new SkillNameIdTuple();
		tuple.setId(child);
		tuple.setName(childName);
		tuple.setDraft(isDraft);
		tuple.setType(type);
		tree.get(parent).add(tuple);
	}

}
