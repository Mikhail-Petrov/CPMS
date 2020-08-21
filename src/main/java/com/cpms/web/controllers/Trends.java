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
import com.cpms.data.entities.Trend;
import com.cpms.data.entities.Trend_Termvariant;
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
@RequestMapping(path = "/trend")
public class Trends {
	
	@Autowired
	@Qualifier("trendDAO")
	private IDAO<Trend> trendDAO;
	
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
	@RequestMapping(value = "/ajaxTrTermSearch",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxTrTermSearch(
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
	public List<Trend> ajaxSearch(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		String name = values.size() > 0 ? (String) values.get(0) : "";
		if (name.isEmpty())
			return new ArrayList<>();

		name = name.toLowerCase();
		 List<Trend> res = new ArrayList<>();
		 for (Trend tr : trendDAO.getAll())
			 if (tr.getName().toLowerCase().contains(name))
				 res.add(new Trend(tr));
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
			Trend trend = null;
			if (id > 0)
				trend = trendDAO.getOne(id);
			Set<Trend> children;
			if (trend != null)
				children = trend.getChildren(trendDAO);
			else {
				children = new HashSet<>();
				for (Trend tr : trendDAO.getAll())
					if (tr.getParent() == null)
						children.add(tr);
			}
			String flag = "";
			while (trend != null) {
				trend = trend.getParent();
				flag += "--";
			}
			for (Trend child : children) {
				answer.getIds().add(child.getId());
				answer.getTerms().add(child.getName());
				answer.getFlags().add(flag);
				//Set<Trend> kids = child.getChildren(trendDAO.getAll());
				//answer.getKids().add(kids == null ? 0 : kids.size());
				answer.getKids().add((long) trendDAO.getInt(child));
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
		model.addAttribute("_VIEW_TITLE", "Trends");
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("html0", ch0);
		model.addAttribute("parent0", parent0);
		ch0 = "";
		
		Trend newTrend = new Trend();
		model.addAttribute("trend", newTrend);
		return "trends";
	}

	@ResponseBody
	@RequestMapping(value = "/ajaxTrend",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxTrend(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		if (values.size() >= 1 && DashboardAjax.isInteger(values.get(0).toString(), 10)) {
			long id = Long.parseLong(values.get(0).toString());
			if (id > 0) {
				Trend trend = trendDAO.getOne(id)
						.localize(LocaleContextHolder.getLocale());
				SkillAnswer answer = new SkillAnswer();
				answer.setName(trend.getName());
				answer.setName_en(trend.getName());
				answer.setName_ru(trend.getName());
				answer.setParentId(trend.getParent() == null ? null : "" + trend.getParent().getId());
				answer.setId(trend.getId());
				answer.setSuccessful(true);
				answer.setMaxLevel(trend.getVariants().size() + 1);
				for (Trend_Termvariant var : trend.getVariants()) {
					answer.addLevelFromVariant(var.getVariant());
				}
				return answer;
			} else {
				SkillAnswer answer = new SkillAnswer();
				answer.setName("Trend Tree Root");
				answer.setAbout("Trend Tree Root");
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
	public String trendCreateAlternativeAsync(Model model, @RequestParam(value = "html0", required = false) String html0,
			@ModelAttribute SkillPostForm recievedTrend, @RequestParam(value = "terms", required = false) List<String> terms,
			HttpServletRequest request,
			Principal principal) {
		ch0 = html0;
		Trend newTrend = new Trend();
		if (recievedTrend.getId() > 0) 
			newTrend = trendDAO.getOne(recievedTrend.getId());
		Long parentId = 0L;
		if (recievedTrend.getParent() != null && recievedTrend.getParent() != "")
			try {
				parentId = Long.parseLong(recievedTrend.getParent());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		parent0 = parentId;
		Trend parent = trendDAO.getOne(parentId);
		newTrend.setParent(parent);
		newTrend.setName(recievedTrend.getName());
		
		// add terms
		Set<Trend_Termvariant> oldVars = newTrend.getVariants();
		newTrend.clearVariants();
		if (terms != null)
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
					for (Trend_Termvariant ct : oldVars)
						if (ct.getVariant().getId() == var.getId()) {
							newTrend.addVariant(ct);
							isOld = true;
							break;
						}
					if (!isOld)
						newTrend.addVariant(new Trend_Termvariant(newTrend, var));
					break;
				}
		}
		if (recievedTrend.getId() == 0)
			trendDAO.insert(newTrend);
		else
			trendDAO.update(newTrend);
		return "redirect:/trend";
	}

	private void deleteTrend(Trend trend, long delUser, Date delDate, List<Trend> all) {
		//trend.setDelDate(delDate);
		//trend.setDelUser(delUser);
		for (Trend child : trend.getChildren(trendDAO))
			deleteTrend(child, delUser, delDate, all);
		//facade.getSkillDAO().update(skill);
		trendDAO.delete(trend);
	}
	@RequestMapping(path = {"/delete"}, 
			method = RequestMethod.POST)
	public String skillDelete(Model model, Principal principal, @RequestParam(value = "del0", required = false) String html0,
			@RequestParam(value = "delId", required = true) Long id) {
		ch0 = html0;
		Trend trend = trendDAO.getOne(id);
		if (trend.getParent() == null)
			parent0 = 0;
		else
			parent0 = trend.getParent().getId();
		long delUser = 0;
		/*Users user = userDAO.getByUsername(((UsernamePasswordAuthenticationToken) principal).getName());
		if (user == null)
			delUser = 0;
		else
			delUser = user.getId();*/
		Date delDate = new Date(System.currentTimeMillis());
		deleteTrend(trend, delUser, delDate, trendDAO.getAll());
		return "redirect:/trend";
	}

}
