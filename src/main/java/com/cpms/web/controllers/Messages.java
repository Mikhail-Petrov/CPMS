package com.cpms.web.controllers;

import java.security.Principal;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Message;
import com.cpms.data.entities.MessageCenter;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.entities.User;

/**
 * Handles skill CRUD web application requests.
 * Almost completely deprecated because of {@link SkillTree}.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/messages")
public class Messages {

	@Autowired
	@Qualifier(value = "facade")
	private ICPMSFacade facade;

	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;

	private User getUser(Principal principal) {
		if (principal == null) return null;
		User user = null;
		String username = ((UsernamePasswordAuthenticationToken) principal).getName();
		if (!username.equals(Security.adminName))
			user = userDAO.getByUsername(username);
		return user;
	}
	
	@RequestMapping(value = {"/", ""},
			method = RequestMethod.GET)
	public String motivations(Model model, Principal principal,
			HttpServletRequest request) {
		model.addAttribute("_VIEW_TITLE", "navbar.messages");
		model.addAttribute("_FORCE_CSRF", true);
		
		model.addAttribute("user", getUser(principal));
		
		return "messages";
	}
	
	@RequestMapping(path = {"/delete"}, 
			method = RequestMethod.GET)
	public String motivationDelete(Model model, Principal principal,
			HttpServletRequest request,
			@RequestParam(name = "id", required = true) Long id) {
		Message message = facade.getMessageDAO().getOne(id);
		User user = message.getOwner();
		MessageCenter rec = null;
		for (MessageCenter r : message.getRecipients())
			if (rec == null) rec = r;
		user.removeInMessage(rec);
		facade.getMessageDAO().delete(message);
		user.setHashed(true);
		userDAO.updateUser(user);
		return "redirect:/messages";
	}
	
	@RequestMapping(path = {"/new"}, 
			method = RequestMethod.GET)
	public String motivationCreate(Model model, Principal principal,
			HttpServletRequest request) {
		Message message = new Message();
		User user = getUser(principal);
		List<User> users = userDAO.getAll();
		if (user == null) {
			if (!users.isEmpty())
				user = users.get(0);
		}
		message.setOwner(user);
		(users.size() > 1 ? (users.get(1).equals(user) ? users.get(2) : user) : user).addInMessage(new MessageCenter(message));
		//message.addRecipient(new MessageCenter(users.size() > 1 ? (users.get(1).equals(user) ? users.get(2) : user) : user));
		message.setTitle("test title");
		message.setText("test text");
		
		facade.getMessageDAO().insert(message);
		user.setHashed(true);
		userDAO.updateUser(user);
		return "redirect:/messages";
	}
}
