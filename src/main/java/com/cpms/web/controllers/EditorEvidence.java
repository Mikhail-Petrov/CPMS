package com.cpms.web.controllers;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.CustomDateEditor;
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

import com.cpms.data.EvidenceType;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Evidence;
import com.cpms.data.entities.Profile;
import com.cpms.exceptions.DependentEntityNotFoundException;
import com.cpms.exceptions.SessionExpiredException;
import com.cpms.facade.ICPMSFacade;

/**
 * Handles evidence CRUD web application requests.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/editor")
public class EditorEvidence {
	
	@Autowired
	@Qualifier(value = "facade")
	private ICPMSFacade facade;
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
	    CustomDateEditor editor =
	    		new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true);
	    binder.registerCustomEditor(Date.class, editor);
	}
	
	@RequestMapping(path = "/{profileId}/{competencyId}/evidence", 
			method = RequestMethod.GET)
	public String evidence(Model model, HttpServletRequest request,
			@RequestParam(name = "id", required = false) Long id,
			@PathVariable("profileId") Long profileId,
			@PathVariable("competencyId") Long competencyId) {
		model.addAttribute("_VIEW_TITLE", "title.edit.evidence");
		Profile profile = facade.getProfileDAO().getOne(profileId);
		Competency competency = profile
				.getCompetencies()
				.stream()
				.filter(x -> x.getId() == competencyId)
				.findFirst()
				.orElse(null);
		if (competency == null) {
			throw new DependentEntityNotFoundException(
					Profile.class,
					Competency.class,
					profileId,
					competencyId,
					request.getPathInfo());
		}
		Evidence evidence;
		boolean create;
		if (id == null) {
			evidence = new Evidence();
			create = true;
		} else {
			evidence = competency
					.getEvidence()
					.stream()
					.filter(x -> x.getId() == id)
					.findFirst()
					.orElse(null);
			if (evidence == null) {
				throw new DependentEntityNotFoundException(
						Competency.class,
						Evidence.class,
						competencyId,
						id,
						request.getPathInfo());
			}
			create = false;
		}
		model.addAttribute("postAddress", "/editor/" + profileId + 
				"/" + competencyId + "/evidence");
		model.addAttribute("evidence", evidence);
		model.addAttribute("create", create);
		model.addAttribute("types", Arrays.asList(EvidenceType.values()));
		return "editEvidence";
	}
	
	@RequestMapping(path = "/{profileId}/{competencyId}/evidence",
			method = RequestMethod.POST)
	public String evidenceCreate(Model model, HttpServletRequest request,
			@PathVariable("profileId") Long profileId,
			@PathVariable("competencyId") Long competencyId,
			@ModelAttribute("evidence") @Valid Evidence recievedEvidence,
			BindingResult bindingResult) {
		if (recievedEvidence == null) {
			throw new SessionExpiredException(null);
		}
		if (recievedEvidence.getExpirationDate() != null &&
				recievedEvidence.getExpirationDate().
					before(recievedEvidence.getAcquiredDate())) {
			bindingResult.rejectValue("acquiredDate", "error.evidence",
				"Acquired date must be before expiration");
		}
		boolean create = (recievedEvidence.getId() == 0);
		if (bindingResult.hasErrors()) {
			model.addAttribute("postAddress", "/editor/" + profileId + 
					"/" + competencyId + "/evidence");
			model.addAttribute("create", create);
			model.addAttribute("types", Arrays.asList(EvidenceType.values()));
			model.addAttribute("_VIEW_TITLE", "title.edit.evidence");
			return ("editEvidence");
		}
		Profile profile = facade.getProfileDAO().getOne(profileId);
		Competency competency = profile
				.getCompetencies()
				.stream()
				.filter(x -> x.getId() == competencyId)
				.findFirst()
				.orElse(null);
		if (competency == null) {
			throw new DependentEntityNotFoundException(
					Profile.class,
					Competency.class,
					profileId,
					competencyId,
					request.getPathInfo());
		}
		if (create) {
			competency.addEvidence(recievedEvidence);
		} else {
			Evidence evidence = competency
					.getEvidence()
					.stream()
					.filter(x -> x.getId() == recievedEvidence.getId())
					.findFirst()
					.orElse(null);
			if (evidence == null) {
				throw new DependentEntityNotFoundException(
						Competency.class,
						Evidence.class,
						competencyId,
						recievedEvidence.getId(),
						request.getPathInfo());
			}
			evidence.setAcquiredDate(recievedEvidence.getAcquiredDate());
			evidence.setExpirationDate(recievedEvidence.getExpirationDate());
			evidence.setDescription(recievedEvidence.getDescription());
			evidence.setDescription_RU(recievedEvidence.getDescription_RU());
			evidence.setType(recievedEvidence.getType());
		}
		facade.getProfileDAO().update(profile);
		return "redirect:/viewer/profile?id=" + profile.getId();
	}
	
	@RequestMapping(path = "/{profileId}/{competencyId}/evidenceAsync",
			method = RequestMethod.POST)
	public String evidenceCreateAsync(Model model, HttpServletRequest request,
			@PathVariable("profileId") Long profileId,
			@PathVariable("competencyId") Long competencyId,
			@ModelAttribute("evidence") @Valid Evidence recievedEvidence,
			BindingResult bindingResult) {
		if (recievedEvidence == null) {
			throw new SessionExpiredException(null);
		}
		if (recievedEvidence.getExpirationDate() != null &&
				recievedEvidence.getExpirationDate().
					before(recievedEvidence.getAcquiredDate())) {
			bindingResult.rejectValue("acquiredDate", "error.evidence",
				"Acquired date must be before expiration");
		}
		boolean create = (recievedEvidence.getId() == 0);
		if (bindingResult.hasErrors()) {
			model.addAttribute("types", Arrays.asList(EvidenceType.values()));
			return ("fragments/editEvidenceModal :: evidenceModalForm");
		}
		Profile profile = facade.getProfileDAO().getOne(profileId);
		Competency competency = profile
				.getCompetencies()
				.stream()
				.filter(x -> x.getId() == competencyId)
				.findFirst()
				.orElse(null);
		if (competency == null) {
			throw new DependentEntityNotFoundException(
					Profile.class,
					Competency.class,
					profileId,
					competencyId,
					request.getPathInfo());
		}
		if (create) {
			competency.addEvidence(recievedEvidence);
		} else {
			Evidence evidence = competency
					.getEvidence()
					.stream()
					.filter(x -> x.getId() == recievedEvidence.getId())
					.findFirst()
					.orElse(null);
			if (evidence == null) {
				throw new DependentEntityNotFoundException(
						Competency.class,
						Evidence.class,
						competencyId,
						recievedEvidence.getId(),
						request.getPathInfo());
			}
			evidence.setAcquiredDate(recievedEvidence.getAcquiredDate());
			evidence.setExpirationDate(recievedEvidence.getExpirationDate());
			evidence.setDescription(recievedEvidence.getDescription());
			evidence.setDescription_RU(recievedEvidence.getDescription_RU());
			evidence.setType(recievedEvidence.getType());
		}
		facade.getProfileDAO().update(profile);
		return "fragments/editEvidenceModal :: evidenceCreationSuccess";
	}
	
	@RequestMapping(path = "/evidence/delete", 
			method = RequestMethod.GET)
	public String evidenceDelete(Model model, HttpServletRequest request,
			@RequestParam(name = "id", required = true) Integer id,
			@RequestParam(name = "profileId", required = true) Integer profileId,
			@RequestParam(name = "competencyId", required = true) Integer competencyId) {
		Profile profile = facade.getProfileDAO().getOne(profileId);
		Competency competency = profile
				.getCompetencies()
				.stream()
				.filter(x -> x.getId() == competencyId)
				.findFirst()
				.orElse(null);
		if (competency == null) {
			throw new DependentEntityNotFoundException(
					Profile.class,
					Competency.class,
					profileId,
					competencyId,
					request.getPathInfo());
		}
		Evidence evidence = competency
				.getEvidence()
				.stream()
				.filter(x -> x.getId() == id)
				.findFirst()
				.orElse(null);
		if (evidence == null) {
			throw new DependentEntityNotFoundException(
					Competency.class,
					Evidence.class,
					competencyId,
					id,
					request.getPathInfo());
		}
		competency.removeEvidence(evidence);
		facade.getProfileDAO().update(profile);
		return "redirect:/viewer/profile?id=" + profile.getId();
	}
	
}
