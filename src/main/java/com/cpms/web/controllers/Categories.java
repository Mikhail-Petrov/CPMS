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
import com.cpms.dao.interfaces.IInnovationTermDAO;
import com.cpms.data.entities.Category;
import com.cpms.data.entities.Category_Termvariant;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.SkillLevel;
import com.cpms.data.entities.Term;
import com.cpms.data.entities.TermVariant;
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
	@Qualifier(value = "innovationDAO")
	private IInnovationTermDAO innDAO;
	
	@Autowired
	@Qualifier(value = "termDAO")
	private IDAO<Term> termDAO;

    @Autowired
    private MessageSource messageSource;

    public static String ch0 = "";
    public static long parent0 = 0;

	@ResponseBody
	@RequestMapping(value = "/ajaxCatTermSearch",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxCatTermSearch(
			@RequestBody String json) {
		Statistic.time();
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		String name = values.size() > 0 ? (String) values.get(0) : "";
		if (name.isEmpty())
			return new InnAnswer();
		Statistic.time("get name");
		
		 List<TermVariant> res = Statistic.termSearch(name, innDAO);
			Statistic.time("found");
		 InnAnswer ret = new InnAnswer();
		 for (TermVariant var : res)
			 ret.addVariant(var);
		 return ret;
	}
	
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
				children = category.getChildren(categoryDAO);
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
				//Set<Category> kids = child.getChildren(categoryDAO.getAll());
				//answer.getKids().add(kids == null ? 0 : kids.size());
				answer.getKids().add(categoryDAO.getInt(child));
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
		model.addAttribute("_NAMED_TITLE", true);
		model.addAttribute("_VIEW_TITLE", "Categories");
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
				SkillAnswer answer = new SkillAnswer();
				answer.setName(category.getName());
				answer.setName_en(category.getName());
				answer.setName_ru(category.getName());
				answer.setParentId(category.getParent() == null ? null : "" + category.getParent().getId());
				answer.setId(category.getId());
				answer.setSuccessful(true);
				answer.setMaxLevel(category.getVariants().size() + 1);
				for (Category_Termvariant var : category.getVariants()) {
					answer.addLevelFromVariant(var.getVariant());
				}
				return answer;
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
			@ModelAttribute SkillPostForm recievedCategory, @RequestParam(value = "terms", required = false) List<String> terms,
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
		
		// add terms
		Set<Category_Termvariant> oldVars = newCategory.getVariants();
		newCategory.clearVariants();
		for (String sterm : terms) {
			String[] split = sterm.split(":");
			if (split.length < 2) continue;
			long termid = 0, varid = 0;
			try {
				termid = Long.parseLong(split[0]);
				varid = Long.parseLong(split[1]);
			} catch (NumberFormatException e) {}
			Term term = termDAO.getOne(termid);
			if (term == null || varid <= 0) continue;
			for (TermVariant var : term.getVariants())
				if (var.getId() == varid) {
					boolean isOld = false;
					for (Category_Termvariant ct : oldVars)
						if (ct.getVariant().getId() == var.getId()) {
							newCategory.addVariant(ct);
							isOld = true;
							break;
						}
					if (!isOld)
						newCategory.addVariant(new Category_Termvariant(newCategory, var));
					break;
				}
		}
		if (recievedCategory.getId() == 0)
			categoryDAO.insert(newCategory);
		else
			categoryDAO.update(newCategory);
		return "redirect:/category";
	}

	private void deleteCategory(Category category, long delUser, Date delDate, List<Category> all) {
		//category.setDelDate(delDate);
		//category.setDelUser(delUser);
		for (Category child : category.getChildren(categoryDAO))
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
