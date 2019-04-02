package com.cpms.web.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
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

import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Message;
import com.cpms.data.entities.MessageCenter;
import com.cpms.data.entities.Motivation;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.entities.User;
import com.cpms.web.MessagePostForm;
import com.cpms.web.MotivationPostForm;
import com.cpms.web.ajax.IAjaxAnswer;
import com.cpms.web.ajax.MessagesAnswer;
import com.cpms.web.ajax.MotivationAnswer;

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
	
	@RequestMapping(value = {"/", ""},
			method = RequestMethod.GET)
	public String motivations(Model model, Principal principal,
			HttpServletRequest request) {
		model.addAttribute("_VIEW_TITLE", "navbar.messages");
		model.addAttribute("_FORCE_CSRF", true);
		
		User user = Security.getUser(principal, userDAO);
		List<Message> inMessages = new ArrayList<>();
		if (user == null)
			inMessages = facade.getMessageDAO().getAll();
		else {
			for (MessageCenter mes : user.getInMessages())
				inMessages.add(mes.getMessage());
		}
		model.addAttribute("inMessages", inMessages);
		model.addAttribute("users", userDAO.getAll());
		model.addAttribute("message", new Message());
		
		return "messages";
	}
	
	@RequestMapping(path = {"/delete"}, 
			method = RequestMethod.GET)
	public String motivationDelete(Model model, Principal principal,
			HttpServletRequest request,
			@RequestParam(name = "id", required = true) Long id) {
		Message message = facade.getMessageDAO().getOne(id);
		User user = Security.getUser(principal, userDAO);
		if (user == null)
			facade.getMessageDAO().delete(message);
		else {
			message.removeRecepient(user);
			facade.getMessageDAO().update(message);
		}
		return "redirect:/messages";
	}
	
	@ResponseBody
	@RequestMapping(value = "/ajaxMessage",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxMessage(
			@RequestBody String json, Principal principal) {
		List<Object> values = DashboardAjax.parseJson(json);
		if (values.size() >= 1 && DashboardAjax.isInteger(values.get(0).toString(), 10)) {
			long id = Long.parseLong(values.get(0).toString());
			if (id < 0) {
				// Change message
				id = -id;
				Message message = facade.getMessageDAO().getOne(id);
				return new MessagesAnswer(message, true);
			} else {
				// New message
				MessagesAnswer answer = new MessagesAnswer(new Message(), true);
				answer.setTitle("");
				answer.setText("");
				answer.setParentId("");
				answer.setId(0);
				answer.setOwner(Security.getUser(principal, userDAO));
				if (id > 0) {
					// Reply
					Message reply = facade.getMessageDAO().getOne(id);
					answer.setParentId("" + id);
					answer.setTitle("RE: " + reply.getTitle());
					if (reply.getOwner() != null)
						answer.getRecepients().add(reply.getOwner().getId());
				}
				return answer;
			}
		} else {
			return new MessagesAnswer();
		}
	}
	
	@RequestMapping(path = "/async", 
			method = RequestMethod.POST)
	public String messageCreateAsync(Model model,
			@ModelAttribute MessagePostForm recievedMessage,
			HttpServletRequest request,
			Principal principal) {
		Message message = new Message();
		if (recievedMessage.getId() > 0) 
			message = facade.getMessageDAO().getOne(recievedMessage.getId());
		Long parentId = 0L;
		if (recievedMessage.getParent() != null && recievedMessage.getParent() != "")
			try {
				parentId = Long.parseLong(recievedMessage.getParent());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		Message parent;
		if (parentId > 0)
			parent = facade.getMessageDAO().getOne(parentId);
		else parent = null;
		message.setParent(parent);
		message.setTitle(recievedMessage.getTitle());
		message.setText(recievedMessage.getText());
		message.setOwner(Security.getUser(principal, userDAO));
		if (message.getOwner() == null)
			message.setOwner(userDAO.getAll().get(0));
		
		if (recievedMessage.getId() == 0)
			message = facade.getMessageDAO().insert(message);
		else
			message = facade.getMessageDAO().update(message);

		String[] userIDs = request.getParameterValues("usersTo");
		for (int i = 0; i < userIDs.length; i++) {
			long userId = 0L;
			try { userId = Long.parseLong(userIDs[i]);
			} catch(NumberFormatException e) {}
			User recepient = userDAO.getByUserID(userId);
			if (recepient != null && !message.getRecipients().stream().anyMatch(x -> x.getUser().equals(recepient)))
				message.addRecipient(new MessageCenter(recepient));
		}
		facade.getMessageDAO().update(message);
		return "redirect:/messages";
	}
}
