
package com.cpms.web.controllers;

import java.io.IOException;
import java.math.BigInteger;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.validation.Valid;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tartarus.snowball.ext.PorterStemmer;

import com.cpms.dao.interfaces.IDAO;
import com.cpms.dao.interfaces.IDraftableSkillDaoExtension;
import com.cpms.dao.interfaces.IInnovationTermDAO;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Article;
import com.cpms.data.entities.Category;
import com.cpms.data.entities.Keyword;
import com.cpms.data.entities.ProjectTermvariant;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.Task_Category;
import com.cpms.data.entities.Task_Trend;
import com.cpms.data.entities.Term;
import com.cpms.data.entities.TermAnswer;
import com.cpms.data.entities.TermVariant;
import com.cpms.data.entities.Trend;
import com.cpms.data.entities.Website;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.entities.Users;
import com.cpms.web.CompetencyMatching;
import com.cpms.web.TermRes;
import com.cpms.web.UserSessionData;
import com.cpms.web.ajax.GroupAnswer;
import com.cpms.web.ajax.IAjaxAnswer;
import com.cpms.web.ajax.InnAnswer;

/**
 * Handles user creating, login operations and user viewing.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/stat/")
public class Statistic {
	
	@Autowired
	@Qualifier(value = "docDAO")
	private IDAO<Article> docDAO;
	
	@Autowired
	@Qualifier(value = "termDAO")
	private IDAO<Term> termDAO;
	
	@Autowired
	@Qualifier(value = "categoryDAO")
	private IDAO<Category> categoryDAO;
	
	@Autowired
	@Qualifier(value = "innovationDAO")
	private IInnovationTermDAO innDAO;
	
	@Autowired
	@Qualifier(value = "keywordDAO")
	private IDAO<Keyword> keyDAO;

	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;

	@Autowired
	@Qualifier(value = "userSessionData")
	private UserSessionData sessionData;
	
	@Autowired
	@Qualifier(value = "facade")
	private ICPMSFacade facade;
	
	@Autowired
	@Qualifier("draftableSkillDAO")
	private IDraftableSkillDaoExtension skillDao;

    @Autowired
    private MessageSource messageSource;


	Map<String, String> sites;
	//Map<String, Keyword> keys;
	//List<String> words = new ArrayList<>();

	private HashMap<String, List<String>> wordsMap;
	List<String> urls = new ArrayList<>(), oldUrls = new ArrayList<>();

	List<Long> nokeysDocs = new ArrayList<>();
	
	public static double sensitivity = 0.01;
	public static int tlimit = 50, sdDelay = 3, osdDelay = 5, ldDays = 30;
	@ResponseBody
	@RequestMapping(value = "/ajaxSettings",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxSettings(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		int i = 0;
		String sens = values.size() > i ? (String) values.get(i++) : "";
		String termLimit = values.size() > i ? (String) values.get(i++) : "";
		String stDate = values.size() > i ? (String) values.get(i++) : "";
		String oldDate = values.size() > i ? (String) values.get(i++) : "";
		String lastdDays = values.size() > i ? (String) values.get(i++) : "";
		sensitivity = Double.parseDouble(sens);
		tlimit = Integer.parseInt(termLimit);
		sdDelay = Integer.parseInt(stDate);
		osdDelay = Integer.parseInt(oldDate);
		ldDays = Integer.parseInt(lastdDays);
		return new GroupAnswer();
	}

	@RequestMapping(path = "/settings", method = RequestMethod.GET)
	public String settings(Model model) {
		model.addAttribute("_VIEW_TITLE", "Settings");
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("_NAMED_TITLE", true);
		model.addAttribute("sensitivity", sensitivity);
		model.addAttribute("tlimit", tlimit);
		model.addAttribute("stdate", sdDelay);
		model.addAttribute("olddate", osdDelay); 
		model.addAttribute("ldDays", ldDays); 

		return "settings";
	}
	
	List<Long> allCats = new ArrayList<>(), allTrends = new ArrayList<>();
	private void getCatsTrends() {
		if (allCats.isEmpty()) {
			// fill allCats for the first time
			List<Category> allCat = facade.getCategoryDAO().getAll();
			for (Category cat : allCat)
				allCats.add(cat.getId());
		}
		if (allTrends.isEmpty()) {
			// fill allTrends for the first time
			List<Trend> allTr = facade.getTrendDAO().getAll();
			for (Trend tr : allTr)
				allTrends.add(tr.getId());
		}
	}
	@ResponseBody
	@RequestMapping(value = "/ajaxGetTerms",
			method = RequestMethod.POST)
	public List<String> ajaxGetTerms(
			@RequestBody String json
			, @RequestParam(value = "catId", required = false) Integer catId
			, @RequestParam(value = "trId", required = false) Integer trId) {
//		List<Object> values = DashboardAjax.parseJson(json, messageSource);
//		int catId = values.size() > 0 ? (int) values.get(0) : 0, trId = 0;
		List<Long> cats = new ArrayList<>(), trends = new ArrayList<>();
		getCatsTrends();
		if (catId == null) catId = 0;
		if (trId == null) trId = 0;
		if (catId > 0)
			//cats.add((long) catId);
			cats = getCatChildIDs(categoryDAO.getOne(catId));
		else
			cats = allCats;
		if (trId > 0)
			trends.add((long) trId);
		else
			trends = allTrends;
		// getting terms
		Date end_date = new Date(System.currentTimeMillis()),
			start_date = changeDate(end_date, 0, -sdDelay, 0),
			old_start_date = changeDate(end_date, -osdDelay, 0, 0);
		int new_docs = innDAO.getDocCount(start_date, end_date, cats, trends),
			old_docs = innDAO.getDocCount(old_start_date, start_date, cats, trends);
		
		List<TermAnswer> termAnswers = innDAO.getTermAnswers(start_date, end_date, old_start_date, cats, trends);
		termAnswers.forEach(x -> x.calcVal(old_docs, new_docs, sensitivity));
		Collections.sort(termAnswers);
		
		List<String> res = new ArrayList<>();
		for (TermAnswer term : termAnswers)
			if (res.size() < tlimit)
				res.add(term.getPreferabletext());
		return res;
	}

	public static String matchErr = "";
	@RequestMapping(path = "/createProfile", method = RequestMethod.GET)
	public String createProfile(Model model
			, @ModelAttribute("name") @Valid String name
			, @ModelAttribute("skills") @Valid String skills) {

		if (name.trim().isEmpty())
			matchErr += "Field 'Name' is empty! ";
		if (skills.trim().isEmpty())
			matchErr += "Field 'Skills' is empty!";
		if (!matchErr.isEmpty())
			return "redirect:/viewer";
		// create profile
		/*Profile profile;
		profile = new Profile();
		profile.setName(name);*/
		//profile = facade.getProfileDAO().insert(profile);
		
		// extract skills
		skills = skills.replace("\r\n", "\n");
		String[] split = skills.split("\n");
		//List<Skill> allSkills = Skills.getAllSkills(facade.getSkillDAO());
		PorterStemmer stemmer = new PorterStemmer();
		Map<String, List<CompetencyMatching>> comps = new LinkedHashMap<>();
		for (int i = 0; i < split.length; i++) {
			String skillData = split[i].toLowerCase().trim();
			List<CompetencyMatching> extracted = extractCompetency(skillData, stemmer);
			Collections.sort(extracted);
			comps.put(skillData, extracted);
			//for (Competency comp : extracted)
				//profile.addCompetencySmart(comp);
		}
		if (!extraction)
			wordsMap.clear();
		//facade.getProfileDAO().update(profile);
		//profile = facade.getProfileDAO().insert(profile);
		
		
		//return "redirect:/viewer/profile?id=" + profile.getId();
		
		// add atributes
		model.addAttribute("_VIEW_TITLE", name);
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("_NAMED_TITLE", true);

		model.addAttribute("name", name);
		model.addAttribute("comps", comps);
		
		return "createProfile";
		//return "redirect:innovations";
	}
	
	private List<CompetencyMatching> extractCompetency(String skillData, PorterStemmer stemmer) {
		skillData = prepareToTokenize(skillData);
		List<CompetencyMatching> ret = new ArrayList<>();
		if (skillData == null || skillData.isEmpty())
			return ret;
		List<String> names = new ArrayList<>();
		// exact match
		List<Skill> skills = skillDao.findByName(skillData);
		for (Skill skill : skills)
			if (!names.contains(skill.getName())) {
				ret.add(new CompetencyMatching(skill, 6, 1.0));
				names.add(skill.getName());
			}
		// check alternatives exact match
		skills = skillDao.findByAlternative("%|" + skillData + "|%");
		for (Skill skill : skills)
			if (!names.contains(skill.getName())) {
				ret.add(new CompetencyMatching(skill, 6, 1.0));
				names.add(skill.getName());
			}
		// prepare contains-query (stemmed words and %)
		String[] words = skillData.split(" ");
		String query = "%";
		for (int i = 0; i < words.length; i++)
			query += stemTerm(words[i], stemmer) + "%";
		// contains
		skills = skillDao.findByName(query);
		for (Skill skill : skills)
			if (!names.contains(skill.getName())) {
				double val = sorensen(skill.getName(), skillData);
				int level = (int) ( ( (double) skill.getMaxLevel()) * val);
				if (level <= 0) level = 1;
				if (level > skill.getMaxLevel()) level = skill.getMaxLevel();
				ret.add(new CompetencyMatching(skill, level, val));
				names.add(skill.getName());
			}
		// check alternatives contains
		skills = skillDao.findByAlternative(query);
		for (Skill skill : skills)
			if (!names.contains(skill.getName())) {
				// find most appropriate alternative name
				double best = 0;
				String[] alts = skill.getAlternative().split("|");
				for (int i = 0; i < alts.length; i++) {
					double cur = sorensen(alts[i], skillData);
					if (cur > best)
						best = cur;
				}
				int level = (int) ( ( (double) skill.getMaxLevel()) * best);
				if (level <= 0) level = 1;
				if (level > skill.getMaxLevel()) level = skill.getMaxLevel();
				ret.add(new CompetencyMatching(skill, level, best));
				names.add(skill.getName());
			}
				
		return ret;
	}

	public static double sorensen(String A, String B) {
		double Al = A.length() * (A.length() + 1.0) / 2.0, Bl = B.length() * (B.length() + 1.0) / 2.0, AB = 0;
		if (Al + Bl == 0) return 0;
		for (int i = 0; i < B.length(); i++)
			for (int j = i; j < B.length(); j++)
			if (A.contains(B.substring(i, j+1)))
				AB++;
		return 2.0 * AB / (Al + Bl);
	}
	
	@RequestMapping(path = "/innovations", method = RequestMethod.GET)
	public String innovations(Model model) {
		model.addAttribute("_VIEW_TITLE", "im.title.innovations");
		model.addAttribute("_FORCE_CSRF", true);
		
		List<Category> cats = categoryDAO.getAll(), categs = new ArrayList<>();
		List<String> categories = new ArrayList<>(), catIDs = new ArrayList<>();
		Collections.sort(cats);
		for (Category cat : cats) {
			categories.add(cat.getPresentationName());
			catIDs.add(cat.getId() + "");
			if (cat.getParent() == null)
				categs.add(cat);
		}
		//String[] categories = {"Strategy and Planning", "--Culture Development", "Recruitment", "--Employer Branding and Communication", "--Recruitment", "--Onboarding", "Talent & Performance Management", "--Performance Management", "--Talent Management", "--Succession Management", "Learning & Training ", "--Competence Development", "--Learning Standards", "--Learning Management System ", "Total Rewards", "--Compensation", "--Benefits", "--Your Time", "Administration & Services", "--HR IT Systems", "--Employee Lifecycle Management", "--Expat Administration"};
		model.addAttribute("categories", categories);
		model.addAttribute("categs", categs);
		// get categories and kids
		Map<Long, List<Category>> cak = new HashMap<>();
		for (Category cat : cats) {
			Set<Category> children = cat.getChildren(categoryDAO);
			if (children.size() > 0 || cat.getParent() == null) {
				List<Category> kids = new ArrayList<>();
				for (Category child : children)
					kids.add(child);
				cak.put(cat.getId(), kids);
			}
		}
		model.addAttribute("catKids", cak);
		model.addAttribute("catIDs", catIDs);
		
		return "innovations";
	}

	@RequestMapping(path = "/documents", method = RequestMethod.GET)
	public String documents(Model model, @RequestParam(value = "catid", required = false) Long catid) {
		model.addAttribute("_VIEW_TITLE", "Last documents");
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("_NAMED_TITLE", true);
		
		List<Article> docsList = new ArrayList<>();
		getCatsTrends();
		List<Long> cats = allCats, trends = allTrends;
		if (catid != null) {
			cats = getCatChildIDs(categoryDAO.getOne(catid));
		}
		for (BigInteger id : innDAO.getLastDocs(changeDate(new Date(System.currentTimeMillis()), 0, 0, -ldDays), cats, trends))
			docsList.add(docDAO.getOne(id.longValue()));
		model.addAttribute("docsList", docsList);
		List<Category> categories = categoryDAO.getAll(), categs = new ArrayList<>();
		Collections.sort(categories);
		for (Category cat : categories)
			if (cat.getParent() == null)
				categs.add(cat);
		model.addAttribute("categs", categs);
		model.addAttribute("catid", catid);
		return "documents";
	}
	
	private List<Long> getCatChildIDs(Category root) {
		List<Long> cats = new ArrayList<>();
		cats.add(root.getId());
		Set<Category> children = root.getChildren(categoryDAO);
		for (Category cat : children) {
			cats.add(cat.getId());
			cats.addAll(getCatChildIDs(cat));
		}
		return cats;
	}

	@RequestMapping(path = "/doc", method = RequestMethod.GET)
	public String doc(Model model
			, @RequestParam(value = "docid", required = true) Long docid
			, @RequestParam(value = "termid", required = true) Long termid) {
		Term term = termDAO.getOne(termid);
		Article doc = docDAO.getOne(docid);
		if (term == null || doc == null)
			return terms(model);
		
		model.addAttribute("_VIEW_TITLE", doc.getTitle());
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("_NAMED_TITLE", true);

		String mask = doc.getMask();
		Document document = getDoc(doc.getUrl());
		String text = mask == null || mask.isEmpty() ? document.body().html() : document.select(mask).html();
		String[] prefixes = {" ", "(", "\"", "[", ">", "-", ",", ".", "\n"},
				postfixes = {" ", ",", ".", ";", "!", "?", "-", ":", "\"", "\n", ")", "]", "<"};
		for (TermVariant var : term.getVariants())
			for (String prefix : prefixes)
				for (String postfix : postfixes)
					text = text.replaceAll("(?i)\\" + prefix + var.getPresentationName() + "\\" + postfix,
							prefix + "!@#" + var.getPresentationName() + "#@!" + postfix);
		text = text.replace("!@#", "<span style=\"background-color: rgb(220,220,0);\">");
		text = text.replace("#@!", "</span>");
		model.addAttribute("text", text);
		return "viewDoc";
	}
	
	@ResponseBody
	@RequestMapping(value = "/ajaxGetInn",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxGetInn(
			@RequestBody String json) {
		// getting innovations
		List<Long> cats = new ArrayList<>(), trends = new ArrayList<>();
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		String catIDs = values.size() > 0 ? (String) values.get(0) : "0";
		long catID = 0;
		try {
			catID = Long.parseLong(catIDs);
		} catch (NumberFormatException e) {}
		if (catID <= 0)
			for (Category cat : facade.getCategoryDAO().getAll())
				cats.add(cat.getId());
		else
			cats = getCatChildIDs(facade.getCategoryDAO().getOne(catID));
		
		trends.add(0L);
		List<Term> res = innDAO.getInnovations(cats, trends);
		InnAnswer ans = new InnAnswer();
		Map<Long, Task> tasks = new HashMap<>();
		List<Task> all = facade.getTaskDAO().getAll();
		for (Task task : all)
			if (task.getVariant() != null)
				tasks.put(task.getVariant().getTerm().getId(), facade.getTaskDAO().getOne(task.getId()));
		for (Term term : res)
			ans.addTerm(term, tasks);
		return ans;
	}
	
	private Task createInnovation(TermVariant variant, Principal principal) {
		// check if a task for this variant exists
		List<Task> all = facade.getTaskDAO().getAll();
		for (Task task : all)
			if (task.getVariant() != null && task.getVariant().getId() == variant.getId())
				return task;
		// if not then create a task
		Task task = new Task();
		task.setName(variant.getText());
		task.setVariant(variant);
		task.addVariant(new ProjectTermvariant(task, variant));
		task.setStatus("1");
		task.setCost(0);
		task.setProjectType(0);
		task.setImpact(0);
		Date dueDate = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000);
		task.setCreatedDate(new Date(System.currentTimeMillis()));
		task.setDueDate(dueDate);
		Users owner = Security.getUser(principal, userDAO);
		if (owner == null)
			owner = userDAO.getAll().get(0);
		task.setUser(owner);
		// get categories and trends for the task
		List<Object[]> catTrends = innDAO.getCatTrendForTerm(variant.getTerm());
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
		}
		return facade.getTaskDAO().insert(task);
	}

	@ResponseBody
	@RequestMapping(value = "/ajaxAdd",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxAdd(
			@RequestBody String json, Principal principal) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		String query = values.size() > 0 ? (String) values.get(0) : "";
		if (query.isEmpty())
			return new InnAnswer();
		boolean isInn = values.size() > 1 ? (boolean) values.get(1) : false;
		// stem each word in query
		String stemtext = "";
		String[] words = prepareToTokenize(query).split(" ");
		PorterStemmer stemmer = new PorterStemmer();
		for (int i = 0; i < words.length; i++) {
			String stem = stemTerm(words[i], stemmer);
			stemtext += stem + " ";
		}
		stemtext = stemtext.trim();
		// check if it in DB
		Term term = innDAO.getTermByStem(stemtext);
		TermVariant variant;
		if (term == null) {
			// if not then add
			term = new Term();
			variant = term.addVariant(query);
			term.setStem(stemtext);
			term.setInn(isInn);
			term = termDAO.insert(term);
		} else {
			// if exists then add as variant
			variant = term.addVariant(query);
			if (variant.getTerm() == null)
				variant.setTerm(term);
			term.setInn(isInn);
			term = termDAO.update(term);
		}
		InnAnswer answer = new InnAnswer();
		// create innovation project
		if (isInn) {
			Task newTask = createInnovation(variant, principal);
			answer.setId(newTask.getId());
		}
		answer.getTerms().add(variant.getText());
		return answer;
	}

	@ResponseBody
	@RequestMapping(value = "/ajaxSearch",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxSearch(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		String query = values.size() > 0 ? (String) values.get(0) : "";
		if (query.isEmpty())
			return new InnAnswer();
		List<TermVariant> vars = termSearch(query, innDAO);
		List<Term> res = new ArrayList<>();
		for (TermVariant var : vars) {
			Term newTerm = var.getTerm().localize(null);
			newTerm.setPref(var.getText());
			res.add(newTerm);
		}
		InnAnswer ans = new InnAnswer();
		Map<Long, Task> tasks = new HashMap<>();
		List<Task> all = facade.getTaskDAO().getAll();
		for (Task task : all)
			if (task.getVariant() != null)
				tasks.put(task.getVariant().getTerm().getId(), facade.getTaskDAO().getOne(task.getId()));
		for (Term term : res)
			ans.addTerm(term, tasks);
		return ans;
	}
	
	public static List<TermVariant> termSearch(String query, IInnovationTermDAO innDAO) {
		String[] split = query.split(" ");
		// search
		Statistic.time();
		String bQuery = buildQuery(split, split.length, 0);
		List<Term> res = innDAO.find(bQuery);
		Statistic.time("term search for " + bQuery);
		if (res == null) res = new ArrayList<>();
		// for complex queries: divide and find
		for (int length = split.length - 1; length > 0 && res.isEmpty(); length--) {
			res = new ArrayList<>();
			for (int start = 0; start + length <= split.length; start++) {
				bQuery = buildQuery(split, length, start);
				Statistic.time();
				List<Term> curRes = innDAO.find(bQuery);
				Statistic.time("term search for " + bQuery);
				if (curRes != null)
					res.addAll(curRes);
			}
		}
		
		List<TermRes> results = new ArrayList<>();
		// calculate sorenson for variants
		for (Term term : res)
			for (TermVariant var : term.getVariants())
				results.add(new TermRes(var, sorensen(query, var.getText())));
		Collections.sort(results);
		// form results
		List<TermVariant> ret = new ArrayList<>();
		for (TermRes tr : results)
			if (ret.size() < tlimit)
				ret.add(tr.getTerm());
		return ret;
	}
	private static String buildQuery(String[] split, int length, int start) {
		String query = "%";
		// stem the query
		PorterStemmer stemmer = new PorterStemmer();
		for (int i = start; i < start + length; i++)
			query += stemTermSt(split[i], stemmer) + "%";
		return query;
	}

	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value = "/ajaxSave",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxSave(
			@RequestBody String json, Principal principal) {
		// get parameters
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		List<Integer> ids = values.size() > 1 ? (List<Integer>) values.get(1) : null;
		List<String> flags = values.size() > 2 ? (List<String>) values.get(2) : null;
		List<String> terms = values.size() > 3 ? (List<String>) values.get(3) : null;
		if (ids == null || flags == null || terms == null) return null;
		// save changes
		InnAnswer ans = new InnAnswer();
		List<Long> changed = new ArrayList<>();
		for (int i = 0; i < ids.size() && i < flags.size() && i < terms.size(); i++) {
			if (changed.contains((long) ids.get(i))) continue;
			Term term = termDAO.getOne((long) ids.get(i));
			boolean change = false;
			if (term.isInn() != !flags.get(i).isEmpty()) {
				change = true;
				term.setInn(!flags.get(i).isEmpty());
			}
			if (!term.getCategory().equals(flags.get(i))) {
				change = true;
				term.setCategory(flags.get(i));
			}
			if (change && !term.getPref().equals(terms.get(i))) {
				change = true;
				term.setPref(terms.get(i));
			}
			if (change) {
				term = termDAO.update(term);
				changed.add(term.getId());
				// create a project for innovation
				if (term.isInn()) {
					TermVariant variant = null;
					for (TermVariant var : term.getVariants())
						if (var.getText().equals(term.getPref()))
							variant = var;
					if (variant != null) {
						ans.getIds().add(createInnovation(variant, principal).getId());
						ans.getTerms().add(variant.getText());
					}
				}
			}
		}
		return ans;
	}
	
	@SuppressWarnings("deprecation")
	@RequestMapping(path = "/docs", method = RequestMethod.GET)
	public String docs(Model model) {
		model.addAttribute("_VIEW_TITLE", "im.title.docs");
		model.addAttribute("_FORCE_CSRF", true);

		List<Article> all = docDAO.getAll();
		long maxID = 0;
		Date parsed = null;
		int maxcount = -1, mincount = 0;
		Map<Integer, Integer> yearMap = new HashMap<>();
		int curYear = new Date(System.currentTimeMillis()).getYear();
		for (int i = 0; i < 6; i++)
			yearMap.put(curYear - i, 0);
		yearMap.put(0, 0);
		nokeysDocs.clear();
		for (Article obj : all) {
			if (obj.getId() > maxID)
				maxID = obj.getId();
			if (parsed == null || obj.getParseDate() != null && obj.getParseDate().after(parsed))
				parsed = obj.getParseDate();
			if (maxcount < 0 || obj.getWordcount() > maxcount)
				maxcount = obj.getWordcount();
			if (mincount < 0 || obj.getWordcount() < mincount)
				mincount = obj.getWordcount();
			if (obj.getWordcount() == 0)
				nokeysDocs.add(obj.getId());
			// detect year
			if (obj.getCreationDate() == null) continue;
			int y = obj.getCreationDate().getYear();
			if (!yearMap.containsKey(y))
				y = 0;
			yearMap.put(y, yearMap.get(y) + 1);
		}
		
		model.addAttribute("amount", all.size());
		model.addAttribute("maxid", maxID);
		model.addAttribute("parsed", parsed);
		model.addAttribute("maxcount", maxcount);
		model.addAttribute("mincount", mincount);
		
		if (nokeysDocs.size() > 10)
			model.addAttribute("nokeys", nokeysDocs.subList(0, 10));
		else
			model.addAttribute("nokeys", nokeysDocs);
		model.addAttribute("nokeyssize", nokeysDocs.size());
		
		List<String[]> years = new ArrayList<>();
		for (Entry<Integer, Integer> e : yearMap.entrySet())
			years.add(new String[]{e.getKey() > 0 ? "" + (1900 + e.getKey()) : "other", "" + e.getValue()});
		model.addAttribute("years", years);
		return "docs";
	}
	
	@SuppressWarnings("deprecation")
	private Date getPlusMonth(Date startDate) {
		Date curDate = (Date) startDate.clone();
		int month = curDate.getMonth();
		if (month == 11) {
			curDate.setYear(curDate.getYear() + 1);
			month = 0;
		} else
			month++;
		curDate.setMonth(month);
		return curDate;
	}

	@SuppressWarnings("deprecation")
	private Date changeDate(Date date, int years, int months, int days) {
		Date ret = (Date) date.clone();
		int d = date.getDate() + days;
		ret.setDate(d);
		ret.setMonth(ret.getMonth() + months);
		ret.setYear(ret.getYear() + years);
		return ret;
	}

	@RequestMapping(path = "/removeInn", method = RequestMethod.GET)
	public String removeInn(Model model, Principal principal, @RequestParam(value = "id", required = true) Long id) {
		Term term = termDAO.getOne(id);
		term.setInn(false);
		term.setCategory("");
		// find a task with this term
		List<Task> tasks = facade.getTaskDAO().getAll();
		for (Task task : tasks) {
			if (task.getVariant() != null)
				for (TermVariant var : term.getVariants())
					if (task.getVariant().getTerm().getId() == var.getTerm().getId()) {
						EditorTask.deleteTask(task, facade, principal, userDAO);
						break;
					}
		}
		termDAO.update(term);
		return "redirect:/stat/innovations";
	}

	public static long pt = -1;
	public static void time() {time("");}
	public static void time(String mes) {
		long ct = System.currentTimeMillis();
		if (mes.isEmpty())
			pt = ct;
		else {
			System.out.print(String.format("\n%s: %d\n", mes, ct-pt));
			pt = ct;
		}
	}
	@SuppressWarnings("deprecation")
	@RequestMapping(path = "/term", method = RequestMethod.GET)
	public String term(Model model, @RequestParam(value = "id", required = true) Long id
			, @RequestParam(value = "order", required = false) Integer order) {
		time();
		if (order == null) order = 0;
		Term term = termDAO.getOne(id);
		if (term == null)
			return terms(model);
		model.addAttribute("_VIEW_TITLE", term.getPref());
		model.addAttribute("title", term.getPref());
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("_NAMED_TITLE", true);
		time("block 1");

		Map<String, List<Float>> sums = new LinkedHashMap<>();
		Date today = new Date(System.currentTimeMillis());
		// get statistics for last 30 days, 3 months, year
		getCatsTrends();
		List<Long> cats = allCats, trends = allTrends;
		int tdc30 = innDAO.getTermDocCount(term, changeDate(today, 0, 0, -30), today),
				tdc3 = innDAO.getTermDocCount(term, changeDate(today, 0, -3, 0), today),
				tdc1 = innDAO.getTermDocCount(term, changeDate(today, -1, 0, 0), today),
				dc30 = innDAO.getDocCount(changeDate(today, 0, 0, -30), today, cats, trends),
				dc3 = innDAO.getDocCount(changeDate(today, 0, -3, 0), today, cats, trends),
				dc1 = innDAO.getDocCount(changeDate(today, -1, 0, 0), today, cats, trends);
		int[][] stat = {
				{
					innDAO.getTermSum(term, changeDate(today, 0, 0, -30), today),
					tdc30,
					dc30 == 0 ? 0 : (int)((float) tdc30 / (float) dc30 * 100.0)
				}, {
					innDAO.getTermSum(term, changeDate(today, 0, -3, 0), today),
					tdc3,
					dc3 == 0 ? 0 : (int)((float) tdc3 / (float) dc3 * 100.0)
				}, {
					innDAO.getTermSum(term, changeDate(today, -1, 0, 0), today),
					tdc1,
					dc1 == 0 ? 0 : (int)((float) tdc1 / (float) dc1 * 100.0)
				}
		};
		model.addAttribute("stat", stat);
		time("stat");
		// get statistics for graphs
		today.setDate(1);
		int years = 5, todayYear = today.getYear(), todayMonth = today.getMonth();
		SimpleDateFormat df = new SimpleDateFormat("yyyy'\n'MMM", Locale.ENGLISH);
		for (int curYear = todayYear - years; curYear <= todayYear; curYear++) {
			today.setYear(curYear);
			for (int curMonth = 0; curMonth < 12; curMonth++) {
				if (curYear == todayYear && curMonth > todayMonth)
					break;
				today.setMonth(curMonth);
				List<Float> sum = new ArrayList<>();
				sum.add((float) innDAO.getTermSum(term, today, getPlusMonth(today)));
				float termDocCount = (float) innDAO.getTermDocCount(term, today, getPlusMonth(today));
				sum.add(termDocCount);
				float docCount = (float) innDAO.getDocCount(today, getPlusMonth(today), cats, trends);
				sum.add(docCount == 0 ? 0 : (termDocCount / docCount));
				sums.put(df.format(today), sum);
			}
		}
		model.addAttribute("sums", sums);
		time("sums");
		
		// get keywords list
		List<Keyword> keys = new ArrayList<>();
		for (BigInteger docID : innDAO.getTermDocsIDs(term, order))
			//keys.add(keyDAO.getOne(docID.longValue()));
			keys.add(getKeyword(term.getId(), docID.longValue()));
		model.addAttribute("keys", keys);
		time("keys");
		model.addAttribute("order", order);
		
		model.addAttribute("termid", id);
		return "viewTerm";
	}
	
	private Keyword getKeyword(long termid, long docid) {
		Keyword res = new Keyword();
		res.setCount(innDAO.getTermCount(termid, docid));
		res.setDoc(docDAO.getOne(docid));
		return res;
	}

	private String err = "";
	@RequestMapping(path = "/terms", method = RequestMethod.GET)
	public String terms(Model model) {
		model.addAttribute("_VIEW_TITLE", "im.title.terms");
		model.addAttribute("_FORCE_CSRF", true);
		return "terms";
	}

	@RequestMapping(path = "/keys", method = RequestMethod.GET)
	public String keys(Model model) {
		model.addAttribute("_VIEW_TITLE", "im.title.keywords");
		model.addAttribute("_FORCE_CSRF", true);

		model.addAttribute("amount", keyDAO.count());
		model.addAttribute("maxid", 0);
		model.addAttribute("maxcount", 60);
		model.addAttribute("badterm", 50);
		model.addAttribute("baddoc", 5);
		
		List<Long> repeats = new ArrayList<>();
		repeats.add(156L);
		repeats.add(146L);
		model.addAttribute("repeats", repeats);
		model.addAttribute("repeatssize", 110);
		return "keys";
	}

	@RequestMapping(path = { "/analizeDoc" }, method = RequestMethod.GET)
	public String analize(Model model, @RequestParam(name = "id", required = false) Long id) {
		if (id != null) {
			Article doc = docDAO.getOne(id);
			if (doc != null)
				extractTerms(doc);
		} else {
			for (Article obj : docDAO.getAll())
				if (obj.getWordcount() == 0)
					extractTerms(obj);
		}
		return "redirect:/stat/docs";
	}
	@ResponseBody
	@RequestMapping(value = "/analizeAllDoc",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxAnalizeDocs(
			@RequestBody String json) {
		nokeysDocs.clear();
		for (Article obj : docDAO.getAll())
			if (obj.getWordcount() == 0)
				nokeysDocs.add(obj.getId());
		//for (Long id : nokeysDocs)
			//extractTerms(docDAO.getOne(id));
		if (nokeysDocs.isEmpty())
			return new GroupAnswer(true);
		extractTerms(docDAO.getOne(nokeysDocs.get(0)));
		return new GroupAnswer();
	}
	@ResponseBody
	@RequestMapping(value = "/insertDC",
			method = RequestMethod.POST)
	public IAjaxAnswer getAllTerms(@RequestBody String json
			, @RequestParam(value = "cat", required = false) boolean cat) {
		innDAO.insertDC(cat);
		return new GroupAnswer(true);
		/*if (all.isEmpty())
			return new GroupAnswer(true);
		return new GroupAnswer();*/
	}

	@RequestMapping(path = { "/clearTermsRep" }, method = RequestMethod.GET)
	public String clearTermsRep(Model model) {
		//allTerms.clear();
		return terms(model);
	}
	
	private String checkError = "";
	@ResponseBody
	@RequestMapping(value = "/checkWebsite",
			method = RequestMethod.POST)
	public Article checkWebsite(@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		int i = 0;
		String url = values.size() > i ? ((String) values.get(i++)).trim() : "";
		String linkm = values.size() > i ? ((String) values.get(i++)).trim() : "";
		String articlem = values.size() > i ? ((String) values.get(i++)).trim() : "";
		String datem = values.size() > i ? ((String) values.get(i++)).trim() : "";
		String datef = values.size() > i ? ((String) values.get(i++)).trim() : "";
		String pages = values.size() > i ? ((String) values.get(i++)).trim() : "";
		String datea = values.size() > i ? ((String) values.get(i++)).trim() : "";

		sites = new HashMap<>();
		urls = new ArrayList<>();
		Article res = null;
		try {
			getPosts(url, linkm, (pages.isEmpty() ? "" : "2 ") + pages);
			//if (sites.isEmpty())
				//getPosts(url, linkm, "");
			if (sites.isEmpty()) throw new Exception();
				res = getArticle(urls.get(0), articlem, datef, datem, datea);
		} catch (Exception e) {
		}
		if (res == null) {
			res = new Article();
			res.setTitle(checkError);
			res.setUrl("");
		}
		if (res.getText() != null && res.getText().length() > 100)
			res.setText(res.getText().substring(0, 100) + "");
		return res;
	}


	private int extracted, curSite = 0;
	private List<Website> curWebsites = null;
	private Date compareDate;
	@ResponseBody
	@RequestMapping(value = "/loadDocs",
			method = RequestMethod.POST)
	public IAjaxAnswer loadDocs(@RequestBody String json) {
		if (curWebsites == null) {
			// initialization
			curWebsites = facade.getWebsiteDAO().getAll();
			curSite = 1;
			sites = new HashMap<>();
			urls = new ArrayList<>();
			compareDate = new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000);
			oldUrls = new ArrayList<>();
			for (Article doc : docDAO.getAll())
				oldUrls.add(doc.getUrl());
		}
		if (curWebsites.isEmpty()) {
			curWebsites = null;
			GroupAnswer ans = new GroupAnswer();
			ans.setSuccess(true);
			return ans;
		}
		Website curWeb = curWebsites.get(0);
		if (!sites.isEmpty()) {
			// article extraction
			String curUrl = urls.get(0);
			/*for (Entry<String, String> e : sites.entrySet()) {
				curUrl = e.getKey();
				break;
			}*/
			Article article = getArticle(
					curUrl, curWeb.getArticleMask(), curWeb.getDateFormat(), curWeb.getDateMask(), curWeb.getDateAttribute());
			if (article.getCreationDate().before(compareDate)) {
				// too old articles
				sites.clear();
				urls.clear();
				curWebsites.remove(0);
				curSite = 1;
				return new GroupAnswer("");
			}
			// save and continue
			article.setWebsite(curWeb);
			facade.getDocumentDAO().insert(article);
			sites.remove(curUrl);
			urls.remove(0);
			return new GroupAnswer("Article extraction from " + curWeb.getName());
		}
		// analyze current page for current website
		getPosts(curWeb.getUrl(), curWeb.getLinkMask(), curSite++ + " " + curWeb.getPageFormat());
		if (sites.isEmpty()) {
			// no articles
			urls.clear();
			curWebsites.remove(0);
			curSite = 1;
			return new GroupAnswer("");
		}
		return new GroupAnswer("Article extraction from " + curWeb.getName());
	}

	@RequestMapping(path = { "/extract" })
	public String extract(Model model) {
		model.addAttribute("_VIEW_TITLE", "im.title.extract");
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("linkm", "h2 a");
		model.addAttribute("articlem", "div[class=entry-content]");
		model.addAttribute("datem", "time");
		model.addAttribute("datea", "datetime");
		model.addAttribute("datef", "yyyy-MM-dd'T'hh:mm:ss");
		return "extract";
	}
	
	@ResponseBody
	@RequestMapping(value = "/ajaxExtract",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxGroup(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		String sources = values.size() > 0 ? (String) values.get(0) : "";
		String linkm = values.size() > 1 ? (String) values.get(1) : "";
		String articlem = values.size() > 2 ? (String) values.get(2) : "";
		String datem = values.size() > 3 ? (String) values.get(3) : "";
		String datef = values.size() > 4 ? (String) values.get(4) : "";
		String pages = values.size() > 5 ? (String) values.get(5) : "";
		String mindate = values.size() > 6 ? (String) values.get(6) : "";
		String maxdate = values.size() > 7 ? (String) values.get(7) : "";
		String datea = values.size() > 8 ? (String) values.get(8) : "";
		extractDocs0(sources, linkm, articlem, datem, datef, pages, mindate, maxdate, datea);
		return new GroupAnswer();
	}
	Date minDate = null, maxDate = null;
	String articlem, datef, datem, datea;
	private String extractDocs0(String sources, String linkm, String articlem, String datem,
			String datef, String pages, String mindate, String maxdate, String datea) {
		extracted = 0;
		String returnVal = "redirect:/stat/docs";
		if (extraction) return returnVal;
		if (sources.isEmpty() || linkm.isEmpty() || articlem.isEmpty() || datem.isEmpty() || datef.isEmpty())
			return returnVal;
		extraction = true;
		sources = sources.replace("\n", " ");
		String[] allSources = sources.split(" ");

		// get new articles' urls
		sites = new HashMap<>();
		urls = new ArrayList<>();
		oldUrls = new ArrayList<>();
		for (Article doc : docDAO.getAll())
			oldUrls.add(doc.getUrl());
		for (int i = 0; i < allSources.length; i++) {
			getPosts(allSources[i], linkm, pages);
		}
		extracted = urls.size();
		
		// check date filter
		minDate = null;
		maxDate = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		try {
			if (!mindate.isEmpty())
				minDate = df.parse(mindate);
		} catch (ParseException e) {}
		try {
			if (!maxdate.isEmpty())
				maxDate = df.parse(maxdate);
		} catch (ParseException e) {}
		this.articlem = articlem;
		this.datef = datef;
		this.datem = datem;
		this.datea = datea;
		// analize docs
		//for (Article doc : inserted)
			//extractTerms(doc);
		extraction = false;
		return returnVal;
	}
	
	@ResponseBody
	@RequestMapping(value = "/ajaxGetDocs",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxGetDocs(
			@RequestBody String json) {
		// extract docs that suit filter
		if (urls.isEmpty())
			return new GroupAnswer(true);
		List<Article> docs = new ArrayList<>();
		int size = urls.size();
		for (int i = urls.size() - 1; i >= size - 50 && i >= 0; i--) {
			Article newDoc = getArticle(urls.get(i), articlem, datef, datem, datea);
			if (newDoc != null) {
			newDoc.setMask(articlem);
				boolean suit = minDate == null && maxDate == null ||
						newDoc.getCreationDate() != null &&
						(minDate == null || newDoc.getCreationDate().after(minDate)) &&
						(maxDate == null || newDoc.getCreationDate().before(maxDate));
				if (suit)
					docs.add(newDoc);
			}
			urls.remove(i);
		}
		docDAO.insertAll(docs);
		
		return new GroupAnswer();
	}
	private boolean extraction = false;
	@RequestMapping(path = { "/extractDocs" }, method = RequestMethod.POST)
	public String extractDocs(Model model, Principal principal
			, @ModelAttribute("sources") @Valid String sources
			, @ModelAttribute("linkm") @Valid String linkm
			, @ModelAttribute("articlem") @Valid String articlem
			, @ModelAttribute("datem") @Valid String datem
			, @ModelAttribute("datef") @Valid String datef
			, @ModelAttribute("pages") @Valid String pages
			, @ModelAttribute("mindate") @Valid String mindate
			, @ModelAttribute("maxdate") @Valid String maxdate
			, @ModelAttribute("datea") @Valid String datea
			) {
		return extractDocs0(sources, linkm, articlem, datem, datef, pages, mindate, maxdate, datea);
	}
	
	private Map<String, String> getPosts(String url, String postMask, String pages) {
		// define pages
		String initUrl = url, pageFormat = "";
		int startPage = 0, endPage = 0;
		int sepInd = pages.indexOf(" ");
		if (sepInd > 0) {
			String[] numbers = pages.substring(0, sepInd).split("-");
			startPage = parseInt(numbers[0], startPage);
			if (numbers.length > 1)
				endPage = parseInt(numbers[1], endPage);
			if (endPage < startPage) endPage = startPage;
			if (pages.length() > sepInd + 1)
				pageFormat = pages.substring(sepInd + 1);
		}
		for (int i = startPage; i <= endPage; i++) {
			if (i > 0) url = initUrl + pageFormat + i;
			
		checkError = "Wrong URL or page format";
		Document doc = getDoc(url);
		if (doc == null) continue;
		// find posts
		checkError = "Wrong link mask";
		Elements newsHeadlines = doc.select(postMask);
		for (Element headline : newsHeadlines) {
			String curUrl = headline.absUrl("href");
			if (oldUrls.contains(curUrl) || urls.contains(curUrl)) continue;
			urls.add(curUrl);
			String title = headline.text();
			sites.put(curUrl, title);
		}
		}
		return sites;
	}
	private int parseInt(String parsed, int defValue) {
		try {
			defValue = Integer.parseInt(parsed);
		} catch(NumberFormatException e) {}
		return defValue;
	}
	
	private Article getArticle(String url, String articleMask, String datef, String datem, String datea) {
		checkError = "Wrong article link";
		Document curDoc = getDoc(url);
		if (curDoc == null) return null;
		
		Article newDoc = new Article();
		newDoc.setUrl(url);
		newDoc.setTitle(sites.get(url));
		String text="";
		try {
			checkError = "Wrong article mask";
			text = curDoc.select(articleMask).first().text();
		} catch (NullPointerException e) {
			return null;
		}
		newDoc.setText(text);
		
		// get date
		checkError = "Wrong date mask";
		Elements times = curDoc.select(datem);
		Date minDate = null;
		checkError = "Wrong date format";
		SimpleDateFormat df = new SimpleDateFormat(datef, Locale.ENGLISH);
		for (Element time : times) {
			checkError = "Wrong date attribute";
			String attr = time.attr(datea);
			try {
				Date parse = df.parse(attr);
				if (minDate == null) minDate = parse;
				if (minDate.after(parse)) minDate = parse;
			} catch (ParseException e) {
			}
		}
		if (minDate != null)
			newDoc.setCreationDate(minDate);
		
		return newDoc;
	}
	
	public static Document getDoc(String url) {
		return getDoc(url, null, null);
	}
	
	public static Document getDoc(String url, String login, String password) {
		//WebDriver driver = new ChromeDrive(new ChromeP);
	    //driver.get(url);
	    //String html_content = driver.getPageSource();
	    
		Document doc = null;
		try {
			if (login == null || password == null)
				doc = Jsoup.connect(url).userAgent("Mozilla").get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//doc = Jsoup.parse(html_content);
		return doc;
	}


	@RequestMapping(path = "/clearFlags", method = RequestMethod.GET)
	public String clearFlags(Model model) {
		termExtraction = false;
		extraction = false;
		return docs(model);
	}
	
	private boolean termExtraction = false;

	//private List<Term> allTerms = null;
	private void extractTerms(Article doc) {
		if (doc == null) return;
		if (termExtraction) return;
		termExtraction = true;
		// get stemmed tokens
		PorterStemmer stemmer = new PorterStemmer();
		EnglishAnalyzer analyzer = new EnglishAnalyzer(Version.LUCENE_36);
		//CharArraySet stopWords = (CharArraySet) analyzer.getStopwordSet();
		List<String> stopWords = Arrays.asList(new String[]{"a", "about", "above", "after", "again", "against", "ain", "all", "am", "an", "and", "any", "are", "aren", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can", "couldn", "couldn't", "d", "did", "didn", "didn't", "do", "does", "doesn", "doesn't", "doing", "don", "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn", "hadn't", "has", "hasn", "hasn't", "have", "haven", "haven't", "having", "he", "her", "here", "hers", "herself", "him", "himself", "his", "how", "i", "if", "in", "into", "is", "isn", "isn't", "it", "it's", "its", "itself", "just", "ll", "m", "ma", "me", "mightn", "mightn't", "more", "most", "mustn", "mustn't", "my", "myself", "needn", "needn't", "no", "nor", "not", "now", "o", "of", "off", "on", "once", "only", "or", "other", "our", "ours", "ourselves", "out", "over", "own", "re", "s", "same", "shan", "shan't", "she", "she's", "should", "should've", "shouldn", "shouldn't", "so", "some", "such", "t", "than", "that", "that'll", "the", "their", "theirs", "them", "themselves", "then", "there", "these", "they", "this", "those", "through", "to", "too", "under", "until", "up", "ve", "very", "was", "wasn", "wasn't", "we", "were", "weren", "weren't", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "won", "won't", "wouldn", "wouldn't", "y", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves", "could", "he'd", "he'll", "he's", "here's", "how's", "i'd", "i'll", "i'm", "i've", "let's", "ought", "she'd", "she'll", "that's", "there's", "they'd", "they'll", "they're", "they've", "we'd", "we'll", "we're", "we've", "what's", "when's", "where's", "who's", "why's", "would"});
		List<Keyword> allKeys = new ArrayList<>();

		String[] curWords = prepareToTokenize(doc.getText()).split(" ");
		List<String> tokens = new ArrayList<>();
		for (int j = 0; j < curWords.length; j++) {
			String word1 = curWords[j];
			if (stopWords.contains(word1.toLowerCase())) continue;
			word1 = stemTerm(word1, stemmer);
			word1 = word1.trim();
			tokens.add(word1);
			String word2 = "";
			if (j + 1 < curWords.length) {
				word2 = curWords[j + 1];
				if (!stopWords.contains(word2.toLowerCase())) {
					word2 = stemTerm(word2, stemmer);
					word2 = word2.trim();
					String newWord = word1 + " " + word2;
					tokens.add(newWord);
				}
			}
			if (j + 2 < curWords.length) {
				String word3 = curWords[j + 2];
				if (stopWords.contains(word3.toLowerCase())) continue;
				word3 = stemTerm(word3, stemmer);
				word3 = word3.trim();
				String newWord = word1 + " " +
						(stopWords.contains(word2.toLowerCase()) ? "" : word2 + " ") + word3;
				tokens.add(newWord);
			}
		}
		analyzer.close();
		/* create a map for terms
		if (allTerms == null)
			allTerms = termDAO.getAll();
		if (keys == null) keys = new HashMap<>();
		if (keys.isEmpty())
		for (Term term : allTerms) {
			String stem = term.getStem();
			if (keys.containsKey(stem)) {
				
			} else {
				Keyword newKey = new Keyword();
				newKey.setTerm(term);
				keys.put(stem, newKey);
			}
		}*/
		// count terms in doc
		List<Term> newTerms = new ArrayList<>();
		Map<String, Keyword> keys= new HashMap<>();
		for (String token : tokens) {
			token = token.trim();
			if (keys.containsKey(token)) {
				keys.get(token).setCount(keys.get(token).getCount() + 1);
			} else {
				Term term = innDAO.getTermByStem(token);
				if (term == null) {
					term = new Term();
					term.setStem(token);
					newTerms.add(term);
				}
				Keyword newKey = new Keyword();
				newKey.setTerm(term);
				newKey.setCount(1);
				keys.put(token, newKey);
			}
		}
		
		// add variants in new terms
		for (Term term : newTerms) {
			List<String> unstemmed = getUnstemmed(term.getStem());
			for (String var : unstemmed)
				term.addVariant(var);
		}
		for (Entry<String, Keyword> e : keys.entrySet()) {
			// save not-zero values
			if (e.getValue().getCount() > 0) {
				e.getValue().setDoc(doc);
				allKeys.add(e.getValue());
			}
			// add variants in old terms
			if (e.getValue().getTerm().getId() > 0) {
				Term oldTerm = e.getValue().getTerm();
				List<String> unstemmed = getUnstemmed(oldTerm.getStem());
				int oldSize = oldTerm.getVariants().size();
				for (String var : unstemmed)
					oldTerm.addVariant(var);
				if (oldTerm.getVariants().size() > oldSize)
					termDAO.update(oldTerm);
			}
		}
		for (Keyword key : allKeys)
			keys.put(key.getTerm().getStem(), new Keyword(key));
		
		// insert new terms and update references
		if (!newTerms.isEmpty()) {
			List<Term> inserted = termDAO.insertAll(newTerms);
			//allTerms.addAll(inserted);
			Map<String, Term> insMap = new HashMap<>();
			for (Term ins : inserted)
				insMap.put(ins.getStem(), ins);
			for (Keyword key : allKeys)
				if (key.getTerm().getId() == 0) {
					Term newTerm = insMap.get(key.getTerm().getStem());
					if (newTerm != null)
						key.setTerm(newTerm);
				}
		}
		// insert keywords
		if (!allKeys.isEmpty())
			keyDAO.insertAll(allKeys);
		// update wordcount
		doc.setWordcount(tokens.size());
		docDAO.update(doc);
		wordsMap.clear();
		termExtraction = false;
	}
	
	private List<String> getUnstemmed(String stemmed) {
		List<String> toReturn = null;
		if (wordsMap != null)
			if (wordsMap.containsKey(stemmed)) {
				toReturn = wordsMap.get(stemmed);
			} else {
				// make variants for terms with several words
				String[] curWords = stemmed.split(" ");
				for (int i = 0; i < curWords.length; i++) {
					List<String> list = wordsMap.get(curWords[i]);
					if (toReturn == null)
						toReturn = new ArrayList<>(list);
					else {
						List<String> oldVars = new ArrayList<>(toReturn);
						toReturn.clear();
						for (String var : oldVars)
							for (String add : list)
								toReturn.add(var + " " + add);
					}
				}
		}

		if (toReturn == null)
			toReturn = new ArrayList<>();
		if (toReturn.isEmpty())
			toReturn.add(stemmed);
		return toReturn;
	}

	private List<String> getUnstemmedOld(String stemmed) {
		List<String> toReturn = null;
		if (wordsMap == null) {
			toReturn = new ArrayList<>();
			toReturn.add(stemmed);
			return toReturn;
		}
		if (wordsMap.containsKey(stemmed)) {
			toReturn = wordsMap.get(stemmed);
		} else {
			String[] curWords = stemmed.split(" ");
			for (int ind = 0; ind < curWords.length; ind++)
				if (wordsMap.containsKey(curWords[ind])) {
					String fromReplace = curWords[ind], toReplace = curWords[ind];
					List<String> vars = wordsMap.get(curWords[ind]);
					if (vars != null && !vars.isEmpty())
						toReplace = vars.get(0);
					if (ind > 0) { toReplace = " " + toReplace; fromReplace = " " + fromReplace; }
					if (ind < curWords.length - 1) { toReplace += " "; fromReplace += " "; }
					stemmed = stemmed.replace(fromReplace, toReplace);
				}
		}
		if (toReturn == null)
			toReturn = new ArrayList<>();
		if (toReturn.isEmpty())
			toReturn.add(stemmed);
		return toReturn;
	}
	
	private static String stemTermSt(String term, PorterStemmer stemmer) {
		term = term.toLowerCase();
		stemmer.setCurrent(term);
		stemmer.stem();
		String stemmed = stemmer.getCurrent().toLowerCase();
		if (stemmed.length() > 1) {
			if (stemmed.startsWith("'"))
				stemmed = stemmed.substring(1);
			if (stemmed.endsWith("'"))
				stemmed = stemmed.substring(0, stemmed.length() - 1);
		}
		return stemmed;
	}
	private String stemTerm(String term, PorterStemmer stemmer) {
		//if (!term.isEmpty()) return term;		// stemming is not using
		String value = term.toLowerCase();
		String stemmed = stemTermSt(term, stemmer);
		if (wordsMap == null)
			wordsMap = new HashMap<>();
		List<String> variants = new ArrayList<>();
		if (wordsMap.containsKey(stemmed)) {
			variants = wordsMap.get(stemmed);
			if (variants == null)
				variants = new ArrayList<>();
		}
		if (!variants.contains(value))
			variants.add(value);
		wordsMap.put(stemmed, variants);
		return stemmed;
	}
	
	private String prepareToTokenize(String corpus) {
		// remove symbols and digits
		/*String[] noizeSymbs = {",", ";", ".", "[", "]", "\"", "{", "}", "/", "\\", "!", "?", "#", "$", "‘", "’", "…",
				"“", "”", "(", ")", "<", ">", "- ", " -", "+", "%", "*", ":", "--", "–", "—–", "&", "|", "~", "•"};
		for (int i = 0; i < noizeSymbs.length; i++)
			corpus = corpus.replace(noizeSymbs[i], " ");
		for (int i = 0; i < 10; i++)
			corpus = corpus.replace(i+"", "");*/
		// remove all except letters
		corpus = " " + corpus + " ";
		corpus = corpus.replaceAll("[^\\w]", "  ").replaceAll(" [0-9]+ ", " ");
		// remove one letter words
		boolean changes = true;
		while (changes) {
			String s = corpus.replaceAll(" \\w ", " ").replaceAll(" [0-9] ", " ");
			changes = s.length() != corpus.length();
			corpus = s;
		}
		while (corpus.indexOf("  ") >= 0)
			corpus = corpus.replace("  ", " ");
		return corpus.trim();//.toLowerCase();
	}
}
