package com.cpms.web.controllers;

import java.io.IOException;
import java.math.BigInteger;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

import com.cpms.dao.interfaces.IDAO;
import com.cpms.dao.interfaces.IDraftableSkillDaoExtension;
import com.cpms.dao.interfaces.IInnovationTermDAO;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Category;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Language;
import com.cpms.data.entities.Message;
import com.cpms.data.entities.MessageCenter;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.ProjectTermvariant;
import com.cpms.data.entities.Requirements;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TaskCenter;
import com.cpms.data.entities.TaskRequirement;
import com.cpms.data.entities.Task_Category;
import com.cpms.data.entities.Task_Trend;
import com.cpms.data.entities.Term;
import com.cpms.data.entities.TermVariant;
import com.cpms.data.entities.Trend;
import com.cpms.exceptions.DependentEntityNotFoundException;
import com.cpms.exceptions.SessionExpiredException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.entities.Users;
import com.cpms.web.SkillUtils;
import com.cpms.web.ajax.GroupAnswer;
import com.cpms.web.ajax.IAjaxAnswer;
import com.cpms.web.ajax.InnAnswer;

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
    
	@Autowired
	@Qualifier(value = "termDAO")
	private IDAO<Term> termDAO;

    @Autowired
    private MessageSource messageSource;

	@Autowired
	@Qualifier("draftableSkillDAO")
	private IDraftableSkillDaoExtension skillDao;
	
	@Autowired
	@Qualifier(value = "innovationDAO")
	private IInnovationTermDAO innDAO;

	@ResponseBody
	@RequestMapping(value = "/task/ajaxTaskTermSearch",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxTaskTermSearch(
			@RequestBody String json) {
		Statistic.time();
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

	@RequestMapping(path = "/{taskId}/requirementAsyncNew", method = RequestMethod.POST)
	public String competencyCreateAsyncNew(Model model, HttpServletRequest request, @PathVariable("taskId") Long taskId,
			@ModelAttribute("requirement") @Valid TaskRequirement recievedRequirement, BindingResult bindingResult) {
		String[] skillIDs = request.getParameterValues("skillIDs");
		if (skillIDs != null && skillIDs.length > 0)
			skillIDs = skillIDs[0].split(",");
		String[] trendIDs = request.getParameterValues("trendIDs");
		if (trendIDs != null && trendIDs.length > 0)
			trendIDs = trendIDs[0].split(",");
		String[] categoryIDs = request.getParameterValues("categoryIDs");
		if (categoryIDs != null && categoryIDs.length > 0)
			categoryIDs = categoryIDs[0].split(",");
		if (recievedRequirement == null) {
			throw new SessionExpiredException(null, messageSource);
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
		for (String trID : trendIDs) {
			long trendID = 0;
			try {
				trendID = Long.parseLong(trID);
			} catch (NumberFormatException e) {
				continue;
			}
			Trend trend = facade.getTrendDAO().getOne(trendID);
			if (trend == null)
				continue;
			if (!task.getTrends().stream().anyMatch(x -> x.getTrend().equals(trend))) {
				task.addTrend(new Task_Trend(trend, task));
			}
		}
		for (String catID : categoryIDs) {
			long categID = 0;
			try {
				categID = Long.parseLong(catID);
			} catch (NumberFormatException e) {
				continue;
			}
			Category category = facade.getCategoryDAO().getOne(categID);
			if (category == null)
				continue;
			if (!task.getCategories().stream().anyMatch(x -> x.getCategory().equals(category))) {
				task.addCategory(new Task_Category(category, task));
			}
		}
		facade.getTaskDAO().update(task);
		return "fragments/editRequirementModal :: requirementCreationSuccess";
	}

	@RequestMapping(path = "/{taskId}/requirementAsync", method = RequestMethod.POST)
	public String competencyCreateAsync(Model model, HttpServletRequest request, @PathVariable("taskId") Long taskId,
			@ModelAttribute("requirement") @Valid TaskRequirement recievedRequirement, BindingResult bindingResult) {
		if (recievedRequirement == null) {
			throw new SessionExpiredException(null, messageSource);
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
						recievedRequirement.getId(), request.getPathInfo(), messageSource);
			}
			requirement.setSkill(recievedRequirement.getSkill());
			requirement.setLevel(recievedRequirement.getLevel());
		}
		facade.getTaskDAO().update(task);
		return "fragments/editRequirementModal :: requirementCreationSuccess";
	}

	private Task existedTask = null;
	
	@RequestMapping(path = "/task", method = RequestMethod.GET)
	public String task(Model model, Principal principal, @RequestParam(name = "id", required = false) Long id
			, @RequestParam(name = "term", required = false) Long termid, @RequestParam(name = "var", required = false) String var) {
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
		// fill some fields for innovation
		String termVariant = "";
		if (termid != null && var != null) {
			Term term = termDAO.getOne(termid);
			if (term != null) {
				for (TermVariant tv : term.getVariants())
					if (tv.getText().equals(var)) {
						task.setName(var);
						task.addVariant(new ProjectTermvariant(task, tv));
						Date dueDate = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000);
						task.setDueDate(dueDate);
						task.setStatus("1");
						task.setCost(0);
						task.setImpact(0);
						task.setProjectType(1);
						task.setVariant(tv);
						if (!term.isInn()) {
							term.setInn(true);
							termDAO.update(term);
						}
						termVariant = String.format("%d:%d", term.getId(), tv.getId());
						break;
					}
				// get categories and trends for the task
				/*List<Object[]> catTrends = innDAO.getCatTrendForTerm(term);
				for (Object[] ct : catTrends) {
					if (ct.length < 2) continue;
					if (ct[1].equals("cat")) {
						Category category = facade.getCategoryDAO().getOne(((BigInteger) ct[0]).longValue());
						if (category != null)
							task.addCategory(new Task_Category(category, task));
					} else if (ct[1].equals("trend")) {
						Trend trend = facade.getTrendDAO().getOne(((BigInteger) ct[0]).longValue());
						if (trend != null)
							task.addTrend(new Task_Trend(trend, task));
					}
				}*/
			}
		}
		model.addAttribute("termVariant", termVariant);
		model.addAttribute("task", task);
		model.addAttribute("create", create);
		List<Language> langs = facade.getLanguageDAO().getAll();
		Collections.sort(langs);
		model.addAttribute("languages", langs);
		List<Users> users = userDAO.getAll();
		for (int i = users.size() - 1; i >= 0; i--)
			if (users.get(i).getProfileId() == null)
				users.remove(i);
		Collections.sort(users);
		model.addAttribute("users", users);
		List<String> names = new ArrayList<>();
		for (Task curTask : facade.getTaskDAO().getAll())
			if (curTask.getDelDate() == null && !curTask.equals(task))
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
		List<String> terms = new ArrayList<>();
		for (ProjectTermvariant tvar : task.getVariants())
			terms.add(tvar.getVariant().getTerm().getId() + "~!@" + tvar.getVariant().getId() + "~!@" + tvar.getVariant().getText());
		model.addAttribute("terms", terms);
		List<String> trends = new ArrayList<>();
		for (Task_Trend tt : task.getTrends())
			trends.add(tt.getTrend().getId() + "~!@" + tt.getTrend().getName());
		model.addAttribute("trends", trends);
		List<String> categories = new ArrayList<>();
		for (Task_Category tt : task.getCategories())
			categories.add(tt.getCategory().getId() + "~!@" + tt.getCategory().getName());
		model.addAttribute("categories", categories);
		List<String[]> reqs = new ArrayList<>();
		for (TaskRequirement tr : task.getRequirements()) {
			if (tr.getSkill().getDelDate() != null)
				continue;
			String[] req = {String.format("|%s (%d)", tr.getSkill().getName(), tr.getSkill().getId()), tr.getLevel() + ""};
			reqs.add(req);
		}
		model.addAttribute("reqs", reqs);
		return "editTask";
	}

	private void updateVariants(Task task, List<String> terms) {
		Set<ProjectTermvariant> oldVars = task.getVariants();
		task.clearVariants();
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
					for (ProjectTermvariant ct : oldVars)
						if (ct.getVariant().getId() == var.getId()) {
							task.addVariant(ct);
							isOld = true;
							break;
						}
					if (!isOld)
						task.addVariant(new ProjectTermvariant(task, var));
					break;
				}
		}
	}
	private void updateTrendsCategories(Task task, List<String> trends, boolean isTrend) {
		Set<Task_Trend> oldVars = task.getTrends();
		Set<Task_Category> oldCats = task.getCategories();
		if (isTrend)
			task.clearTrends();
		else
			task.clearCategories();
		if (trends != null)
		for (String strend : trends) {
			long trendid = 0, varid = 0;
			try {
				trendid = Long.parseLong(strend);
			} catch (NumberFormatException e) {}
			boolean isOld = false;
			if (isTrend) {
				Trend trend = facade.getTrendDAO().getOne(trendid);
				if (trend == null) continue;
				for (Task_Trend tt : oldVars)
					if (tt.getTrend().getId() == trend.getId()) {
						task.addTrend(tt);
						isOld = true;
						break;
					}
				if (!isOld)
					task.addTrend(new Task_Trend(trend, task));
			} else {
				Category category = facade.getCategoryDAO().getOne(trendid);
				if (category == null) continue;
				for (Task_Category tc : oldCats)
					if (tc.getCategory().getId() == category.getId()) {
						task.addCategory(tc);
						isOld = true;
						break;
					}
				if (!isOld)
					task.addCategory(new Task_Category(category, task));
			}
		}
	}
	@RequestMapping(path = "/task", method = RequestMethod.POST)
	public String taskCreate(Model model, HttpServletRequest request
			, @ModelAttribute("task") @Valid Task recievedTask, @RequestParam(required=false, name="file") MultipartFile file,
			BindingResult bindingResult, Principal principal, @RequestParam(required=false, name="skills") String skills
			, @RequestParam(required=false, name="termVariant") String termVariant
			, @RequestParam(required=false, name="trendIDs") List<String> trendIDs
			, @RequestParam(required=false, name="categoryIDs") List<String> categoryIDs
			, @RequestParam(required=false, name="terms") List<String> terms) {
		if (recievedTask == null) {
			throw new SessionExpiredException(null, messageSource);
		}
		boolean create = (recievedTask.getId() == 0);
		Task task;
		if (create) {
			recievedTask.setStatus("1");
			recievedTask.setCost(0);
			recievedTask.setImpact(0);
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
			updateVariants(recievedTask, terms);
			updateTrendsCategories(recievedTask, trendIDs, true);
			updateTrendsCategories(recievedTask, categoryIDs, false);
			if (termVariant != null) {
				String[] split = termVariant.split(":");
				long termID = 0, varID = 0;
				if (split.length > 1)
				try {
					termID = Long.parseLong(split[0]);
					varID = Long.parseLong(split[1]);
				} catch (NumberFormatException e) {}
				Term term = facade.getTermDAO().getOne(termID);
				if (term != null)
					for (TermVariant tv : term.getVariants())
						if (tv.getId() == varID)
							recievedTask.setVariant(tv);
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
			updateVariants(task, terms);
			updateTrendsCategories(task, trendIDs, true);
			updateTrendsCategories(task, categoryIDs, false);
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
		String title = String.format("New terminology task: %s (id: %d)", task.getPresentationName(), task.getId());
		String text = "You have been assigned a new terminology task.";
		String type = "2";
		String url = request.getRequestURL().toString();
		for (Long perfID : performers) {
			Users newRecipient = userDAO.getByUserID(perfID);
			task.addRecipient(new TaskCenter(newRecipient));
			CommonModelAttributes.newTask.put(perfID, -1);
			Messages.createSendMessage(task, principal, userDAO, title, text, type, newRecipient, url, emailSender, facade);
		}
		
		// add requirements
		String[] reqs = skills.split(";");
		for (int i = 0; i < reqs.length; i++) {
			if (reqs[i].isEmpty()) continue;
			try {
				int level = Integer.parseInt(reqs[i].split("\\): ")[1]);
				String[] split = reqs[i].split("\\): ")[0].split("\\(");
				long skillID = Long.parseLong(split[split.length - 1]);
				Skill skill = facade.getSkillDAO().getOne(skillID);
				task.addRequirement(new TaskRequirement(skill, level));
			} catch (Exception e) {}
		}
		
		task.getRecipients();
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
			throw new SessionExpiredException(null, messageSource);
		}
		if (recievedTask.getName().length() < 5 || recievedTask.getName().length() > 100)
			throw new SessionExpiredException(null, messageSource);
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

	public static void deleteTask(Task task, ICPMSFacade facade, Principal principal, IUserDAO userDAO) {
		for (Message mes : task.getMessages()) {
			mes.setTask(null);
			facade.getMessageDAO().update(mes);
		}
		for (TaskCenter center : task.getRecipients())
			CommonModelAttributes.newTask.put(center.getUser().getId(), -1);
		//facade.getTaskDAO().delete(task);
		long delUser;
		Users user = userDAO.getByUsername(((UsernamePasswordAuthenticationToken) principal).getName());
		if (user == null)
			delUser = 0;
		else
			delUser = user.getId();
		Date delDate = new Date(System.currentTimeMillis());
		task.setDelDate(delDate);
		task.setDelUser(delUser);
		facade.getTaskDAO().update(task);
	}
	@RequestMapping(path = "/task/delete", method = RequestMethod.GET)
	public String taskDelete(Model model, Principal principal, @RequestParam(name = "id", required = true) Long id) {
		deleteTask(facade.getTaskDAO().getOne(id), facade, principal, userDAO);
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
						requirChange.getKey(), request.getPathInfo(), messageSource);
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
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		if (values.size() >= 3) {
			Task task = new Task();
			String[] requirements = values.get(0).toString().split("\\|");
			for (int i = 0; i < requirements.length; i++) {
				if (requirements[i].isEmpty())
					continue;
				String[] reqVals = requirements[i].split("\\(")[1].split("\\): ");
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
