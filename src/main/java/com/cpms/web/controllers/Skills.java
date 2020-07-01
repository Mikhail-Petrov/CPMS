package com.cpms.web.controllers;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cpms.dao.interfaces.IApplicationsService;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.dao.interfaces.IDraftableSkillDaoExtension;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Article;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.Task;
import com.cpms.exceptions.WrongJsonException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.RoleTypes;
import com.cpms.security.entities.Users;
import com.cpms.web.PagingUtils;
import com.cpms.web.SkillUtils;
//import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpms.web.ajax.IAjaxAnswer;
import com.cpms.web.ajax.InnAnswer;
import com.cpms.web.ajax.SkillAnswer;

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

    @Autowired
    private MessageSource messageSource;

	@ResponseBody
	@RequestMapping(value = "/ajaxSearch",
			method = RequestMethod.POST)
	public List<Skill> ajaxSearch(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		String name = values.size() > 0 ? (String) values.get(0) : "";
		if (name.isEmpty())
			return new ArrayList<>();
		name = "%" + name.replace(" ", "%") + "%";
		
		 List<Skill> res = new ArrayList<>();
		 for (Skill skill : skillDao.findByName(name)) {
			 res.add(new Skill(skill));
		 }
		 return res;
	}
	
	@ResponseBody
	@RequestMapping(value = "/ajaxSkillChildren",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxSkillChildren(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		if (values.size() >= 1 && DashboardAjax.isInteger(values.get(0).toString(), 10)) {
			long id = Long.parseLong(values.get(0).toString());
			InnAnswer answer = new InnAnswer();
			answer.setId(id);
			Skill skill = null;
			if (id > 0)
				skill = facade.getSkillDAO().getOne(id);
			List<Skill> children = skillDao.getChildren(skill);
			String flag = "";
			while (skill != null) {
				skill = skill.getParent();
				flag += "--";
			}
			for (Skill child : children) {
				answer.getIds().add(child.getId());
				answer.getTerms().add(child.getName());
				answer.getFlags().add(flag);
				Set<Skill> kids = child.getChildren(skillDao);
				answer.getKids().add(kids == null ? 0 : kids.size());
			}
			return answer;
		} else {
			return new InnAnswer();
		}
	}
	
	@RequestMapping(path = { "/extractSkills" }, method = RequestMethod.GET)
	public String extractSkills(Model model) {
		String url = "http://data.europa.eu/esco/skill/L";
		Document doc = Statistic.getDoc(url);
		List<Skill> skills = new ArrayList<>();
		Skill main = new Skill("language skills and knowledge", "");
		skills.add(main);
		System.out.print(new Date(System.currentTimeMillis()) + "\n");
		for (Element root : doc.select("a[class*=show-underline]")) {
			List<Skill> newSkills = extractSkill(root.attr("href"), main);
			if (newSkills != null)
				skills.addAll(newSkills);
		}
		System.out.print(new Date(System.currentTimeMillis()) + "\n");
		facade.getSkillDAO().insertAll(skills);
		System.out.print(new Date(System.currentTimeMillis()) + "\n");
		return "redirect:/skills";
	}
	
	private List<Skill> extractSkill(String url, Skill parent) {
		Document doc = Statistic.getDoc(url);
		if (doc == null) return null;
		// get name and description
		String name, about;
		name = doc.select("[class*=header-solid] h1").text();
		if (name.length() > 100) 
			name = name.substring(0, 100);
		if (name == null || name.isEmpty()) return null;
		about = doc.select("pre").text();
		if (about.length() > 4000)
			about = about.substring(0, 4000);
		// get content: alternative and links to children
		Elements alternatives = doc.select("h2:contains(Alternative label) + ul li");
		String alternative = "";
		for (Element alt : alternatives)
			alternative += (alternative.isEmpty() ? "" : "|") + alt.text();
		Elements urls = null, ul = doc.select("h2:contains(Narrower skills/competences) + ul");
		if (!ul.isEmpty())
			urls = ul.first().select("li a");
		// create skill
		Skill newSkill = new Skill(name, about);
		newSkill.setParent(parent);
		if (alternative.length() > 4000)
			alternative = alternative.substring(0, 4000);
		if (!alternative.isEmpty())
			newSkill.setAlternative(alternative);
		List<Skill> ret = new ArrayList<>();
		ret.add(newSkill);
		// get children
		if (urls != null)
		for (Element child : urls) {
			List<Skill> newSkills = extractSkill(child.attr("href"), newSkill);
			if (newSkills != null)
				ret.addAll(newSkills);
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Object> parseJsonObject(String json, MessageSource messageSource) {
		ObjectMapper mapper = new ObjectMapper();
		List<Object> values = null;
		try {
			values = mapper.readValue(json, ArrayList.class);
		} catch (IOException e) {
			throw new WrongJsonException(json, e, messageSource);
		}
		return values;
	}
	
	private static List<Skill> allSkills;
	public static List<Skill> getAllSkills(IDAO<Skill> skillDAO) {
		if (allSkills == null || allSkills.size() != skillDAO.count()) {
			allSkills = skillDAO.getAll();
			//Collections.sort(allSkills);
		}
		return new ArrayList<>(allSkills);
	}
	private List<Skill> getAllSkills() {
		if (allSkills == null || allSkills.size() != facade.getSkillDAO().count()) {
			//allSkills = facade.getSkillDAO().getAll();
			allSkills = skillDao.getChildren(null);
			//Collections.sort(allSkills);
		}
		return allSkills;
	}

	private List<Skill> addSkillsListToModel(Principal principal, HttpServletRequest request) {
		List<Skill> skills;
		if (CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT)) {
			Users owner = userDAO.getByUsername((
					(UsernamePasswordAuthenticationToken)principal
					).getName());
			//skills = facade.getSkillDAO().getAll();
			skills = getAllSkills();
			//skills.addAll(skillDao.getDraftsOfUser(owner.getId()));
		//} else if (CommonModelAttributes.userHasRole(request, RoleTypes.MANAGER)) {
			//skills = skillDao.getAllIncludingDrafts();
		} else {
			skills = getAllSkills();
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
		model.addAttribute("company", new Profile());
		model.addAttribute("task", new Task());
		
		String[] defLevels = {"Basic", "Intermediate", "Advanced", "4", "5", "6"};
		model.addAttribute("defaultLevels", defLevels);
		
		if (CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT)) {
			Users owner = userDAO.getByUsername((
					(UsernamePasswordAuthenticationToken)principal
					).getName());
			List<Skill> skills = getAllSkills();
			//skills.addAll(skillDao.getDraftsOfUser(owner.getId()));
			model.addAttribute("skills", SkillTree.produceTree(sortSkills(skills)));
			Skill newSkill = new Skill();
			newSkill.setMaxLevel(1);
			model.addAttribute("skill", newSkill);
		} else {
			if (CommonModelAttributes.userHasRole(request, RoleTypes.MANAGER))
				//model.addAttribute("skills", SkillTree.produceTree(sortSkills(skillDao.getAllIncludingDrafts())));
				model.addAttribute("skills", SkillTree.produceTree(getAllSkills()));
			else
				model.addAttribute("skills", SkillTree.produceTree(getAllSkills()));
			Skill newSkill = new Skill();
			newSkill.setMaxLevel(1);
			model.addAttribute("skill", newSkill);
		}
		List<Skill> allSkills = addSkillsListToModel(principal, request);
		model.addAttribute("skillsList", SkillUtils.sortAndAddIndents(
				Skills.sortSkills(allSkills), skillDao));
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
