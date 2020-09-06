package com.cpms.web.controllers;

import java.io.IOException;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
	
	private long getExpertId(Principal principal) {
		Users curUser = Security.getUser(principal, userDAO);
		if (curUser != null && curUser.getProfileId() > 0) {
			Profile pr = getExpert(curUser.getProfileId());
			if (pr != null)
				return pr.getId();
		}
		List<Users> all = userDAO.getAll();
		for (Users user : all)
			if (user.getProfileId() != null)
				if (user.getProfileId() > 0) {
					Profile pr = getExpert(user.getProfileId());
					if (pr != null)
						return pr.getId();
				}
		return 0;
	}
	
	private Long getSession() {
		/*VotingSession session = null;
		for (VotingSession vs : facade.getVotingSessionDAO().getAll()) {
			session = vs;
			for (VotingSessionUser vsu : vs.getUsers())
				if (vsu.getExpert().getId() == expertId) {
					session = vs;
					break;
				}
		}
		return session;*/
		List<Long> ids = facade.getVotingSessionDAO().getIDs();
		if (ids.isEmpty())
			return 0L;
		if (ids.get(0) == null)
			ids.set(0, 0L);
		return ids.get(0);
	}
	
	@RequestMapping(value = {"/", ""},
			method = RequestMethod.GET)
	public String skills(Model model, HttpServletRequest request, Principal principal) {
		model.addAttribute("_NAMED_TITLE", true);
		model.addAttribute("_VIEW_TITLE", "Innovation Voting");
		model.addAttribute("_FORCE_CSRF", true);

		long expertId = getExpertId(principal);
		// define session
		long sessionId = getSession();
		// define session parameters for the user
		List<Task> innList = new ArrayList<>();
		List<Integer> spends = new ArrayList<>();
		int budget = 0;
		if (sessionId == 0) {
			return "redirect:/voting/results";
		} else {
			VotingSession session = facade.getVotingSessionDAO().getOne(sessionId);
			boolean inSession = principal == null;
			if (!inSession)
				for (VotingSessionUser vsu : session.getUsers())
					if (vsu.getExpert().getId() == expertId) {
						inSession = true;
						break;
			}
			if (inSession) {
				budget = session.getBudget();
				for (SessionInnovation si : session.getInnovations()) {
					innList.add(si.getInnovation());
					for (com.cpms.data.entities.Voting vote : si.getVotes())
						if (vote.getExpert().getId() == expertId) {
							spends.add(vote.getSpending());
						}
				}
			}
		}
		while (spends.size() < innList.size())
			spends.add(0);
		model.addAttribute("innList", innList);
		model.addAttribute("spends", spends);
		model.addAttribute("budget", budget);
		
		return "voting";
	}
	@RequestMapping(path = {"/results"}, 
			method = RequestMethod.GET)
	public String results(Model model, Principal principal, @RequestParam(name = "sessionId", required = false) Long sessionId) {
		model.addAttribute("_NAMED_TITLE", true);
		model.addAttribute("_VIEW_TITLE", "Voting Results");
		model.addAttribute("_FORCE_CSRF", true);
		
		// get sessions list
		Map<Long, String> sesList = new HashMap<>();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		for (VotingSession session : facade.getVotingSessionDAO().getAll()) {
			String startDate = "null", endDate = "null";
			if (session.getStartDate() != null)
				startDate = df.format(session.getStartDate());
			if (session.getEndDate() != null)
				endDate = df.format(session.getEndDate());
			sesList.put(session.getId(), String.format("%s - %s", startDate , endDate));
		}
		model.addAttribute("sesList", sesList);

		List<VoteResults> voteResults = new ArrayList<>();
		// define session
		if (sessionId == null || sessionId == 0)
			sessionId = getSession();
	
		if (sessionId > 0) {
			// get results
			VotingSession session = facade.getVotingSessionDAO().getOne(sessionId);
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
		}
		model.addAttribute("voteResults", voteResults);
		model.addAttribute("sessionId", sessionId);
		return "voteResults";
	}
	
	@RequestMapping(path = {"/save"}, 
			method = RequestMethod.GET)
	public String skillDelete(Model model, Principal principal, @RequestParam(name = "spendings", required = false) String spendings) {
		long expertId = getExpertId(principal);
		Profile expert = getExpert(expertId);
		// define session
		long sessionId = getSession();
		if (sessionId == 0)
			return "redirect:/voting";
		VotingSession session = facade.getVotingSessionDAO().getOne(sessionId);

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
		return "redirect:/voting";
	}
	
	List<Profile> experts = new ArrayList<>();
	List<Task> innovations = new ArrayList<>();
	private List<Profile> getExperts() {
		List<Long> ids = facade.getProfileDAO().getIDs();
		boolean change = ids.size() != experts.size();
		if (!change)
			for (Profile expert : experts)
				if (!ids.contains(expert.getId())) {
					change = true;
					break;
				}
		if (change) {
			experts.clear();
			for (Long id : ids) {
					Profile expert = facade.getProfileDAO().getOne(id);
					if (expert != null)
						experts.add(expert);
			}
		}
		return experts;
	}
	private List<Task> getInnovations() {
		List<Long> ids = facade.getTaskDAO().getIDs();
		boolean change = ids.size() != innovations.size();
		if (!change)
			for (Task inn : innovations)
				if (!ids.contains(inn.getId())) {
					change = true;
					break;
				}
		if (change) {
			innovations.clear();
			for (Long id : ids) {
					Task inn = facade.getTaskDAO().getOne(id);
					if (inn != null)
						innovations.add(inn);
			}
		}
		return innovations;
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
			id = getSession();
		}
		if (id == 0) {
			session = new VotingSession();
			create = true;
		} else {
			session = facade.getVotingSessionDAO().getOne(id);
			create = false;
		}
		
		model.addAttribute("votingSession", session);
		model.addAttribute("create", create);
		List<Profile> experts = new ArrayList<>();
		for (Long taskId : facade.getProfileDAO().getIDs()) {
			Profile expert = new Profile();
			expert.setId(taskId);
			expert.setName(facade.getProfileDAO().getNameByID(taskId));
			experts.add(expert);
		}
		model.addAttribute("experts", experts);
		List<Task> innovations = new ArrayList<>();
		for (Long taskId : facade.getTaskDAO().getIDs()) {
			Task task = new Task();
			task.setId(taskId);
			task.setName(facade.getTaskDAO().getNameByID(taskId));
			innovations.add(task);
		}
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

	private Profile getExpert(long id) {
		for (Profile expert : experts)
			if (expert.getId() == id)
				return expert;
		return facade.getProfileDAO().getOne(id);
	}

	private Task getInnovation(long id) {
		for (Task inn : innovations)
			if (inn.getId() == id)
				return inn;
		return facade.getTaskDAO().getOne(id);
	}
	
	private void updateUsers(VotingSession session, String users) {
		Set<VotingSessionUser> oldVars = session.getUsers();
		session.clearUsers();
		if (users == null)
			return;
		String[] split = users.split(",");
		boolean getExp = false;
		for (int i = 0; i < split.length; i++) {
			long userid = 0;
			try {
				userid = Long.parseLong(split[i]);
			} catch (NumberFormatException e) {}
			if (userid <= 0) continue;
			boolean isOld = false;
			for (VotingSessionUser tt : oldVars)
				if (tt.getExpert().getId() == userid) {
					session.addUser(tt);
					isOld = true;
					break;
				}
			if (!isOld) {
				if (!getExp)
					getExperts();
				Profile user = getExpert(userid);
				if (user == null) continue;
				session.addUser(new VotingSessionUser(session, user));
			}
		}
	}

	private void updateInnovations(VotingSession session, String inns) {
		Set<SessionInnovation> oldVars = session.getInnovations();
		session.clearInnovations();
		if (inns == null)
			return;
		String[] split = inns.split(",");
		boolean getInn = false;
		for (int i = 0; i < split.length; i++) {
			long innid = 0;
			try {
				innid = Long.parseLong(split[i]);
			} catch (NumberFormatException e) {}
			if (innid <= 0) continue;
			boolean isOld = false;
			for (SessionInnovation tt : oldVars)
				if (tt.getInnovation().getId() == innid) {
					session.addInnovation(tt);
					isOld = true;
					break;
				}
			if (!isOld) {
				if (!getInn)
					getInnovations();
				Task innovation = getInnovation(innid);
				if (innovation == null) continue;
				session.addInnovation(new SessionInnovation(session, innovation));
			}
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
		if (innovations == null) innovations = "";
		
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
		return "redirect:/voting/session";
	}

}
