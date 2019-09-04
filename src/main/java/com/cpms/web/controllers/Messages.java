package com.cpms.web.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
import com.cpms.data.entities.TaskCenter;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.entities.Users;
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

    @Autowired
	@Qualifier(value = "mailSender")
    public JavaMailSender emailSender;
	
	@RequestMapping(value = {"/", ""},
			method = RequestMethod.GET)
	public String motivations(Model model, Principal principal,
			HttpServletRequest request) {
		model.addAttribute("_VIEW_TITLE", "navbar.messages");
		model.addAttribute("_FORCE_CSRF", true);
		
		Users user = Security.getUser(principal, userDAO);
		List<Message> messages = new ArrayList<>();
		List<MessageCenter> inMessages = new ArrayList<>();
		if (user == null) {
			messages = facade.getMessageDAO().getAll();
			for (Message mes : messages)
				inMessages.addAll(mes.getRecipients());
		} else {
			for (MessageCenter mes : user.getInMessages())
				inMessages.add(mes);
			for (Message mes : user.getMessages())
				messages.add(mes);
		}
		Collections.sort(inMessages);
		model.addAttribute("inMessages", inMessages);
		Collections.sort(messages);
		model.addAttribute("messages", messages);
		model.addAttribute("users", userDAO.getAll());
		model.addAttribute("message", new Message());
		
		return "messages";
	}
	
	private void deleteMessage(Principal principal, Long id) {
		if (id <= 0) return;
		Message message = facade.getMessageDAO().getOne(id);
		if (message == null) return;
		Users user = Security.getUser(principal, userDAO);
		if (user == null)
			facade.getMessageDAO().delete(message);
		else {
			CommonModelAttributes.newMes.put(user.getId(), -1);
			message.removeRecepient(user);
			facade.getMessageDAO().update(message);
		}
	}
	
	private Message readMessage(Principal principal, Long id) {
		if (id <= 0) return null;
		Message message = facade.getMessageDAO().getOne(id);
		if (message == null) return null;
		Users curUser = Security.getUser(principal, userDAO);
		boolean isChanged = false;
		for (MessageCenter messageCenter : message.getRecipients())
			if (messageCenter.getUser().equals(curUser) && !messageCenter.isRed()) {
				isChanged = true;
				messageCenter.setRed(true);
				break;
			}
		if (isChanged) {
			message = facade.getMessageDAO().update(message);
			for (MessageCenter center : message.getRecipients())
				CommonModelAttributes.newMes.put(center.getUser().getId(), -1);
		}
		return message;
	}
	
	@RequestMapping(path = {"/delete"}, 
			method = RequestMethod.GET)
	public String motivationDelete(Model model, Principal principal,
			HttpServletRequest request,
			@RequestParam(name = "id", required = true) Long id) {
		deleteMessage(principal, id);
		return "redirect:/messages";
	}
	
	@RequestMapping(path = {"/selected/delete"}, 
			method = RequestMethod.GET)
	public String selectedDelete(Model model, Principal principal,
			@RequestParam(name = "ids", required = true) String ids) {
		String[] split = ids.split(",");
		for (int i = 1; i < split.length; i++) {
			long id = 0;
			try {
				id = Long.parseLong(split[i]);
			} catch (NumberFormatException e) {}
			deleteMessage(principal, id);
		}
		return "redirect:/messages";
	}
	
	@RequestMapping(path = {"/selected/read"}, 
			method = RequestMethod.GET)
	public String selectedRead(Model model, Principal principal,
			@RequestParam(name = "ids", required = true) String ids) {
		String[] split = ids.split(",");
		for (int i = 1; i < split.length; i++) {
			long id = 0;
			try {
				id = Long.parseLong(split[i]);
				if (id <= 0) continue;
				readMessage(principal, id);
			} catch (NumberFormatException e) {}
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
				// Change/view message
				id = -id;
				return new MessagesAnswer(readMessage(principal, id), true);
			} else {
				// New message
				MessagesAnswer answer = new MessagesAnswer(new Message(), true);
				answer.setTitle("");
				answer.setText("");
				answer.setParentId("");
				answer.setType("1");
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
		List<Long> oldTo = new ArrayList<Long>();
		for (MessageCenter center : message.getRecipients())
			oldTo.add(center.getUser().getId());
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
			Users recepient = userDAO.getByUserID(userId);
			if (recepient != null && !message.getRecipients().stream().anyMatch(x -> x.getUser().equals(recepient)))
				message.addRecipient(new MessageCenter(recepient));
		}
		facade.getMessageDAO().update(message);
		for (MessageCenter center : message.getRecipients())
			CommonModelAttributes.newMes.put(center.getUser().getId(), -1);
		for (MessageCenter center : message.getRecipients())
			if (!oldTo.contains(center.getUser().getId()))
				Messages.sendEmail(request, emailSender, center.getUser(), message.getText());
		return "redirect:/messages";
	}
	
	public static void sendEmail(HttpServletRequest request, JavaMailSender emailSender, Users to, String text) {
		
		if (emailSender == null || to == null || to.getEmail() == null || to.getEmail().isEmpty())
			return;
		
        text = "Dear " + to.getUsername() + ",<br>You have got new message in the CPM system.<br>" + text;
        String url = request.getRequestURL().toString().replace(request.getRequestURI(), "");
        text += "<br>Please, look through by the following link: <a href='" + url + "'>" + url + "</a><br>CPM system administartor";
        
		// Create a Simple MailMessage.
        //SimpleMailMessage message = new SimpleMailMessage();
        
        try {
			MimeMessage message = emailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setSubject("Proofreading: new message");
			helper.setTo(to.getEmail());
			helper.setText(text, true);
         
        /*message.setTo(to.getEmail());
        message.setSubject("Proofreading: new message");
        message.setText(text);*/
 
        // Send Message!
        	emailSender.send(message);
        } catch (MailException | MessagingException e) {
        	e.printStackTrace();
        }
	}
}
