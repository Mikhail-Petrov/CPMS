package com.cpms.web.controllers;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.cpms.dao.interfaces.IApplicationsService;
import com.cpms.dao.interfaces.IDraftableSkillDaoExtension;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.applications.CompetencyApplication;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Skill;
import com.cpms.exceptions.DataAccessException;
import com.cpms.exceptions.NoResidentUserProfile;
import com.cpms.exceptions.SessionExpiredException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.entities.Users;
import com.cpms.web.ApplicationsPostForm;
import com.cpms.web.SkillUtils;

/**
 * Controller that handles applications.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/applications")
public class Applications {
	
	@Autowired
	@Qualifier("applicationsService")
	private IApplicationsService applicationsService;
	
	@Autowired
	@Qualifier("draftableSkillDAO")
	private IDraftableSkillDaoExtension skillDao;
	
	@Autowired
	@Qualifier("facade")
	private ICPMSFacade facade;
	
	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;
	
	/**
	 * Allows to send Dates via post.
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
	    CustomDateEditor editor =
	    		new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true);
	    binder.registerCustomEditor(Date.class, editor);
	}

	@RequestMapping(path = "/suggest/competency",
			method = RequestMethod.GET)
	public String competencySuggest(Model model, Principal principal) {
		model.addAttribute("_VIEW_TITLE", "title.edit.application.competency");
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("competency", new Competency());
		model.addAttribute("create", true);
		List<Skill> skills = facade.getSkillDAO().getAll();
		Users owner = userDAO.getByUsername((
				(UsernamePasswordAuthenticationToken)principal
				).getName());
		skills.addAll(skillDao.getDraftsOfUser(owner.getId()));
		model.addAttribute("skillsList", SkillUtils.sortAndAddIndents(Skills.sortSkills(skills)));
		model.addAttribute("postAddress", "/applications/suggest/competency");
		return "editCompetency";
	}
	
	@RequestMapping(path = "/suggest/evidence/{competencyId}/",
			method = RequestMethod.GET)
	public String evidenceSuggest(Model model,
			@PathVariable("competencyId") long competencyId) {
		model.addAttribute("_VIEW_TITLE", "title.edit.application.evidence");
		model.addAttribute("create", true);
		model.addAttribute("postAddress", "/applications/suggest/evidence/" + 
				competencyId + "/");
		return "editEvidence";
	}
	
	@RequestMapping(path = "/suggest/competency",
			method = RequestMethod.POST)
	public String competencySuggestPost(Model model, HttpServletRequest request,
			@ModelAttribute("competency") @Valid Competency recievedCompetency,
			BindingResult bindingResult, Principal principal) {
		if (recievedCompetency == null) {
			throw new SessionExpiredException(null);
		}
		if (recievedCompetency.getLevel() > 
			recievedCompetency.getSkill().getMaxLevel()) {
			bindingResult.rejectValue("level", "error.skillLevel",
					"Skill's largest possible level is " + 
							recievedCompetency.getSkill().getMaxLevel());
		}
		if (bindingResult.hasErrors()) {
			model.addAttribute("_VIEW_TITLE", "title.edit.application.competency");
			model.addAttribute("_FORCE_CSRF", true);
			model.addAttribute("create", true);
			model.addAttribute("skillsList", facade.getSkillDAO().getAll());
			model.addAttribute("postAddress", "/applications/suggest/competency");
			return ("editCompetency");
		}
		CompetencyApplication application = new CompetencyApplication();
		application.setLevel(recievedCompetency.getLevel());
		application.setSkillId(recievedCompetency.getSkill().getId());
		Users owner = userDAO.getByUsername((
				(UsernamePasswordAuthenticationToken)principal
				).getName());
		Long ownerId = owner.getProfileId();
		if (ownerId == null) {
			throw new NoResidentUserProfile("", request.getPathInfo());
		}
		application.setOwnerId(ownerId);
		applicationsService.suggestCompetency(application);
		return "redirect:/viewer/profile?id=" + ownerId;
	}
	
	@RequestMapping(path = "/recall/competency",
			method = RequestMethod.POST)
	public String competencyRecall(Principal principal,
			@RequestParam(name = "id", required = true) long id) {
		CompetencyApplication competency = 
				applicationsService.retrieveSuggestedCompetencyById(id);
		Long ownerId = userDAO.getByUsername((
				(UsernamePasswordAuthenticationToken)principal
				).getName())
			.getProfileId();
		if (competency == null || ownerId == null || competency.getOwner() == null ||
				competency.getOwner().getId() != ownerId.longValue()) {
			throw new DataAccessException("Wrong competency specified.");
		}
		applicationsService.deleteSuggestedCompetency(id);
		return "redirect:/viewer/profile?id=" + ownerId;
	}
	
	@RequestMapping(path = "/actions",
			method = RequestMethod.POST)
	public String takeActions(@ModelAttribute ApplicationsPostForm postForm) {
		postForm.getNodes().forEach(x -> {
			switch (x.getState()) {
			case APPROVE:
				applicationsService.approveCompetencyApplication(x.getApplicationId());
				break;
			case REJECT:
				applicationsService.deleteSuggestedCompetency(x.getApplicationId());
				break;
            case NONE: break;
            default: break;
			}
		});
		return "redirect:/dashboard";
	}

}
