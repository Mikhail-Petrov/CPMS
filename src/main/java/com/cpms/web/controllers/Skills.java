package com.cpms.web.controllers;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.util.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tartarus.snowball.ext.PorterStemmer;

import com.cpms.dao.interfaces.IApplicationsService;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.dao.interfaces.IDraftableSkillDaoExtension;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Article;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Keyword;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TaskRequirement;
import com.cpms.exceptions.WrongJsonException;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.RoleTypes;
import com.cpms.security.entities.Users;
import com.cpms.web.PagingUtils;
import com.cpms.web.SkillUtils;
//import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpms.web.ajax.IAjaxAnswer;
import com.cpms.web.ajax.InnAnswer;
import com.cpms.web.ajax.SkillAnswer;

/**
 * Viewer for profile and task entities.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/skills")
public class Skills {
	
	@Autowired
	@Qualifier("facade")
	private ICPMSFacade facade;
	
	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;
	
	@Autowired
	@Qualifier("applicationsService")
	private IApplicationsService applicationsService;
	
	@Autowired
	@Qualifier("draftableSkillDAO")
	private IDraftableSkillDaoExtension skillDao;

    @Autowired
    private MessageSource messageSource;

    private List<String> skillTerms = new ArrayList<>(), texts = new ArrayList<>();
    private Map<Long, Map<Integer, Integer>> X = new HashMap<>();
    private List<Map<Integer, Integer>> Y = new ArrayList<>();
    private List<Skill> reqsToAdd = new ArrayList<>();
    
    private class SkillCos implements Comparable<SkillCos> {
    	private double cos;
    	private Skill skill;

    	public SkillCos(Skill skill, double cos) {
    		setSkill(skill);
    		setCos(cos);
    	}
		@Override
		public int compareTo(SkillCos o) {
			return Double.compare(o.getCos(), getCos());
		}

		public double getCos() {
			return cos;
		}

		public void setCos(double cos) {
			this.cos = cos;
		}

		public Skill getSkill() {
			return skill;
		}

		public void setSkill(Skill skill) {
			this.skill = skill;
		}
    	
    }
	@ResponseBody
	@RequestMapping(value = "/extractReq",
			method = RequestMethod.POST)
	public List<Object> extractReq(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		Long taskId = values.size() > 0 ? Long.parseLong(values.get(0).toString()) : 0;
		Integer curSt = values.size() > 1 ? (Integer) values.get(1) : 0;
		String taskName = values.size() > 2 ? (String) values.get(2) : "";
		switch(curSt) {
		case 0:
			// get skills
			List<Skill> skills = facade.getSkillDAO().getAll();
			skillTerms.clear();
			X.clear();
			PorterStemmer stemmer = new PorterStemmer();
			List<String> stopWords = Arrays.asList(new String[]{"a", "about", "above", "after", "again", "against", "ain", "all", "am", "an", "and", "any", "are", "aren", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can", "couldn", "couldn't", "d", "did", "didn", "didn't", "do", "does", "doesn", "doesn't", "doing", "don", "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn", "hadn't", "has", "hasn", "hasn't", "have", "haven", "haven't", "having", "he", "her", "here", "hers", "herself", "him", "himself", "his", "how", "i", "if", "in", "into", "is", "isn", "isn't", "it", "it's", "its", "itself", "just", "ll", "m", "ma", "me", "mightn", "mightn't", "more", "most", "mustn", "mustn't", "my", "myself", "needn", "needn't", "no", "nor", "not", "now", "o", "of", "off", "on", "once", "only", "or", "other", "our", "ours", "ourselves", "out", "over", "own", "re", "s", "same", "shan", "shan't", "she", "she's", "should", "should've", "shouldn", "shouldn't", "so", "some", "such", "t", "than", "that", "that'll", "the", "their", "theirs", "them", "themselves", "then", "there", "these", "they", "this", "those", "through", "to", "too", "under", "until", "up", "ve", "very", "was", "wasn", "wasn't", "we", "were", "weren", "weren't", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "won", "won't", "wouldn", "wouldn't", "y", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves", "could", "he'd", "he'll", "he's", "here's", "how's", "i'd", "i'll", "i'm", "i've", "let's", "ought", "she'd", "she'll", "that's", "there's", "they'd", "they'll", "they're", "they've", "we'd", "we'll", "we're", "we've", "what's", "when's", "where's", "who's", "why's", "would"});
			for (Skill skill : skills) {
				if (skill.getDelDate() != null) continue;
				if (skill.getParent() == null) continue;
			// calculate terms for skill
				String[] tokens = Statistic.prepareToTokenize(skill.getName()).split(" ");
				Map<Integer, Integer> counts = new HashMap<>();
				for (int i = 0; i < tokens.length; i++) {
					if (stopWords.contains(tokens[i].toLowerCase())) continue;
					String word = Statistic.stemTermSt(tokens[i], stemmer).trim();
					if (skillTerms.contains(word)) {
						int index = skillTerms.indexOf(word), value = 0;
						if (counts.containsKey(index))
							value = counts.get(index);
						counts.put(index, value + 1);
					} else {
						counts.put(skillTerms.size(), 1);
						skillTerms.add(word);
					}
				}
				X.put(skill.getId(), counts);
			}
			break;
		case 1:
			if (taskId > 0) {
				Task innovation = facade.getTaskDAO().getOne(taskId);
				if (innovation == null)
					return returnError("innovation not found");
				taskName = innovation.getName();
			}
			// google this innovation in monster.com
			texts.clear();
			for (int page = 0; page < 5 && texts.size() < 10; page++) {
				String res = googleMonster(taskName.replace(" ", "+"), 0);
				if (!res.isEmpty()) {
					return returnError(res);
				}
			}
			if (texts.isEmpty())
				return returnError("dice.com is not available");
				//return returnError("monster.com is not available");
			break;
		case 2:
			// calculate terms for the innovation
			Y.clear();
			stemmer = new PorterStemmer();
			for (String text : texts) {
			// calculate terms for text
				String[] tokens = Statistic.prepareToTokenize(text).split(" ");
				Map<Integer, Integer> counts = new HashMap<>();
				for (int i = 0; i < tokens.length; i++) {
					String word = Statistic.stemTermSt(tokens[i], stemmer).trim();
					if (skillTerms.contains(word)) {
						int index = skillTerms.indexOf(word), value = 0;
						if (counts.containsKey(index))
							value = counts.get(index);
						counts.put(index, value + 1);
					}
				}
				Y.add(counts);
			}
			texts.clear();
			skillTerms.clear();
			break;
		case 3:
			// calculate cos sum for each skill and get the best ones
			skills = facade.getSkillDAO().getAll();
			List<SkillCos> skillsCos = new ArrayList<>();
			for (Skill skill : skills) {
				if (!X.containsKey(skill.getId()))
					continue;
				double cos = 0;
				Map<Integer, Integer> x = X.get(skill.getId());
				for (Map<Integer, Integer> y : Y) {
					double ab = 0, a2 = 0, b2 = 0;
					for (Entry<Integer, Integer> aa : x.entrySet()) {
						int a = aa.getValue();
						a2 += a*a;
						if (y.containsKey(aa.getKey()))
							ab += a * y.get(aa.getKey());
					}
					if (ab == 0)
						continue;
					for (Entry<Integer, Integer> bb : y.entrySet())
						b2 += bb.getValue() * bb.getValue();
					if (a2 > 0 && b2 > 0)
						cos += ab / Math.sqrt(a2) / Math.sqrt(b2);
				}
				SkillCos skillCos = new SkillCos(skill, cos);
				skillsCos.add(skillCos);
			}
			X.clear();
			Y.clear();
			Collections.sort(skillsCos);
			reqsToAdd.clear();
			for (int i = 0; i < 5 && i < skillsCos.size(); i++)
				if (skillsCos.get(i).getCos() > 0)
					reqsToAdd.add(skillsCos.get(i).getSkill());
			break;
		case 4:
			// add requirements to the task
			if (taskId > 0) {
				// add to the existed task
				Task innovation = facade.getTaskDAO().getOne(taskId);
				if (innovation == null)
					return returnError("innovation not found");
				for (int i = 0; i < reqsToAdd.size(); i++) {
					Skill skill = reqsToAdd.get(i);
					if (skill == null) continue;
					int level = skill.getMaxLevel() - i;
					if (level < 1)
						level = 1;
					TaskRequirement req = new TaskRequirement(skill, level);
					innovation.addRequirement(req);
				}
				reqsToAdd.clear();
				facade.getTaskDAO().update(innovation);
			} else {
				// return for the new task
				List<Object> res = new ArrayList<>();
				res.add("");
				for (int i = 0; i < reqsToAdd.size(); i++) {
					Skill skill = reqsToAdd.get(i);
					if (skill == null) continue;
					int level = skill.getMaxLevel() - i;
					if (level < 1)
						level = 1;
					//res.add(String.format("%d:%d", skill.getId(), level));
					res.add(String.format("|%s (%d)\n%d", skill.getName(), skill.getId(), level));
				}
				return res;
			}
		}
		return new ArrayList<>();
	}
	
	private List<Object> returnError(String mes) {
		List<Object> error = new ArrayList<>();
		error.add(mes);
		return error;
	}
	
	private String googleMonster(String name, int page) {
		//String URL = "https://www.google.com/search?q=site:job-openings.monster.com+%22" + name + "%22&hl=en";
		//String URL = "https://www.google.com/search?q=site:dice.com/jobs/detail+%22" + name + "%22&hl=en";
		String URL = "https://www.indeed.com/jobs?q=" + name + "&l=";
		if (page > 0)
			URL += "&start=" + (page * 10);
		Document google = Statistic.getDoc(URL);
		if (google == null)
			return "indeed.com is not available";
			//return "Google is not available";
		//Elements links = google.select("div[class='r'] a");
		//Elements links = google.select("a[href^='/url']");
		Elements links = google.select("a[class='jobtitle turnstileLink ']");
		for (Element link : links) {
			Document doc = Statistic.getDoc(link.absUrl("href"));
			if (doc == null)
				continue;
			/*Elements text = doc.select("div[name='sanitizedHtml']");
			if (text != null && !text.isEmpty())
				texts.add(text.text());
			if (doc.text() != null && !doc.text().isEmpty())
				texts.add(doc.text());*/
			Elements text = doc.select("div[class='jobsearch-jobDescriptionText']");
			if (text != null && !text.isEmpty())
				texts.add(text.text());
		}
		return "";
	}
    
	@RequestMapping(value = "/suggested",
			method = RequestMethod.GET)
	public String getDraft(Model model, HttpServletRequest request, Principal principal) {
		model.addAttribute("_VIEW_TITLE", "Suggested skills");
		model.addAttribute("_NAMED_TITLE", true);
		model.addAttribute("_FORCE_CSRF", true);
		
		List<Skill> drafts = skillDao.getDraftsOfUser(0L);
		List<String[]> skillsList = new ArrayList<>();
		for (Skill draft : drafts) {
			Long owner = draft.getOwner();
			String userName = "admin";
			if (owner != null && owner > 0) {
				Users user = userDAO.getByUserID(owner);
				if (user != null)
					userName = user.getUsername();
			}
			String[] skill = {draft.getId() + "", draft.getName(), userName};
			skillsList.add(skill);
		}
		model.addAttribute("skillsList", skillsList);
		
		return "suggested";
	}
	
	@RequestMapping(value = "/addDraft",
			method = RequestMethod.GET)
	public String addDraft(Model model, HttpServletRequest request, Principal principal
			, @RequestParam(name = "name", required = true) String name) {
		Skill skill = new Skill(name, "");
		long userId = 0;
		Users user = Security.getUser(principal, userDAO);
		if (user != null)
			userId = user.getId();
		skill.setDelUser(userId);
		skill.setDelDate(new Date(System.currentTimeMillis()));
		skill.setOwner(userId);
		facade.getSkillDAO().insert(skill);
		
		String ret = "redirect:/skills";
		if (user != null) {
			Long profileId = user.getProfileId();
			if (profileId != null && profileId > 0) {
				Profile expert = facade.getProfileDAO().getOne(profileId);
				if (expert != null) {
					expert.addCompetency(new Competency(skill, 6));
					facade.getProfileDAO().update(expert);
					ret = "redirect:/viewer/profile?id=" + expert.getId();
				}
			}
		}
		
		return ret;
	}
	
	@RequestMapping(value = "/saveDraft",
			method = RequestMethod.GET)
	public String saveDraft(@RequestParam(name = "id", required = true) Long id,
			@RequestParam(name = "name", required = false) String name,
			@RequestParam(name = "parentId", required = false) Long parentId) {
		Skill skill = facade.getSkillDAO().getOne(id);
		if (skill != null) {
			skill.setDelDate(null);
			skill.setDelUser(null);
			skill.setOwner(null);
			if (name != null && !name.isEmpty())
				skill.setName(name);
			if (parentId != null && parentId > 0 && parentId != id) {
				Skill parent = facade.getSkillDAO().getOne(parentId);
				if (parent != null)
					skill.setParent(parent);
			}
			facade.getSkillDAO().update(skill);
		}

		return "redirect:/skills/suggested";
	}
	
	@RequestMapping(value = "/deleteDraft",
			method = RequestMethod.GET)
	public String deleteDraft(@RequestParam(name = "id", required = true) Long id) {
		Skill skill = facade.getSkillDAO().getOne(id);
		if (skill != null)
			facade.getSkillDAO().delete(skill);

		return "redirect:/skills/suggested";
	}

	@ResponseBody
	@RequestMapping(value = "/ajaxSearch",
			method = RequestMethod.POST)
	public List<Skill> ajaxSearch(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		String name = values.size() > 0 ? (String) values.get(0) : "";
		if (name.isEmpty())
			return new ArrayList<>();
		name = "%" + name.replace(" ", "%") + "%";
		
		 List<Skill> res = new ArrayList<>();
		 for (Skill skill : skillDao.findByName(name)) {
			 res.add(new Skill(skill));
		 }
		 return res;
	}
	
	@ResponseBody
	@RequestMapping(value = "/ajaxSkillChildren",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxSkillChildren(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		if (values.size() >= 1 && DashboardAjax.isInteger(values.get(0).toString(), 10)) {
			long id = Long.parseLong(values.get(0).toString());
			InnAnswer answer = new InnAnswer();
			answer.setId(id);
			Skill skill = null;
			if (id > 0)
				skill = facade.getSkillDAO().getOne(id);
			List<Skill> children = skillDao.getChildren(skill);
			String flag = "";
			while (skill != null) {
				skill = skill.getParent();
				flag += "--";
			}
			for (Skill child : children) {
				answer.getIds().add(child.getId());
				answer.getTerms().add(child.getName());
				answer.getFlags().add(flag);
				//Set<Skill> kids = child.getChildren(skillDao);
				//answer.getKids().add(kids == null ? 0 : kids.size());
				answer.getKids().add((long) skillDao.countChildren(child));
			}
			return answer;
		} else {
			return new InnAnswer();
		}
	}
	
	@RequestMapping(path = { "/extractSkills" }, method = RequestMethod.GET)
	public String extractSkills(Model model) {
		String url = "http://data.europa.eu/esco/skill/L";
		Document doc = Statistic.getDoc(url);
		List<Skill> skills = new ArrayList<>();
		Skill main = new Skill("language skills and knowledge", "");
		skills.add(main);
		System.out.print(new Date(System.currentTimeMillis()) + "\n");
		for (Element root : doc.select("a[class*=show-underline]")) {
			List<Skill> newSkills = extractSkill(root.attr("href"), main);
			if (newSkills != null)
				skills.addAll(newSkills);
		}
		System.out.print(new Date(System.currentTimeMillis()) + "\n");
		facade.getSkillDAO().insertAll(skills);
		System.out.print(new Date(System.currentTimeMillis()) + "\n");
		return "redirect:/skills";
	}
	
	private List<Skill> extractSkill(String url, Skill parent) {
		Document doc = Statistic.getDoc(url);
		if (doc == null) return null;
		// get name and description
		String name, about;
		name = doc.select("[class*=header-solid] h1").text();
		if (name.length() > 100) 
			name = name.substring(0, 100);
		if (name == null || name.isEmpty()) return null;
		about = doc.select("pre").text();
		if (about.length() > 4000)
			about = about.substring(0, 4000);
		// get content: alternative and links to children
		Elements alternatives = doc.select("h2:contains(Alternative label) + ul li");
		String alternative = "";
		for (Element alt : alternatives)
			alternative += (alternative.isEmpty() ? "" : "|") + alt.text();
		Elements urls = null, ul = doc.select("h2:contains(Narrower skills/competences) + ul");
		if (!ul.isEmpty())
			urls = ul.first().select("li a");
		// create skill
		Skill newSkill = new Skill(name, about);
		newSkill.setParent(parent);
		if (alternative.length() > 4000)
			alternative = alternative.substring(0, 4000);
		if (!alternative.isEmpty())
			newSkill.setAlternative(alternative);
		List<Skill> ret = new ArrayList<>();
		ret.add(newSkill);
		// get children
		if (urls != null)
		for (Element child : urls) {
			List<Skill> newSkills = extractSkill(child.attr("href"), newSkill);
			if (newSkills != null)
				ret.addAll(newSkills);
		}
		return ret;
	}
	
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
	
	private static List<Skill> allSkills;
	public static List<Skill> getAllSkills(IDAO<Skill> skillDAO) {
		if (allSkills == null || allSkills.size() != skillDAO.count()) {
			allSkills = skillDAO.getAll();
			//Collections.sort(allSkills);
		}
		return new ArrayList<>(allSkills);
	}
	private List<Skill> getAllSkills() {
		if (allSkills == null || allSkills.size() != facade.getSkillDAO().count()) {
			//allSkills = facade.getSkillDAO().getAll();
			allSkills = skillDao.getChildren(null);
			//Collections.sort(allSkills);
		}
		return allSkills;
	}

	private List<Skill> addSkillsListToModel(Principal principal, HttpServletRequest request) {
		List<Skill> skills;
		if (CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT)) {
			Users owner = userDAO.getByUsername((
					(UsernamePasswordAuthenticationToken)principal
					).getName());
			//skills = facade.getSkillDAO().getAll();
			skills = getAllSkills();
			//skills.addAll(skillDao.getDraftsOfUser(owner.getId()));
		//} else if (CommonModelAttributes.userHasRole(request, RoleTypes.MANAGER)) {
			//skills = skillDao.getAllIncludingDrafts();
		} else {
			skills = getAllSkills();
		}
		return skills;
	}
	
	@RequestMapping(value = {"/", ""},
			method = RequestMethod.GET)
	public String skills(Model model, HttpServletRequest request, Principal principal) {
		long countProfiles = facade.getProfileDAO().count(),
				countTasks = facade.getTaskDAO().count();
		model.addAttribute("profilePages", countProfiles / PagingUtils.PAGE_SIZE 
				+ (countProfiles % PagingUtils.PAGE_SIZE > 0 ? 1 : 0));
		model.addAttribute("taskPages", countTasks / PagingUtils.PAGE_SIZE 
				+ (countTasks % PagingUtils.PAGE_SIZE > 0 ? 1 : 0));
		model.addAttribute("_VIEW_TITLE", "title.viewer");
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("company", new Profile());
		model.addAttribute("task", new Task());
		model.addAttribute("html0", EditorSkill.ch0);
		model.addAttribute("parent0", EditorSkill.parent0);
		EditorSkill.ch0 = "";
		
		String[] defLevels = {"Basic", "Intermediate", "Advanced", "4", "5", "6"};
		model.addAttribute("defaultLevels", defLevels);
		
		if (CommonModelAttributes.userHasRole(request, RoleTypes.EXPERT)) {
			Users owner = userDAO.getByUsername((
					(UsernamePasswordAuthenticationToken)principal
					).getName());
			List<Skill> skills = getAllSkills();
			//skills.addAll(skillDao.getDraftsOfUser(owner.getId()));
			model.addAttribute("skills", SkillTree.produceTree(sortSkills(skills)));
			Skill newSkill = new Skill();
			newSkill.setMaxLevel(1);
			model.addAttribute("skill", newSkill);
		} else {
			if (CommonModelAttributes.userHasRole(request, RoleTypes.MANAGER))
				//model.addAttribute("skills", SkillTree.produceTree(sortSkills(skillDao.getAllIncludingDrafts())));
				model.addAttribute("skills", SkillTree.produceTree(getAllSkills()));
			else
				model.addAttribute("skills", SkillTree.produceTree(getAllSkills()));
			Skill newSkill = new Skill();
			newSkill.setMaxLevel(1);
			model.addAttribute("skill", newSkill);
		}
		List<Skill> allSkills = addSkillsListToModel(principal, request);
		model.addAttribute("skillsList", SkillUtils.sortAndAddIndents(
				Skills.sortSkills(allSkills), skillDao));
		model.addAttribute("skillsAndParents", getSkillsAndParents(allSkills));
		return "skills";
	}
	
	public static List<Skill> sortSkills(List<Skill> skills) {
		Collections.sort(skills);
		return skills;
	}
	
	public static Map<Long, ArrayList<Long>> getSkillsAndParents(List<Skill> allSkills) {
		Map<Long, ArrayList<Long>> skillsAndParents = new HashMap<>();
		for (Skill skill : allSkills) {
			ArrayList<Long> parents = new ArrayList<>();
			Skill curSkill = skill;
			do {
				parents.add(curSkill.getId());
				curSkill = curSkill.getParent();
				if (parents.size() > 20)
					break;
			} while (curSkill != null);
			skillsAndParents.put(skill.getId(), parents);
		}
		return skillsAndParents;
	}

}
