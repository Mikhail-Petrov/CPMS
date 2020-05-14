package com.cpms.web.controllers;

import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.validation.Valid;

import org.apache.lucene.analysis.CharArraySet;
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

import com.cpms.dao.implementations.jpa.JPADocumentDAO;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.Article;
import com.cpms.data.entities.Keyword;
import com.cpms.data.entities.Term;
import com.cpms.web.UserSessionData;
import com.cpms.web.ajax.GroupAnswer;
import com.cpms.web.ajax.IAjaxAnswer;

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
	@Qualifier(value = "keywordDAO")
	private IDAO<Keyword> keyDAO;

	@Autowired
	@Qualifier(value = "userSessionData")
	private UserSessionData sessionData;

    @Autowired
    private MessageSource messageSource;


	Map<String, String> sites;
	Map<String, Keyword> keys;
	List<String> words = new ArrayList<>();

	private HashMap<String, List<String>> wordsMap;
	List<String> urls = new ArrayList<>(), oldUrls = new ArrayList<>();

	List<Long> nokeysDocs = new ArrayList<>();

	@SuppressWarnings("deprecation")
	@RequestMapping(path = "/docs", method = RequestMethod.GET)
	public String docs(Model model) {
		model.addAttribute("_VIEW_TITLE", "users.management.title");
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

	@RequestMapping(path = "/terms", method = RequestMethod.GET)
	public String terms(Model model) {
		model.addAttribute("_VIEW_TITLE", "users.management.title");
		model.addAttribute("_FORCE_CSRF", true);

		model.addAttribute("amount", termDAO.count());
		model.addAttribute("maxid", 0);
		model.addAttribute("repeats", 50);
		
		List<Long> nokeys = new ArrayList<>();
		nokeys.add(156L);
		nokeys.add(146L);
		model.addAttribute("nokeys", nokeys);
		model.addAttribute("nokeyssize", 110);
		return "terms";
	}

	@RequestMapping(path = "/keys", method = RequestMethod.GET)
	public String keys(Model model) {
		model.addAttribute("_VIEW_TITLE", "users.management.title");
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
		for (Long id : nokeysDocs)
			extractTerms(docDAO.getOne(id));
		return new GroupAnswer();
	}

	@RequestMapping(path = { "/clearTermsRep" }, method = RequestMethod.GET)
	public String clearTermsRep(Model model) {
		// TODO: keep terms repeat after /terms-get
		return "redirect:/stat/terms";
	}

	@RequestMapping(path = { "/deleteBadTerms" }, method = RequestMethod.GET)
	public String deleteBadTerms(Model model, @RequestParam(name = "id", required = false) Long id) {
		// TODO: keep terms without keywords after /terms-get
		
		return "redirect:/stat/terms";
	}

	@RequestMapping(path = { "/removeBadKeys" }, method = RequestMethod.GET)
	public String removeBadKeys(Model model, @RequestParam(name = "isTerm", required = true) boolean isTerm) {
		// TODO: keep keys with bad links after /keys-get
		
		return "redirect:/stat/keys";
	}

	@RequestMapping(path = { "/reboundKeysRep" }, method = RequestMethod.GET)
	public String reboundKeysRep(Model model, @RequestParam(name = "id", required = false) Long id) {
		// TODO: keep keys repeat after /keys-get
		
		return "redirect:/stat/keys";
	}

	@RequestMapping(path = { "/extract" })
	public String extract(Model model) {
		model.addAttribute("_VIEW_TITLE", "users.management.title");
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("linkm", "h2 a");
		model.addAttribute("articlem", "div[class=entry-content]");
		model.addAttribute("datem", "time");
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
		extractDocs0(sources, linkm, articlem, datem, datef, pages, mindate, maxdate);
		return new GroupAnswer();
	}

	private String extractDocs0(String sources, String linkm, String articlem, String datem,
			String datef, String pages, String mindate, String maxdate) {
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
		
		// check date filter
		Date minDate = null, maxDate = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		try {
			if (!mindate.isEmpty())
				minDate = df.parse(mindate);
		} catch (ParseException e) {}
		try {
			if (!maxdate.isEmpty())
				maxDate = df.parse(maxdate);
		} catch (ParseException e) {}
		// extract docs that suit filter
		List<Article> docs = new ArrayList<>();
		for (String url : urls) {
			Article newDoc = getPosts2(url, articlem, datef, datem);
			if (newDoc != null) {
				boolean suit = minDate == null && maxDate == null ||
						newDoc.getCreationDate() != null &&
						(minDate == null || newDoc.getCreationDate().after(minDate)) &&
						(maxDate == null || newDoc.getCreationDate().before(maxDate));
				if (suit)
					docs.add(newDoc);
			}
		}
		List<Article> inserted = docDAO.insertAll(docs);
		
		// analize docs
		for (Article doc : inserted)
			extractTerms(doc);
		extraction = false;
		return returnVal;
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
			) {
		return extractDocs0(sources, linkm, articlem, datem, datef, pages, mindate, maxdate);
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
			if (i > 0) url = initUrl + pageFormat + i + "/";
			
		Document doc = getDoc(url);
		if (doc == null) continue;
		// find posts
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
	
	private Article getPosts2(String url, String articleMask, String datef, String datem) {
		Document curDoc = getDoc(url);
		if (curDoc == null) return null;
		
		Article newDoc = new Article();
		newDoc.setUrl(url);
		newDoc.setTitle(sites.get(url));
		String text = curDoc.select(articleMask).first().text();
		newDoc.setText(text);
		
		// get date
		Elements times = curDoc.select(datem);
		Date minDate = null;
		SimpleDateFormat df = new SimpleDateFormat(datef, Locale.ENGLISH);
		for (Element time : times) {
			String attr = time.attr("datetime");
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
		//WebDriver driver = new ChromeDrive(new ChromeP);
	    //driver.get(url);
	    //String html_content = driver.getPageSource();
	    
		Document doc = null;
		try {
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

	private List<Term> allTerms = null;
	private void extractTerms(Article doc) {
		if (doc == null) return;
		if (termExtraction) return;
		termExtraction = true;
		// get stemmed tokens
		PorterStemmer stemmer = new PorterStemmer();
		EnglishAnalyzer analyzer = new EnglishAnalyzer(Version.LUCENE_36);
		CharArraySet stopWords = (CharArraySet) analyzer.getStopwordSet();
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
		// create a map for terms
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
		}
		// count terms in doc
		List<Term> newTerms = new ArrayList<>();
		for (String token : tokens) {
			token = token.trim();
			if (keys.containsKey(token)) {
				keys.get(token).setCount(keys.get(token).getCount() + 1);
			} else {
				Term term = new Term();
				List<String> unstemmed = getUnstemmed(token);
				for (String var : unstemmed)
					term.addVariant(var);
				term.setStem(token);
				newTerms.add(term);
				Keyword newKey = new Keyword();
				newKey.setTerm(term);
				newKey.setCount(1);
				keys.put(token, newKey);
			}
		}
		
		// save not-zero values
		for (Entry<String, Keyword> e : keys.entrySet()) {
			if (e.getValue().getCount() > 0) {
				e.getValue().setDoc(doc);
				allKeys.add(e.getValue());
			}
		}
		for (Keyword key : allKeys)
			keys.put(key.getTerm().getStem(), new Keyword(key));
		
		// insert new terms and update references
		if (!newTerms.isEmpty()) {
			List<Term> inserted = termDAO.insertAll(newTerms);
			allTerms.addAll(inserted);
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
		termExtraction = false;
	}

	private List<String> getUnstemmed(String stemmed) {
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
	
	private String stemTerm(String term, PorterStemmer stemmer) {
		//if (!term.isEmpty()) return term;		// stemming is not using
		term = term.toLowerCase();
		String value = term;
		stemmer.setCurrent(term);
		stemmer.stem();
		String stemmed = stemmer.getCurrent().toLowerCase();
		if (stemmed.length() > 1) {
			if (stemmed.startsWith("'"))
				stemmed = stemmed.substring(1);
			if (stemmed.endsWith("'"))
				stemmed = stemmed.substring(0, stemmed.length() - 1);
		}
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
		String[] noizeSymbs = {",", ";", ".", "[", "]", "\"", "{", "}", "/", "\\", "!", "?", "#", "$", "‘", "’", "…",
				"“", "”", "(", ")", "<", ">", "- ", " -", "+", "%", "*", ":", "--", "–", "—–", "&", "|", "~", "•"};
		for (int i = 0; i < noizeSymbs.length; i++)
			corpus = corpus.replace(noizeSymbs[i], " ");
		for (int i = 0; i < 10; i++)
			corpus = corpus.replace(i+"", "");
		while (corpus.indexOf("  ") >= 0)
			corpus = corpus.replace("  ", " ");
		return corpus;//.toLowerCase();
	}
}
