package com.cpms.web.controllers;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.Category;
import com.cpms.data.entities.Skill;
import com.cpms.exceptions.WrongJsonException;
import com.cpms.web.SkillPostForm;
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
@RequestMapping(path = "/category")
public class Categories {
	
	@Autowired
	@Qualifier("categoryDAO")
	private IDAO<Category> categoryDAO;

    @Autowired
    private MessageSource messageSource;

    public static String ch0 = "";
    public static long parent0 = 0;

	@ResponseBody
	@RequestMapping(value = "/ajaxSearch",
			method = RequestMethod.POST)
	public List<Category> ajaxSearch(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		String name = values.size() > 0 ? (String) values.get(0) : "";
		if (name.isEmpty())
			return new ArrayList<>();
		
		 List<Category> res = new ArrayList<>();
		 for (Category cat : categoryDAO.getAll())
			 if (cat.getName().contains(name))
				 res.add(cat);
		 return res;
	}
	
	@ResponseBody
	@RequestMapping(value = "/ajaxChildren",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxChildren(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		if (values.size() >= 1 && DashboardAjax.isInteger(values.get(0).toString(), 10)) {
			long id = Long.parseLong(values.get(0).toString());
			InnAnswer answer = new InnAnswer();
			answer.setId(id);
			Category category = null;
			if (id > 0)
				category = categoryDAO.getOne(id);
			Set<Category> children;
			if (category != null)
				children = category.getChildren(categoryDAO.getAll());
			else {
				children = new HashSet<>();
				for (Category cat : categoryDAO.getAll())
					if (cat.getParent() == null)
						children.add(cat);
			}
			String flag = "";
			while (category != null) {
				category = category.getParent();
				flag += "--";
			}
			for (Category child : children) {
				answer.getIds().add(child.getId());
				answer.getTerms().add(child.getName());
				answer.getFlags().add(flag);
				Set<Category> kids = child.getChildren(categoryDAO.getAll());
				answer.getKids().add(kids == null ? 0 : kids.size());
			}
			return answer;
		} else {
			return new InnAnswer();
		}
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
	
	@RequestMapping(value = {"/", ""},
			method = RequestMethod.GET)
	public String skills(Model model, HttpServletRequest request, Principal principal) {
		model.addAttribute("_VIEW_TITLE", "title.viewer");
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("html0", ch0);
		model.addAttribute("parent0", parent0);
		ch0 = "";
		
		Category newCategory = new Category();
		model.addAttribute("category", newCategory);
		return "categories";
	}

	@ResponseBody
	@RequestMapping(value = "/ajaxCategory",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxCategory(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		if (values.size() >= 1 && DashboardAjax.isInteger(values.get(0).toString(), 10)) {
			long id = Long.parseLong(values.get(0).toString());
			if (id > 0) {
				Category category = categoryDAO.getOne(id)
						.localize(LocaleContextHolder.getLocale());
				Skill skill = new Skill(), parent = new Skill();
				if (category.getParent() == null)
					parent = null;
				else
					parent.setId(category.getParent().getId());
				skill.setParent(parent);
				skill.setName(category.getName());
				skill.setId(category.getId());
				return new SkillAnswer(skill, true);
			} else {
				SkillAnswer answer = new SkillAnswer();
				answer.setName("Category Tree Root");
				answer.setAbout("Category Tree Root");
				answer.setId(0);
				answer.setSuccessful(true);
				answer.setMaxLevel(1);
				return answer;
			}
		} else {
			return new SkillAnswer();
		}
	}
	
	@RequestMapping(path = "/alternativeAsync", 
			method = RequestMethod.POST)
	public String categoryCreateAlternativeAsync(Model model, @RequestParam(value = "html0", required = false) String html0,
			@ModelAttribute SkillPostForm recievedCategory,
			HttpServletRequest request,
			Principal principal) {
		ch0 = html0;
		Category newCategory = new Category();
		if (recievedCategory.getId() > 0) 
			newCategory = categoryDAO.getOne(recievedCategory.getId());
		Long parentId = 0L;
		if (recievedCategory.getParent() != null && recievedCategory.getParent() != "")
			try {
				parentId = Long.parseLong(recievedCategory.getParent());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		parent0 = parentId;
		Category parent = categoryDAO.getOne(parentId);
		newCategory.setParent(parent);
		newCategory.setName(recievedCategory.getName());
		if (recievedCategory.getId() == 0)
			categoryDAO.insert(newCategory);
		else
			categoryDAO.update(newCategory);
		return "redirect:/category";
	}

	private void deleteCategory(Category category, long delUser, Date delDate, List<Category> all) {
		//category.setDelDate(delDate);
		//category.setDelUser(delUser);
		for (Category child : category.getChildren(all))
			deleteCategory(child, delUser, delDate, all);
		//facade.getSkillDAO().update(skill);
		categoryDAO.delete(category);
	}
	@RequestMapping(path = {"/delete"}, 
			method = RequestMethod.POST)
	public String skillDelete(Model model, Principal principal, @RequestParam(value = "del0", required = false) String html0,
			@RequestParam(value = "delId", required = true) Long id) {
		ch0 = html0;
		Category category = categoryDAO.getOne(id);
		if (category.getParent() == null)
			parent0 = 0;
		else
			parent0 = category.getParent().getId();
		long delUser = 0;
		/*Users user = userDAO.getByUsername(((UsernamePasswordAuthenticationToken) principal).getName());
		if (user == null)
			delUser = 0;
		else
			delUser = user.getId();*/
		Date delDate = new Date(System.currentTimeMillis());
		deleteCategory(category, delUser, delDate, categoryDAO.getAll());
		return "redirect:/category";
	}

}
