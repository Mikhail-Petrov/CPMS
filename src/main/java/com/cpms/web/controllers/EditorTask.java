package com.cpms.web.controllers;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Language;
import com.cpms.data.entities.Message;
import com.cpms.data.entities.MessageCenter;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Requirements;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TaskCenter;
import com.cpms.data.entities.TaskRequirement;
import com.cpms.exceptions.DependentEntityNotFoundException;
import com.cpms.exceptions.SessionExpiredException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.entities.Users;
import com.cpms.web.ajax.GroupAnswer;
import com.cpms.web.ajax.IAjaxAnswer;

/**
 * Handles task CRUD web application requests.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/editor")
public class EditorTask {

	@Autowired
	@Qualifier(value = "facade")
	private ICPMSFacade facade;

	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;

    @Autowired
	@Qualifier(value = "mailSender")
    public JavaMailSender emailSender;

	@RequestMapping(path = "/{taskId}/requirementAsyncNew", method = RequestMethod.POST)
	public String competencyCreateAsyncNew(Model model, HttpServletRequest request, @PathVariable("taskId") Long taskId,
			@ModelAttribute("requirement") @Valid TaskRequirement recievedRequirement, BindingResult bindingResult) {
		String[] skillIDs = request.getParameterValues("skillIDs");
		if (recievedRequirement == null) {
			throw new SessionExpiredException(null);
		}
		Task task = facade.getTaskDAO().getOne(taskId);
		for (String compID : skillIDs) {
			long skillID = 0;
			try {
				skillID = Long.parseLong(compID);
			} catch (NumberFormatException e) {
				continue;
			}
			Skill skill = facade.getSkillDAO().getOne(skillID);
			if (skill == null)
				continue;
			if (!task.getRequirements().stream().anyMatch(x -> x.getSkill().equals(skill))) {
				task.addRequirement(new TaskRequirement(skill, skill.getMaxLevel()));
			}
		}
		facade.getTaskDAO().update(task);
		return "fragments/editRequirementModal :: requirementCreationSuccess";
	}

	@RequestMapping(path = "/{taskId}/requirementAsync", method = RequestMethod.POST)
	public String competencyCreateAsync(Model model, HttpServletRequest request, @PathVariable("taskId") Long taskId,
			@ModelAttribute("requirement") @Valid TaskRequirement recievedRequirement, BindingResult bindingResult) {
		if (recievedRequirement == null) {
			throw new SessionExpiredException(null);
		}
		if (recievedRequirement.getLevel() > recievedRequirement.getSkill().getMaxLevel()) {
			bindingResult.rejectValue("level", "error.skillLevel",
					"Skill's largest possible level is " + recievedRequirement.getSkill().getMaxLevel());
		}
		Task task = facade.getTaskDAO().getOne(taskId);
		if (task.getRequirements().stream().anyMatch(x -> x.getSkill().equals(recievedRequirement.getSkill()))) {
			bindingResult.rejectValue("skill", "error.skill", "Such skill is already used.");
		}
		boolean create = (recievedRequirement.getId() == 0);
		if (bindingResult.hasErrors()) {
			return ("fragments/editRequirementModal :: requirementModalForm");
		}
		if (create) {
			task.addRequirement(recievedRequirement);
		} else {
			TaskRequirement requirement = task.getRequirements().stream()
					.filter(x -> x.getId() == recievedRequirement.getId()).findFirst().orElse(null);
			if (requirement == null) {
				throw new DependentEntityNotFoundException(Profile.class, Competency.class, taskId,
						recievedRequirement.getId(), request.getPathInfo());
			}
			requirement.setSkill(recievedRequirement.getSkill());
			requirement.setLevel(recievedRequirement.getLevel());
		}
		facade.getTaskDAO().update(task);
		return "fragments/editRequirementModal :: requirementCreationSuccess";
	}

	private Task existedTask = null;
	
	@RequestMapping(path = "/task", method = RequestMethod.GET)
	public String task(Model model, Principal principal, @RequestParam(name = "id", required = false) Long id) {
		model.addAttribute("_VIEW_TITLE", "title.edit.task");
		model.addAttribute("_FORCE_CSRF", true);
		Task task;
		boolean create = false;
		if (existedTask != null)
			task = existedTask;
		else if (id == null) {
			task = new Task();
			create = true;
		} else {
			task = facade.getTaskDAO().getOne(id);
			create = false;
		}
		existedTask = null;
		model.addAttribute("task", task);
		model.addAttribute("create", create);
		List<Language> langs = facade.getLanguageDAO().getAll();
		Collections.sort(langs);
		model.addAttribute("languages", langs);
		List<Skill> skills = facade.getSkillDAO().getAll();
		Collections.sort(skills);
		model.addAttribute("skills", skills);
		List<Users> users = userDAO.getAll();
		Collections.sort(users);
		model.addAttribute("users", users);
		List<String> names = new ArrayList<>();
		for (Task curTask : facade.getTaskDAO().getAll())
			if (!curTask.equals(task))
				names.add(curTask.getName());
		model.addAttribute("names", names);
		List<Long> performers = new ArrayList<>();
		for (TaskCenter recepient : task.getRecipients())
			performers.add(recepient.getUser().getId());
		if (performers.size() == users.size()) {
			performers.clear();
			performers.add(-1L);
		}
		model.addAttribute("performers", performers);
		return "editTask";
	}
	
	@RequestMapping(path = "/task", method = RequestMethod.POST)
	public String taskCreate(Model model, HttpServletRequest request, @ModelAttribute("task") @Valid Task recievedTask, @RequestParam(required=false, name="file") MultipartFile file,
			BindingResult bindingResult, Principal principal) {
		if (recievedTask == null) {
			throw new SessionExpiredException(null);
		}
		boolean create = (recievedTask.getId() == 0);
		Task task;
		if (create) {
			recievedTask.setStatus("1");
			if (recievedTask.getDueDate() == null)
				return "redirect:/viewer/task";
			recievedTask.setCreatedDate(new Date(System.currentTimeMillis()));
			Users owner = Security.getUser(principal, userDAO);
			if (owner == null)
				owner = userDAO.getAll().get(0);
			recievedTask.setUser(owner);
			if (file != null && !file.isEmpty()) {
				try {
					recievedTask.setImage(file.getBytes());
				} catch (IOException e) {}
				recievedTask.setImageType(file.getContentType());
			}
			task = facade.getTaskDAO().insert(recievedTask);
		} else {
			task = facade.getTaskDAO().getOne(recievedTask.getId());
			if (recievedTask.getDueDate() == null)
				recievedTask.setDueDate(task.getDueDate());
			task.update(recievedTask);
			if (file != null && !file.isEmpty()) {
				try {
					task.setImage(file.getBytes());
				} catch (IOException e) {}
				task.setImageType(file.getContentType());
			}
			task = facade.getTaskDAO().update(task);
		}

		// identify performers
		String[] userIDs = request.getParameterValues("performers");
		List<Long> performers = new ArrayList<>();
		if (userIDs != null)
			if (userIDs.length > 0 && userIDs[0].equals("all"))
				for (Users user : userDAO.getAll())
					performers.add(user.getId());
			else
				for (int i = 0; i < userIDs.length; i++)
					try { performers.add(Long.parseLong(userIDs[i]));
					} catch (NumberFormatException e) {}
		// forget about old performers
		List<Users> toRemove = new ArrayList<Users>();
		if (!create)
			for (TaskCenter center : task.getRecipients())
				if (performers.contains(center.getUser().getId()))
					performers.remove(center.getUser().getId());
				else
					toRemove.add(center.getUser());
		for (Users oldUser : toRemove) {
			CommonModelAttributes.newTask.put(oldUser.getId(), -1);
			task.removeRecepient(oldUser);
		}
		// add new performers in the task and send them messages
		String title = String.format("New translation task: %s (id: %d)", task.getPresentationName(), task.getId());
		String text = "New proofreading task has been assigned to you.";
		String type = "2";
		String url = request.getRequestURL().toString();
		for (Long perfID : performers) {
			Users newRecipient = userDAO.getByUserID(perfID);
			task.addRecipient(new TaskCenter(newRecipient));
			CommonModelAttributes.newTask.put(perfID, -1);
			Messages.createSendMessage(task, principal, userDAO, title, text, type, newRecipient, url, emailSender, facade);
		}
		facade.getTaskDAO().update(task);
		return "redirect:/viewer/task?id=" + task.getId();
	}
	
	public static Message createTaskMessage(Task task, Principal principal, IUserDAO userDAO) {
		Message newMessage = new Message();
		newMessage.setTask(task);
		Users owner = Security.getUser(principal, userDAO);
		newMessage.setOwner(owner);
		if (newMessage.getOwner() == null)
			newMessage.setOwner(userDAO.getAll().get(0));
		newMessage.setTitle(String.format("New translation task: %s (id: %d)", task.getPresentationName(), task.getId()));
		newMessage.setText(
				String.format("Translation from language '%s' to language '%s'.",
					task.getSource() == null ? "" : task.getSource().getCode(),
					task.getTarget() == null ? "" : task.getTarget()));
		newMessage.setType("2");
		return newMessage;
		
	}

	@RequestMapping(path = "/taskAsync", method = RequestMethod.POST)
	public String taskCreateAsync(Model model, @ModelAttribute("task") @Valid Task recievedTask, HttpServletRequest request, Principal principal, @RequestParam("file") MultipartFile file,
			BindingResult bindingResult) {
		if (recievedTask == null) {
			throw new SessionExpiredException(null);
		}
		if (recievedTask.getName().length() < 5 || recievedTask.getName().length() > 100)
			throw new SessionExpiredException(null);
		boolean create = (recievedTask.getId() == 0);
		if (bindingResult.hasErrors()) {
			return ("fragments/editTaskModal :: taskModalForm");
		}
		Task task;
		if (create) {
			recievedTask.setStatus("1");
			Users owner = Security.getUser(principal, userDAO);
			if (owner == null)
				owner = userDAO.getAll().get(0);
			recievedTask.setUser(owner);
			if (file != null && !file.isEmpty()) {
				try {
					recievedTask.setImage(file.getBytes());
				} catch (IOException e) {}
				recievedTask.setImageType(file.getContentType());
			}
			task = facade.getTaskDAO().insert(recievedTask);
			for (TaskCenter center : task.getRecipients())
				CommonModelAttributes.newTask.put(center.getUser().getId(), -1);
		} else {
			task = facade.getTaskDAO().getOne(recievedTask.getId());
			task.setAbout(recievedTask.getAbout());
			task.setOriginal(recievedTask.getOriginal());
			task.setName(recievedTask.getName());
			task = facade.getTaskDAO().update(task);
		}

		// identify performers for a message
		String[] userIDs = request.getParameterValues("performers");
		List<Long> performers = new ArrayList<>();
		if (userIDs != null)
			if (userIDs[0].equals("all"))
				for (Users user : userDAO.getAll())
					performers.add(user.getId());
			else
				for (int i = 0; i < userIDs.length; i++)
					try { performers.add(Long.parseLong(userIDs[i]));
					} catch (NumberFormatException e) {}

		// forget about old performers
		if (!create)
			for (TaskCenter center : task.getRecipients())
 				performers.remove(center.getUser().getId());
		if (!performers.isEmpty()) {
			Message newMessage = createTaskMessage(task, principal, userDAO);
			for (MessageCenter center : newMessage.getRecipients())
				CommonModelAttributes.newMes.put(center.getUser().getId(), -1);
			newMessage = facade.getMessageDAO().insert(newMessage);
			String url = request.getRequestURL().toString().replace("editor/task/status", "messages");
			for (long userID : performers) {
				Users newRecipient = userDAO.getByUserID(userID);
				newMessage.addRecipient(new MessageCenter(newRecipient));
				task.addRecipient(new TaskCenter(newRecipient));
				Messages.sendMessageEmail(url, emailSender, newRecipient, newMessage.getText());
			}
			for (MessageCenter center : newMessage.getRecipients())
				CommonModelAttributes.newMes.put(center.getUser().getId(), -1);
			facade.getMessageDAO().update(newMessage);
			facade.getTaskDAO().update(task);
		}
		for (TaskCenter center : task.getRecipients())
			CommonModelAttributes.newTask.put(center.getUser().getId(), -1);
		return "fragments/editTaskModal :: taskCreationSuccess";
	}

	@RequestMapping(path = { "/task/send" }, method = RequestMethod.POST)
	public String taskSend(Model model, Principal principal, @ModelAttribute("task_id") @Valid long id,
			@ModelAttribute("message_text") @Valid String text, @ModelAttribute("isFinal") @Valid String isFinal) {
		Task task = facade.getTaskDAO().getOne(id);
		String returnVal = "redirect:/viewer/task?id=" + task.getId();
		if (isFinal.equals("1"))
			for (Message mes : task.getMessages())
				if (mes.getType().equals("f")) {
					mes.setType("3");
					facade.getMessageDAO().update(mes);
				}
		if (text.isEmpty()) {
			if (isFinal.equals("1")) {
				List<Message> messages = task.getMessages();
				Collections.sort(messages);
				Message message = messages.get(0);
				message.setType("f");
				facade.getMessageDAO().update(message);
			}
			return returnVal;
		}
		Message message = new Message();
		Users owner = Security.getUser(principal, userDAO);
		if (owner == null)
			owner = userDAO.getAll().get(0);
		message.setOwner(owner);
		message.setTask(task);
		message.setType(isFinal.equals("1") ? "f" : "3");
		message.setText(text);
		message.setTitle("");
		for (MessageCenter center : message.getRecipients())
			CommonModelAttributes.newMes.put(center.getUser().getId(), -1);
		facade.getMessageDAO().insert(message);
		return returnVal;
	}

	@RequestMapping(path = "/task/delete", method = RequestMethod.GET)
	public String taskDelete(Model model, @RequestParam(name = "id", required = true) Long id) {
		Task task = facade.getTaskDAO().getOne(id);
		for (Message mes : task.getMessages()) {
			mes.setTask(null);
			facade.getMessageDAO().update(mes);
		}
		for (TaskCenter center : task.getRecipients())
			CommonModelAttributes.newTask.put(center.getUser().getId(), -1);
		facade.getTaskDAO().delete(task);
		return "redirect:/viewer/tasks";
	}

	@RequestMapping(path = "/task/status", method = RequestMethod.GET)
	public String taskStatus(Model model, @RequestParam(name = "id", required = true) Long id, @RequestParam(name = "status", required = true) String status,
			Principal principal, HttpServletRequest request) {
		Task task = facade.getTaskDAO().getOne(id);
		if (status.startsWith("-")) {
			task.setStatus("1");
			task.setCompletedDate(null);
		} else {
			task.setStatus(status.equals("1") ? "2" : "3");
			if (task.getStatus().equals("2"))
				task.setCompletedDate(new Date(System.currentTimeMillis()));
		}
		for (TaskCenter center : task.getRecipients())
			CommonModelAttributes.newTask.put(center.getUser().getId(), -1);
		// send messages
		String title = String.format("New translation task status: %s (id: %d)", task.getPresentationName(), task.getId());
		String type = "2";
		String url = request.getRequestURL().toString();
		if (task.getStatus().equals("2")) {
			Users recipient = task.getUser(), sender = Security.getUser(principal, userDAO);
			if (sender == null)
				sender = userDAO.getAll().get(0);
			String text = String.format("The task has been resolved. Please, approve or decline it.");
			Messages.createSendMessage(task, principal, userDAO, title, text, type, recipient, url, emailSender, facade);
		} else if (task.getStatus().equals("3")) {
			String text = "The task has been approved.";
			for (TaskCenter center : task.getRecipients())
				Messages.createSendMessage(task, principal, userDAO, title, text, type, center.getUser(), url, emailSender, facade);
		} else {
			String text = "The task has been returned.";
			for (TaskCenter center : task.getRecipients())
				Messages.createSendMessage(task, principal, userDAO, title, text, type, center.getUser(), url, emailSender, facade);
		}
		facade.getTaskDAO().update(task);
		return "redirect:/viewer/tasks";
	}

	@RequestMapping(path = { "/task/saveChanges" }, method = RequestMethod.POST)
	public String profileSave(Model model, HttpServletRequest request,
			@ModelAttribute("requirements") @Valid Requirements requirements, BindingResult bindingResult) {
		Task task = facade.getTaskDAO().getOne(requirements.getTaskId());
		boolean change = false;

		HashMap<Long, Integer> changes = requirements.getChanges();
		for (Entry<Long, Integer> requirChange : changes.entrySet()) {
			TaskRequirement requirement = task.getRequirements().stream()
					.filter(x -> x.getId() == requirChange.getKey()).findFirst().orElse(null);
			if (requirement == null) {
				throw new DependentEntityNotFoundException(Task.class, TaskRequirement.class, task.getId(),
						requirChange.getKey(), request.getPathInfo());
			}
			if (requirement.getLevel() != requirChange.getValue()) {
				requirement.setLevel(requirChange.getValue());
				change = true;
			}
		}

		if (change)
			facade.getTaskDAO().update(task);
		return "redirect:/viewer/task?id=" + task.getId();
	}
	
	@ResponseBody
	@RequestMapping(value = "/ajaxGroup",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxGroup(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json);
		if (values.size() >= 3 && DashboardAjax.isInteger(values.get(0).toString(), 10)) {
			Task task = new Task();
			String[] requirements = values.get(0).toString().split(",");
			for (int i = 0; i < requirements.length; i++) {
				String[] reqVals = requirements[i].split("(")[1].split("): ");
				long id = Long.parseLong(reqVals[0]);
				int lvl = Integer.parseInt(reqVals[1]);
				Skill skill = facade.getSkillDAO().getOne(id);
				task.addRequirement(new TaskRequirement(skill, lvl));
			}
			List<Language> langs = facade.getLanguageDAO().getAll();
			task.setSource(Language.findByCode(values.get(1).toString(), langs));
			task.setTarget(values.get(2).toString());
			return new GroupAnswer(facade, userDAO, task);
		} else {
			return new GroupAnswer();
		}
	}
}
