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
import com.cpms.data.entities.Website;
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
@RequestMapping(path = "/website")
public class Websites {
	
	@Autowired
	@Qualifier("websiteDAO")
	private IDAO<Website> websiteDAO;
	
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
	@RequestMapping(value = "/ajaxWebTermSearch",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxWebTermSearch(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		String name = values.size() > 0 ? (String) values.get(0) : "";
		if (name.isEmpty())
			return new InnAnswer();
		
		 List<TermVariant> res = Statistic.termSearch(name, innDAO);
		 InnAnswer ret = new InnAnswer();
		 for (TermVariant var : res)
			 ret.addVariant(var);
		 return ret;
	}
	
	@ResponseBody
	@RequestMapping(value = "/ajaxSearch",
			method = RequestMethod.POST)
	public List<Website> ajaxSearch(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		String name = values.size() > 0 ? (String) values.get(0) : "";
		if (name.isEmpty())
			return new ArrayList<>();

		name = name.toLowerCase();
		 List<Website> res = new ArrayList<>();
		 for (Website web : websiteDAO.getAll())
			 if (web.getName().toLowerCase().contains(name))
				 res.add(new Website(web));
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
			Website website = null;
			if (id > 0)
				website = websiteDAO.getOne(id);
			Set<Website> children;
			if (website != null)
				children = website.getChildren(websiteDAO);
			else {
				children = new HashSet<>();
				for (Website web : websiteDAO.getAll())
					if (web.getParent() == null)
						children.add(web);
			}
			String flag = "";
			while (website != null) {
				website = website.getParent();
				flag += "--";
			}
			for (Website child : children) {
				answer.getIds().add(child.getId());
				answer.getTerms().add(child.getName());
				answer.getFlags().add(flag);
				//Set<Website> kids = child.getChildren(websiteDAO.getAll());
				//answer.getKids().add(kids == null ? 0 : kids.size());
				answer.getKids().add((long) websiteDAO.getInt(child));
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
		model.addAttribute("_VIEW_TITLE", "Websites");
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("html0", ch0);
		model.addAttribute("parent0", parent0);
		ch0 = "";
		
		Website newWebsite = new Website();
		model.addAttribute("website", newWebsite);
		return "websites";
	}

	@ResponseBody
	@RequestMapping(value = "/ajaxWebsite",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxWebsite(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		if (values.size() >= 1 && DashboardAjax.isInteger(values.get(0).toString(), 10)) {
			long id = Long.parseLong(values.get(0).toString());
			if (id > 0) {
				Website website = websiteDAO.getOne(id)
						.localize(LocaleContextHolder.getLocale());
				SkillAnswer answer = new SkillAnswer();
				answer.setName(website.getName());
				answer.setAbout(website.getUrl());
				answer.setName_en(website.getArticleMask());
				answer.setName_ru(website.getLinkMask());
				answer.setAbout_ru(website.getDateMask());
				answer.setAbout_en(website.getDateFormat());
				answer.setDattr(website.getDateAttribute());
				answer.setPages(website.getPageFormat());
				answer.setDraft(website.getShow() > 0);
				answer.setParentId(website.getParent() == null ? null : "" + website.getParent().getId());
				answer.setId(website.getId());
				answer.setSuccessful(true);
				answer.setMaxLevel(1);
				return answer;
			} else {
				SkillAnswer answer = new SkillAnswer();
				answer.setName("Website Tree Root");
				answer.setAbout("Website Tree Root");
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
	public String websiteCreateAlternativeAsync(Model model, @RequestParam(value = "html0", required = false) String html0,
			@ModelAttribute SkillPostForm recievedWebsite, @RequestParam(value = "terms", required = false) List<String> terms,
			HttpServletRequest request,
			Principal principal) {
		ch0 = html0;
		Website newWebsite = new Website();
		if (recievedWebsite.getId() > 0) 
			newWebsite = websiteDAO.getOne(recievedWebsite.getId());
		Long parentId = 0L;
		if (recievedWebsite.getParent() != null && recievedWebsite.getParent() != "")
			try {
				parentId = Long.parseLong(recievedWebsite.getParent());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		parent0 = parentId;
		Website parent = websiteDAO.getOne(parentId);
		newWebsite.setParent(parent);
		newWebsite.setName(recievedWebsite.getName());
		newWebsite.setUrl(recievedWebsite.getAbout());
		newWebsite.setArticleMask(recievedWebsite.getName_en());
		newWebsite.setLinkMask(recievedWebsite.getName_ru());
		newWebsite.setDateMask(recievedWebsite.getAbout_ru());
		newWebsite.setDateFormat(recievedWebsite.getAbout_en());
		newWebsite.setDateAttribute(recievedWebsite.getDattr());
		newWebsite.setPageFormat(recievedWebsite.getPages());
		newWebsite.setShow(recievedWebsite.isDraft() ? 1 : 0);
		newWebsite.getMissingFields();
		
		if (recievedWebsite.getId() == 0)
			websiteDAO.insert(newWebsite);
		else
			websiteDAO.update(newWebsite);
		return "redirect:/website";
	}

	private void deleteWebsite(Website website, long delUser, Date delDate, List<Website> all) {
		//website.setDelDate(delDate);
		//website.setDelUser(delUser);
		for (Website child : website.getChildren(websiteDAO))
			deleteWebsite(child, delUser, delDate, all);
		//facade.getSkillDAO().update(skill);
		websiteDAO.delete(website);
	}
	@RequestMapping(path = {"/delete"}, 
			method = RequestMethod.POST)
	public String skillDelete(Model model, Principal principal, @RequestParam(value = "del0", required = false) String html0,
			@RequestParam(value = "delId", required = true) Long id) {
		ch0 = html0;
		Website website = websiteDAO.getOne(id);
		if (website.getParent() == null)
			parent0 = 0;
		else
			parent0 = website.getParent().getId();
		long delUser = 0;
		/*Users user = userDAO.getByUsername(((UsernamePasswordAuthenticationToken) principal).getName());
		if (user == null)
			delUser = 0;
		else
			delUser = user.getId();*/
		Date delDate = new Date(System.currentTimeMillis());
		deleteWebsite(website, delUser, delDate, websiteDAO.getAll());
		return "redirect:/website";
	}

}
