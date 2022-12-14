
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
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
import com.cpms.data.entities.DocumentCategory;
import com.cpms.data.entities.DocumentTrend;
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
import com.cpms.data.entities.VoteResults;
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
	public static int tlimit = 50, sdDelay = 3, osdDelay = 5, ldDays = 30, suggestedSk = 5;
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
		model.addAttribute("_VIEW_TITLE", "Innovations");
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("_NAMED_TITLE", true);
		
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

	@RequestMapping(path = "/radar", method = RequestMethod.GET)
	public String radar(Model model) {
		model.addAttribute("_VIEW_TITLE", "im.title.innovations");
		model.addAttribute("_FORCE_CSRF", true);

		model.addAttribute("catList", getCatListOld());
		
		List<VoteResults> categs = new ArrayList<>();
		List<String> labels = new ArrayList<>();
		List<Double[]> data = new ArrayList<>();
		
		List<Long> catIDs = new ArrayList<>();
		List<List<List<Integer>>> N = new ArrayList<>();
		// get all parent categories
		for (Category cat : facade.getCategoryDAO().getAll()) {
			if (cat.getParent() != null)
				continue;
			VoteResults res = new VoteResults();
			res.setName(cat.getName());
			res.setValue(1);
			res.setPercent(1);
			categs.add(res);
			catIDs.add(cat.getId());
			List<List<Integer>> impacts = new ArrayList<>();
			for (int i = 0; i < 3; i++)
				impacts.add(new ArrayList<>());
			N.add(impacts);
		}
		// get and count all innovations
		List<Task> inns = new ArrayList<>();
		List<Double> k = new ArrayList<>();
		for (long innID : facade.getTaskDAO().getIDs()) {
			Task inn = facade.getTaskDAO().getOne(innID);
			inn.setImpact(Math.abs(2 - inn.getImpact()));		// change low and high impact, so high is closer to the center
			Set<Task_Category> categories = inn.getCategories();
			if (categories == null || categories.isEmpty())
				continue;
			inns.add(inn);
			Category parent = null;
			for (Task_Category tc : categories) {
				parent = tc.getCategory();
				break;
			}
			while (parent.getParent() != null)
				parent = parent.getParent();
			Integer impact = inn.getImpact();
			if (impact == null)
				impact = 0;
			int indexOf = catIDs.indexOf(parent.getId());
			k.add((double) indexOf);
			N.get(indexOf).get(impact).add(inns.size() - 1);
		}
		// get coordinates for innovations
		Date startDate = docDAO.getDate(false);
		Date finishDate = new Date(System.currentTimeMillis());
		for (List<List<Integer>> catN : N) {
			for (List<Integer> impN : catN) {
				double axe, r = 0;
				do axe = Math.ceil(impN.size() / ++r);
				while (axe / r > 3);
				double ai = 0, ri = 0;
				for (Integer innIndex : impN) {
					Task inn = inns.get(innIndex);
					double dr = (ri + 0.5)/r,
							rad = 2.15 * ((double) inn.getImpact() + 1.0 + dr) / 4.0,
							da = (ai + 0.5) / axe;
					double angle = Math.PI * 2 / (double) catIDs.size() * (k.get(innIndex) + da);
					double x = Math.sin(angle) * rad,
							y = Math.cos(angle) * rad;
					if (++ai >= axe) {
						ai = 0;
						ri++;
					}
					Double[] xy = {x, y};
					data.add(xy);
					List<Long> terms = new ArrayList<>();
					terms.add(inn.getVariant().getTerm().getId());
					for (ProjectTermvariant ptv : inn.getVariants())
						terms.add(ptv.getVariant().getTerm().getId());
					labels.add(String.format("%d\n%s\nNumber of documents: %d", inn.getId(), inn.getName(),
							innDAO.getTermDocCount(terms, startDate, finishDate)));
				}
			}
		}

		model.addAttribute("categs", categs);
		model.addAttribute("labels", labels);
		model.addAttribute("data", data);
		
		return "radar";
	}
	
	private List<List<List<String>>> getCatListOld() {
		List<List<List<String>>> catList = new ArrayList<>();
		List<Category> cats = categoryDAO.getAll();
		Map<Long, Integer> catIndexes = new HashMap<>();
		for (int i = 0; i < cats.size(); i++) {
			if (cats.get(i).getParent() != null)
				continue;
			List<List<String>> cat = new ArrayList<>();
			List<String> name = new ArrayList<>();
			name.add(cats.get(i).getName());
			cat.add(name);
			for (int j = 0; j < 3; j++)
				cat.add(new ArrayList<>());
			catList.add(cat);
			catIndexes.put(cats.get(i).getId(), catList.size() - 1);
		}
		List<Task> all = facade.getTaskDAO().getAll();
		for (Task task : all)
			if (task.getVariant() != null)
				for (Task_Category tc : task.getCategories()) {
					long catId = tc.getCategory().getId();
					if (tc.getCategory().getParent() != null)
						catId = tc.getCategory().getParent().getId();
					List<String> toAdd = catList.get(catIndexes.get(catId)).get(task.getImpact()+1);
					if (!toAdd.contains(task.getName()))
						toAdd.add(task.getName());
				}
		return catList;
	}

	@RequestMapping(path = "/documents", method = RequestMethod.GET)
	public String documents(Model model, @RequestParam(value = "catid", required = false) Long catid) {
		model.addAttribute("_VIEW_TITLE", "Last documents");
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("_NAMED_TITLE", true);
		
		List<Article> docsList = new ArrayList<>();
		//getCatsTrends();
		List<Long> cats = null //allCats
				, trends = null; //allTrends;
		Category cat = null;
		if (catid != null) {
			cat = categoryDAO.getOne(catid);
			cats = getCatChildIDs(cat);
		}
		for (BigInteger id : innDAO.getLastDocs(changeDate(new Date(System.currentTimeMillis()), 0, 0, -ldDays), cats, trends))
			docsList.add(docDAO.getOne(id.longValue()));
		model.addAttribute("docsList", docsList);
		List<Category> categories = categoryDAO.getAll(), categs = new ArrayList<>();
		Collections.sort(categories);
		for (Category categ : categories)
			if (categ.getParent() == null)
				categs.add(categ);
		model.addAttribute("categs", categs);
		// get categories and kids
		Map<Long, List<Category>> cak = new HashMap<>();
		for (Category category : categories) {
			Set<Category> children = category.getChildren(categoryDAO);
			if (children.size() > 0 || category.getParent() == null) {
				List<Category> kids = new ArrayList<>();
				for (Category child : children)
					kids.add(child);
				cak.put(category.getId(), kids);
			}
		}
		model.addAttribute("catKids", cak);
		if (cat == null || cat.getParent() == null) {
			model.addAttribute("catid", catid);
			model.addAttribute("childid", 0);
		} else {
			model.addAttribute("catid", cat.getParent().getId());
			model.addAttribute("childid", catid);
		}
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
		List<Term> terms = new ArrayList<>();
		terms.add(term);
		// find associated terms
		for (Task task : facade.getTaskDAO().getAll())
			if (task.getVariant() != null && task.getVariant().getTerm().getId() == term.getId()) {
				Task innTask = facade.getTaskDAO().getOne(task.getId());
				for (ProjectTermvariant ptv : innTask.getVariants()) {
					boolean contains = false;
					for (Term asTerm : terms)
						if (asTerm.getId() == ptv.getVariant().getTerm().getId()) {
							contains = true;
							break;
						}
					if (!contains)
						terms.add(ptv.getVariant().getTerm());
				}
			}
		for (Term asTerm : terms)
		for (TermVariant var : asTerm.getVariants())
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
				tasks.put(task.getVariant().getTerm().getId(), task);//facade.getTaskDAO().getOne(task.getId()));
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
	@RequestMapping(value = "/saveDocChange",
			method = RequestMethod.POST)
	public IAjaxAnswer ajaxSaveDoc(
			@RequestBody String json) {
		List<Object> values = DashboardAjax.parseJson(json, messageSource);
		// get doc
		long id = values.size() > 0 ? (Integer) values.get(0) : 0;
		if (id <= 0) return null;
		Article doc = docDAO.getOne(id);
		// update categories
		Set<DocumentCategory> oldCats = doc.getCats();
		doc.clearCats();
		String cats = values.size() > 1 ? (String) values.get(1) : "";
		String[] split = cats.split(",");
		for (int i = 0; i < split.length; i++) {
			long catId = 0;
			try {catId = Long.parseLong(split[i]); } catch(NumberFormatException e) {continue;}
			DocumentCategory dc = null;
			for (DocumentCategory oldCat : oldCats)
				if (oldCat.getCategory().getId() == catId)
					dc = oldCat;
			if (dc == null) {
				Category category = facade.getCategoryDAO().getOne(catId);
				dc = new DocumentCategory(category, doc);
			}
			dc.setManual(1);
			doc.addCat(dc);
		}
		// update trends
		Set<DocumentTrend> oldTrends = doc.getTrends();
		doc.clearTrends();
		String trends = values.size() > 2 ? (String) values.get(2) : "";
		split = trends.split(",");
		for (int i = 0; i < split.length; i++) {
			long trendId = 0;
			try {trendId = Long.parseLong(split[i]); } catch(NumberFormatException e) {continue;}
			DocumentTrend dt = null;
			for (DocumentTrend oldTrend : oldTrends)
				if (oldTrend.getTrend().getId() == trendId)
					dt = oldTrend;
			if (dt == null) {
				Trend trend = facade.getTrendDAO().getOne(trendId);
				dt = new DocumentTrend(trend, doc);
			}
			dt.setManual(1);
			doc.addTrend(dt);
		}
		docDAO.update(doc);
		return new GroupAnswer();
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
				tasks.put(task.getVariant().getTerm().getId(), task);//facade.getTaskDAO().getOne(task.getId()));
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
	
	@RequestMapping(path = "/docs", method = RequestMethod.GET)
	public String docs(Model model, @RequestParam(value = "catid", required = false) Long catid
			, @RequestParam(value = "startDate", required = false) String start_Date
			, @RequestParam(value = "finishDate", required = false) String finish_Date) {
		model.addAttribute("_VIEW_TITLE", "im.title.docs");
		model.addAttribute("_FORCE_CSRF", true);
		
		model.addAttribute("amount", docDAO.count());
		model.addAttribute("parsed", docDAO.getDate(true));
		
		// get statistics for graph
		Map<String, Integer> stats = new LinkedHashMap<>();
		//getCatsTrends();
		List<Long> trends = null;//allTrends;
		if (catid == null) catid = 0L;
		Map<Long, List<Long>> catMap = new LinkedHashMap<>();
		if (catid > 0)
			catMap.put(catid, new ArrayList<>());
		List<Category> allCategs = facade.getCategoryDAO().getAll(), categs = new ArrayList<>();
		// find root categories or subcategories
		for (Category cat : allCategs) {
			if (catid <= 0 && cat.getParent() == null || cat.getParent() != null && cat.getParent().getId() == catid)
				catMap.put(cat.getId(), new ArrayList<>());
			if (cat.getParent() == null)
				categs.add(cat);
		}
		model.addAttribute("categs", categs);
		// create categories id map
		for (Category cat : allCategs) {
			long curID = cat.getId();
			if (catMap.containsKey(curID) && !catMap.get(curID).contains(cat.getId()))
				catMap.get(curID).add(cat.getId());
			if (cat.getParent() != null) {
				curID = cat.getParent().getId();
				if (catMap.containsKey(curID) && !catMap.get(curID).contains(cat.getId()))
					catMap.get(curID).add(cat.getId());
			}
		}
		Date startDate = docDAO.getDate(false), finishDate = new Date(System.currentTimeMillis());
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		try {
			if (start_Date != null && !start_Date.isEmpty()) startDate = df.parse(start_Date);
			if (finish_Date != null && !finish_Date.isEmpty()) finishDate = df.parse(finish_Date);
		} catch (ParseException e) {}
		model.addAttribute("startDate", df.format(startDate));
		model.addAttribute("finishDate", df.format(finishDate));
		if (catid > 0) {
			Category curCat = facade.getCategoryDAO().getOne(catid);
			int count = innDAO.getDocCount(startDate , finishDate, catMap.get(catid), trends);
			stats.put(String.format("<b><p>%s</p><p>%d</p></b> (id:%d)", curCat.getName(), count, curCat.getId()), count);
		}
		for (Category cat : allCategs)
			if (cat.getId() != catid && catMap.containsKey(cat.getId())) {
				int count = innDAO.getDocCount(startDate , finishDate, catMap.get(cat.getId()), trends);
				stats.put(String.format("<p>%s</p><p>%d</p> (id:%d)", cat.getName(), count, cat.getId()), count);
			}

		model.addAttribute("stats", stats);
		model.addAttribute("catid", catid);
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
		List<Long> cats = allCats, trends = allTrends, terms = new ArrayList<>();
		terms.add(term.getId());
		// find associated terms
		for (Task task : facade.getTaskDAO().getAll())
			if (task.getVariant() != null && task.getVariant().getTerm().getId() == term.getId()) {
				Task innTask = facade.getTaskDAO().getOne(task.getId());
				for (ProjectTermvariant ptv : innTask.getVariants())
					if (!terms.contains(ptv.getVariant().getTerm().getId()))
						terms.add(ptv.getVariant().getTerm().getId());
			}
		int tdc30 = innDAO.getTermDocCount(terms, changeDate(today, 0, 0, -30), today),
				tdc3 = innDAO.getTermDocCount(terms, changeDate(today, 0, -3, 0), today),
				tdc1 = innDAO.getTermDocCount(terms, changeDate(today, -1, 0, 0), today),
				dc30 = innDAO.getDocCount(changeDate(today, 0, 0, -30), today, cats, trends),
				dc3 = innDAO.getDocCount(changeDate(today, 0, -3, 0), today, cats, trends),
				dc1 = innDAO.getDocCount(changeDate(today, -1, 0, 0), today, cats, trends);
		int[][] stat = {
				{
					innDAO.getTermSum(terms, changeDate(today, 0, 0, -30), today),
					tdc30,
					dc30 == 0 ? 0 : (int)((float) tdc30 / (float) dc30 * 100.0)
				}, {
					innDAO.getTermSum(terms, changeDate(today, 0, -3, 0), today),
					tdc3,
					dc3 == 0 ? 0 : (int)((float) tdc3 / (float) dc3 * 100.0)
				}, {
					innDAO.getTermSum(terms, changeDate(today, -1, 0, 0), today),
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
				sum.add((float) innDAO.getTermSum(terms, today, getPlusMonth(today)));
				float termDocCount = (float) innDAO.getTermDocCount(terms, today, getPlusMonth(today));
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
		for (BigInteger docID : innDAO.getTermDocsIDs(terms, order))
			//keys.add(keyDAO.getOne(docID.longValue()));
			keys.add(getKeyword(terms, docID.longValue()));
		model.addAttribute("keys", keys);
		time("keys");
		List<String[]> docCats = new ArrayList<>();
		for (Keyword key : keys)
			for (DocumentCategory dc : key.getDoc().getCats()) {
				String docCatID = String.format("%d-%d", dc.getDocument().getId(), dc.getCategory().getId()),
						name = dc.getCategory().getName();
				String[] docCat = {docCatID, name};
				docCats.add(docCat);
		}
		model.addAttribute("docCats", docCats);
		List<String[]> docTrends = new ArrayList<>();
		for (Keyword key : keys)
			for (DocumentTrend dt : key.getDoc().getTrends()) {
				String docTrID = String.format("%d-%d", dt.getDocument().getId(), dt.getTrend().getId()),
						name = dt.getTrend().getName();
				String[] docTr = {docTrID, name};
				docTrends.add(docTr);
		}
		model.addAttribute("docTrends", docTrends);
		model.addAttribute("order", order);

		model.addAttribute("termid", id);
		model.addAttribute("categs", facade.getCategoryDAO().getAll());
		model.addAttribute("trends", facade.getTrendDAO().getAll());
		return "viewTerm";
	}
	
	private Keyword getKeyword(List<Long> terms, long docid) {
		Keyword res = new Keyword();
		res.setCount(innDAO.getTermCount(terms, docid));
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
	public IAjaxAnswer ajaxAnalizeDocs(HttpServletRequest request,
			@RequestBody String json) {
		if (request.getSession().getMaxInactiveInterval() > 0)
			request.getSession().setMaxInactiveInterval(0);
		//nokeysDocs.clear();
		if (nokeysDocs.isEmpty())
			for (Long obj : docDAO.getIDs())
			//if (obj.getWordcount() == 0)
				nokeysDocs.add(obj);
		//for (Long id : nokeysDocs)
			//extractTerms(docDAO.getOne(id));
		if (nokeysDocs.isEmpty())
			return new GroupAnswer(true);
		extractTerms(docDAO.getOne(nokeysDocs.get(0)));
		nokeysDocs.remove(0);
		return new GroupAnswer(String.format("Unindexed documents left: %d", nokeysDocs.size()));
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
	public IAjaxAnswer loadDocs(@RequestBody String json, HttpServletRequest request) {
		if (curWebsites == null) {
			if (request.getSession().getMaxInactiveInterval() > 0)
				request.getSession().setMaxInactiveInterval(0);
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
			if (article == null || article.getCreationDate() == null || article.getCreationDate().before(compareDate)) {
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
			return new GroupAnswer(String.format("Article extraction from %s<br>Articles left: %d", curWeb.getName(), urls.size()));
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
		
		if (oldUrls != null)
			oldUrls.add(newDoc.getUrl());
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
		return docs(model, null, null, null);
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
	
	public static String stemTermSt(String term, PorterStemmer stemmer) {
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
	
	public static String prepareToTokenize(String corpus) {
		// remove symbols and digits
		/*String[] noizeSymbs = {",", ";", ".", "[", "]", "\"", "{", "}", "/", "\\", "!", "?", "#", "$", "?", "?", "?",
				"?", "?", "(", ")", "<", ">", "- ", " -", "+", "%", "*", ":", "--", "?", "??", "&", "|", "~", "?"};
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
