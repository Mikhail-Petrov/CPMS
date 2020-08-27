package com.cpms.web.controllers;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Category;
import com.cpms.data.entities.Language;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.ProjectTermvariant;
import com.cpms.data.entities.SessionInnovation;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TaskCenter;
import com.cpms.data.entities.TaskRequirement;
import com.cpms.data.entities.Task_Category;
import com.cpms.data.entities.Task_Trend;
import com.cpms.data.entities.Term;
import com.cpms.data.entities.TermVariant;
import com.cpms.data.entities.Trend;
import com.cpms.data.entities.VoteResults;
import com.cpms.data.entities.VotingSession;
import com.cpms.data.entities.VotingSessionUser;
import com.cpms.exceptions.SessionExpiredException;
import com.cpms.exceptions.WrongJsonException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.entities.Users;

/**
 * Viewer for profile and task entities.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/voting")
public class Voting {
	
	@Autowired
	@Qualifier(value = "facade")
	private ICPMSFacade facade;

	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;

    @Autowired
    private MessageSource messageSource;

    public static String ch0 = "";
    public static long parent0 = 0;

	
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
	
	private long getExpertId() {
		List<Users> all = userDAO.getAll();
		for (Users user : all)
			if (user.getProfileId() != null)
				if (user.getProfileId() > 0) {
					Profile pr = facade.getProfileDAO().getOne(user.getProfileId());
					if (pr != null)
						return pr.getId();
				}
		return 0;
	}
	
	private VotingSession getSession(long expertId) {
		VotingSession session = null;
		for (VotingSession vs : facade.getVotingSessionDAO().getAll()) {
			session = vs;
			for (VotingSessionUser vsu : vs.getUsers())
				if (vsu.getExpert().getId() == expertId) {
					session = vs;
					break;
				}
		}
		return session;
	}
	
	@RequestMapping(value = {"/", ""},
			method = RequestMethod.GET)
	public String skills(Model model, HttpServletRequest request, Principal principal) {
		model.addAttribute("_NAMED_TITLE", true);
		model.addAttribute("_VIEW_TITLE", "Innovation Voting");
		model.addAttribute("_FORCE_CSRF", true);
		
		long expertId = getExpertId();
		// define session
		VotingSession session = getSession(expertId);
		// define session parameters for the user
		List<Task> innList = new ArrayList<>();
		List<Integer> spends = new ArrayList<>();
		int budget = 100, available = 20;
		if (session == null) {
			List<Task> tasks = facade.getTaskDAO().getAll();
			for (Task task : tasks)
				if (task.getVariant() != null)
					innList.add(task);
		} else {
			session = facade.getVotingSessionDAO().getOne(session.getId());
			budget = session.getBudget();
			available = budget;
			for (SessionInnovation si : session.getInnovations()) {
				innList.add(si.getInnovation());
				for (com.cpms.data.entities.Voting vote : si.getVotes())
					if (vote.getExpert().getId() == expertId) {
						available -= vote.getSpending();
						spends.add(vote.getSpending());
					}
			}
		}
		while (spends.size() < innList.size())
			spends.add(0);
		model.addAttribute("innList", innList);
		model.addAttribute("spends", spends);
		model.addAttribute("budget", budget);
		model.addAttribute("available", available);
		
		return "voting";
	}
	@RequestMapping(path = {"/results"}, 
			method = RequestMethod.GET)
	public String results(Model model, Principal principal) {
		model.addAttribute("_NAMED_TITLE", true);
		model.addAttribute("_VIEW_TITLE", "Voting Results");
		model.addAttribute("_FORCE_CSRF", true);

		long expertId = getExpertId();
		// define session
		VotingSession session = getSession(expertId);
		if (session == null)
			return "redirect:/voting";
		session = facade.getVotingSessionDAO().getOne(session.getId());

		// get results
		List<VoteResults> voteResults = new ArrayList<>();
		int sum = 0;
		for (SessionInnovation si : session.getInnovations()) {
			VoteResults res = new VoteResults();
			res.setName(si.getInnovation().getName());
			int value = 0;
			for (com.cpms.data.entities.Voting vote : si.getVotes())
				value += vote.getSpending();
			res.setValue(value);
			sum += value;
			voteResults.add(res);
		}
		if (sum > 0)
			for (VoteResults res : voteResults)
				res.setPercent(res.getValue() * 100 / sum);
		Collections.sort(voteResults);
		model.addAttribute("voteResults", voteResults);
		return "voteResults";
	}
	
	@RequestMapping(path = {"/save"}, 
			method = RequestMethod.GET)
	public String skillDelete(Model model, Principal principal, @RequestParam(name = "spendings", required = false) String spendings) {
		long expertId = getExpertId();
		Profile expert = facade.getProfileDAO().getOne(expertId);
		// define session
		VotingSession session = getSession(expertId);
		if (session == null)
			return "redirect:/voting";
		session = facade.getVotingSessionDAO().getOne(session.getId());

		String[] spends = spendings.split(",");
		for (int i = 0; i < spends.length; i++) {
			String[] split = spends[i].split(":");
			if (split.length < 2) continue;
			long innId = 0;
			int spend = 0;
			try {
				innId = Long.parseLong(split[0]);
				spend = Integer.parseInt(split[1]);
			} catch (NumberFormatException e) {}
			if (innId <= 0) continue;
			if (spend < 0) spend = 0;

			for (SessionInnovation si : session.getInnovations())
				if (si.getInnovation().getId() == innId) {
					boolean exists = false;
					for (com.cpms.data.entities.Voting vote : si.getVotes())
						if (vote.getExpert().getId() == expertId) {
							vote.setSpending(spend);
							exists = true;
						}
					if (!exists)
						si.addVote(new com.cpms.data.entities.Voting(si, expert, spend));
				}
		}
		facade.getVotingSessionDAO().update(session);
		return "redirect:/voting/session";
	}
	
	private List<Profile> getExperts() {
		List<Users> users = userDAO.getAll();
		List<Profile> experts = new ArrayList<>();
		Collections.sort(users);
		for (Users user : users)
			if (user.getProfileId() != null) {
				Profile expert = facade.getProfileDAO().getOne(user.getProfileId());
				if (expert != null)
					experts.add(expert);
			}
		return experts;
	}
	@RequestMapping(path = "/session", method = RequestMethod.GET)
	public String task(Model model, Principal principal, @RequestParam(name = "id", required = false) Long id) {
		model.addAttribute("_VIEW_TITLE", "Session edit");
		model.addAttribute("_NAMED_TITLE", true);
		model.addAttribute("_FORCE_CSRF", true);
		VotingSession session;
		boolean create = false;
		// temp: get last session
		if (id == null) {
			List<VotingSession> all = facade.getVotingSessionDAO().getAll();
			if (!all.isEmpty())
				id = all.get(all.size() - 1).getId();
		}
		if (id == null) {
			session = new VotingSession();
			create = true;
		} else {
			session = facade.getVotingSessionDAO().getOne(id);
			create = false;
		}
		
		model.addAttribute("votingSession", session);
		model.addAttribute("create", create);
		model.addAttribute("experts", getExperts());
		List<Task> innovations = new ArrayList<>();
		for (Task task : facade.getTaskDAO().getAll())
			if (task.getVariant() != null)
				innovations.add(task);
		model.addAttribute("innovations", innovations);

		List<Long> performers = new ArrayList<>();
		for (VotingSessionUser vsu : session.getUsers())
			performers.add(vsu.getExpert().getId());
		model.addAttribute("performers", performers);
		List<Long> inns = new ArrayList<>();
		for (SessionInnovation si : session.getInnovations())
			inns.add(si.getInnovation().getId());
		model.addAttribute("inns", inns);
		
		return "editVotingSession";
	}

	private void updateUsers(VotingSession session, String users) {
		Set<VotingSessionUser> oldVars = session.getUsers();
		session.clearUsers();
		if (users == null)
			return;
		String[] split = users.split(",");
		for (int i = 0; i < split.length; i++) {
			long userid = 0;
			try {
				userid = Long.parseLong(split[i]);
			} catch (NumberFormatException e) {}
			if (userid <= 0) continue;
			boolean isOld = false;
			Profile user = facade.getProfileDAO().getOne(userid);
			if (user == null) continue;
			for (VotingSessionUser tt : oldVars)
				if (tt.getExpert().getId() == userid) {
					session.addUser(tt);
					isOld = true;
					break;
				}
			if (!isOld)
				session.addUser(new VotingSessionUser(session, user));
		}
	}

	private void updateInnovations(VotingSession session, String inns) {
		Set<SessionInnovation> oldVars = session.getInnovations();
		session.clearInnovations();
		if (inns == null)
			return;
		String[] split = inns.split(",");
		for (int i = 0; i < split.length; i++) {
			long innid = 0;
			try {
				innid = Long.parseLong(split[i]);
			} catch (NumberFormatException e) {}
			if (innid <= 0) continue;
			boolean isOld = false;
			Task innovation = facade.getTaskDAO().getOne(innid);
			if (innovation == null) continue;
			for (SessionInnovation tt : oldVars)
				if (tt.getInnovation().getId() == innid) {
					session.addInnovation(tt);
					isOld = true;
					break;
				}
			if (!isOld)
				session.addInnovation(new SessionInnovation(session, innovation));
		}
	}
	
	@RequestMapping(path = "/session", method = RequestMethod.POST)
	public String taskCreate(Model model, HttpServletRequest request
			, @ModelAttribute("votingSession") @Valid VotingSession recievedSession,
			BindingResult bindingResult, Principal principal, @RequestParam(required=false, name="performers") String users
			, @RequestParam(required=false, name="innovations") String innovations) {
		if (recievedSession == null) {
			throw new SessionExpiredException(null, messageSource);
		}
		boolean create = (recievedSession.getId() == 0);
		if (users == null) users = "";
		
		if (users.contains("all")) {
			List<Profile> experts = getExperts();
			users = "0";
			for (Profile expert : experts)
				users += "," + expert.getId();
		}
		if (innovations.contains("all")) {
			innovations = "0";
			for (Task task : facade.getTaskDAO().getAll())
				if (task.getVariant() != null)
					innovations += "," + task.getId();
		}
		VotingSession session;
		if (create) {
			updateUsers(recievedSession, users);
			updateInnovations(recievedSession, innovations);
			session = facade.getVotingSessionDAO().insert(recievedSession);
		} else {
			session = facade.getVotingSessionDAO().getOne(recievedSession.getId());
			session.update(recievedSession);
			updateUsers(session, users);
			updateInnovations(session, innovations);
			session = facade.getVotingSessionDAO().update(session);
		}
		return "redirect:/voting";
	}

}
