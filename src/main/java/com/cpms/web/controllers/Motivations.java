package com.cpms.web.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cpms.data.entities.Motivation;
import com.cpms.facade.ICPMSFacade;
import com.cpms.web.MotivationPostForm;
import com.cpms.web.MotivationUtils;
import com.cpms.web.ajax.IAjaxAnswer;
import com.cpms.web.ajax.MotivationAnswer;
import com.cpms.web.ajax.SkillAnswer;

/**
 * Handles skill CRUD web application requests.
 * Almost completely deprecated because of {@link SkillTree}.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/motivations")
public class Motivations {

	@Autowired
	@Qualifier(value = "facade")
	private ICPMSFacade facade;
	
	public static Map<Long, ArrayList<Long>> getMotivationsAndParents(List<Motivation> allMotivations) {
		// Find all motivation which are not group and add them to
		Map<Long, ArrayList<Long>> motivationsAndParents = new HashMap<>();
		for (Motivation motivation : allMotivations) {
			ArrayList<Long> parents = new ArrayList<>();
			Motivation curMotivation = motivation;
			do {
				parents.add(curMotivation.getId());
				curMotivation = curMotivation.getParent();
				if (parents.size() > 20)
					break;
			} while (curMotivation != null);
			motivationsAndParents.put(motivation.getId(), parents);
		}
		return motivationsAndParents;
	}
	
	private void addMotivationsListToModel(Model model, Principal principal,
			HttpServletRequest request) {
		List<Motivation> motivations = facade.getMotivationDAO().getAll();
		model.addAttribute("motivationsList", MotivationUtils.sortAndAddIndents(motivations));
		model.addAttribute("motivationsAndParents", getMotivationsAndParents(motivations));
	}

	@RequestMapping(value = {"/", ""},
			method = RequestMethod.GET)
	public String motivations(Model model, Principal principal,
			HttpServletRequest request,
			@RequestParam(name = "id", required = false) Long id) {
		model.addAttribute("_VIEW_TITLE", "title.motivations");
		model.addAttribute("_FORCE_CSRF", true);
		
		List<Motivation> motivations = facade.getMotivationDAO().getAll();
		Collections.sort(motivations);
		model.addAttribute("motivations", MotivationTree.produceTree(motivations));
		Motivation newMotivation = new Motivation();
		newMotivation.setCost(1);
		model.addAttribute("motivation", newMotivation);
		
		addMotivationsListToModel(model, principal, request);
		return "motivations";
	}
	
	@ResponseBody
	@RequestMapping(value = "/ajaxMotivation",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxMotivation(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json);
		if (values.size() >= 1 && DashboardAjax.isInteger(values.get(0).toString(), 10)) {
			long id = Long.parseLong(values.get(0).toString());
			if (id > 0) {
				Motivation motivation = facade.getMotivationDAO().getOne(id);
				return new MotivationAnswer(motivation, true);
			} else {
				MotivationAnswer answer = new MotivationAnswer(new Motivation(), true);
				answer.setName("Motivation Tree Root");
				answer.setDescription("Motivation Tree Root");
				answer.setId(0);
				if (values.size() >= 2)
					answer.setIsGroup((boolean) values.get(1));
				return answer;
			}
		} else {
			return new MotivationAnswer();
		}
	}
	
	@RequestMapping(path = {"/delete"}, 
			method = RequestMethod.GET)
	public String motivationDelete(Model model, Principal principal,
			HttpServletRequest request,
			@RequestParam(name = "id", required = true) Long id) {
		Motivation motivation = facade.getMotivationDAO().getOne(id);
		facade.getMotivationDAO().delete(motivation);
		return "redirect:/motivations";
	}
	
	@RequestMapping(path = "/alternativeAsync", 
			method = RequestMethod.POST)
	public String motivationCreateAlternativeAsync(Model model,
			@ModelAttribute MotivationPostForm recievedMotivation,
			HttpServletRequest request,
			Principal principal) {
		Motivation newMotivation = new Motivation();
		if (recievedMotivation.getId() > 0) 
			newMotivation = facade.getMotivationDAO().getOne(recievedMotivation.getId());
		Long parentId = 0L;
		if (recievedMotivation.getParent() != null && recievedMotivation.getParent() != "")
			try {
				parentId = Long.parseLong(recievedMotivation.getParent());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		Motivation parent;
		if (parentId > 0)
			parent = facade.getMotivationDAO().getOne(parentId);
		else parent = null;
		newMotivation.setParent(parent);
		newMotivation.setCost(recievedMotivation.getCost());
		newMotivation.setName(recievedMotivation.getName());
		newMotivation.setDescription(recievedMotivation.getDescription());
		newMotivation.setCode(recievedMotivation.getCode());
		newMotivation.setIsGroup(recievedMotivation.isGroup());
		if (recievedMotivation.getId() == 0)
			facade.getMotivationDAO().insert(newMotivation);
		else
			facade.getMotivationDAO().update(newMotivation);
		return "redirect:/motivations";
	}
}
