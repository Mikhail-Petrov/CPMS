package com.cpms.web.controllers;

import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.tartarus.snowball.ext.PorterStemmer;

import com.cpms.dao.interfaces.IDAO;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Article;
import com.cpms.data.entities.Keyword;
import com.cpms.data.entities.Message;
import com.cpms.data.entities.MessageCenter;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.Term;
import com.cpms.data.entities.TestConfig;
import com.cpms.exceptions.SessionExpiredException;
import com.cpms.exceptions.WrongUserProfileException;
import com.cpms.security.RegistrationForm;
import com.cpms.security.RoleTypes;
import com.cpms.security.entities.Role;
import com.cpms.security.entities.Users;
import com.cpms.security.entities.UserData;
import com.cpms.web.UserSessionData;

/**
 * Handles user creating, login operations and user viewing.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/security/")
public class Security {

	public static String adminName = "admin", adminPassword = "admin";

	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;

	@Autowired
	@Qualifier(value = "profileDAO")
	private IDAO<Profile> profileDAO;

	@Autowired
	@Qualifier(value = "messageDAO")
	private IDAO<Message> messageDAO;

	@Autowired
	@Qualifier(value = "taskDAO")
	private IDAO<Task> taskDAO;
	
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

	private ArrayList<String> urls, oldUrls;

	final private String docsSep = "\r\n";
	private String corpus, keyWords;
	private long secs;
	Map<String, String> sites;
	Map<String, Keyword> keys;
	
	List<List<Double>> matTF = new ArrayList<>(), matIDF = new ArrayList<>(),
			//matT = new ArrayList<>(), matD = new ArrayList<>(),   
			matTFIDF = new ArrayList<>();
	List<String> words = new ArrayList<>();

	private HashMap<String, String> wordsMap;

	public static Users getUser(Principal principal, IUserDAO userDAO) {
		if (principal == null) return null;
		Users user = null;
		String username = ((UsernamePasswordAuthenticationToken) principal).getName();
		if (!username.equals(Security.adminName))
			user = userDAO.getByUsername(username);
		return user;
	}

	@RequestMapping(path = "/register", method = RequestMethod.GET)
	public String register(Model model, @RequestParam(name = "userId", required = false) Long id) {
		boolean isCreate = false;
		if (id == null || id == 0 || userDAO.getByUserID(id) == null)
			isCreate = true;
		if (isCreate)
			model.addAttribute("_VIEW_TITLE", "title.register");
		else
			model.addAttribute("_VIEW_TITLE", "title.edit.user");
		model.addAttribute("_FORCE_CSRF", true);
		RegistrationForm form = new RegistrationForm();
		Profile profile = sessionData.getProfile();
		if (profile != null)
			form.setProfileId(profile.getId());
		if (!isCreate) {
			Users user = userDAO.getByUserID(id);
			form.setId(id);
			form.setRole(user.getRole());
			form.setUsername(user.getUsername());
			if (form.getRole() != null && form.getRole().equals(RoleTypes.EXPERT.toRoleName()))
				form.setProfileId(user.getProfileId());
			form.setEmail(user.getEmail());
		}
		model.addAttribute("registrationForm", form);
		model.addAttribute("isCreate", isCreate);
		List<String> roleList = new ArrayList<>();
		for (RoleTypes role : RoleTypes.values())
			roleList.add(role.toRoleName());
		model.addAttribute("roleList", roleList);
		model.addAttribute("expertRole", RoleTypes.EXPERT.toRoleName());
		// Get profiles which are not attached to user (plus this user's profile)
		List<Profile> profileList = profileDAO.getAll(), removeList = new ArrayList<>();
		for (Profile profileInList : profileList) {
			Users profileUser = userDAO.getByProfile(profileInList);
			if (profileUser != null && (id == null || profileUser.getId() != id))
				removeList.add(profileInList);
		}
		for (Profile profileInList : removeList)
			profileList.remove(profileInList);
		Collections.sort(profileList);
		model.addAttribute("profileList", profileList);
		return "register";
	}

	@RequestMapping(path = "/register", method = RequestMethod.POST)
	public String registerPost(HttpServletRequest request,
			@ModelAttribute("registrationForm") @Valid RegistrationForm registrationForm, BindingResult bindingResult,
			Model model) {
		if (registrationForm == null) {
			throw new SessionExpiredException(null, messageSource);
		}
		if (bindingResult.hasErrors()) {
			model.addAttribute("_VIEW_TITLE", "title.register");
			model.addAttribute("_FORCE_CSRF", true);
			return "register";
		}
		Users user = new Users();
		boolean isCreate = false;
		Long userId = registrationForm.id;
		if (userId == null)
			userId = 0L;
		if (userId == 0 || userDAO.getByUserID(userId) == null)
			isCreate = true;
		if (!isCreate)
			user = userDAO.getByUserID(userId);
		user.setUsername(registrationForm.getUsername());
		if (registrationForm.getRole().equals(RoleTypes.EXPERT.toRoleName()))
			if (registrationForm.getProfileId() != null)
				user.setProfileId(registrationForm.getProfileId());
			else
				user.setProfileId(null);
		if (isCreate || registrationForm.getPassword() != null && !registrationForm.getPassword().isEmpty())
			user.setPassword(registrationForm.getPassword());
		else
			user.setPassword("123");
		user.setEmail(registrationForm.getEmail());
		Role newRole = new Role();
		newRole.setRolename(registrationForm.role);
		user.addRole(newRole);
		if (isCreate)
			userDAO.insertUser(user);
		else
			userDAO.updateUser(user);
		return "redirect:/security/users";
	}

	private void correctProfileCheck(Profile profile, HttpServletRequest request, long userId) {
		if (profile == null) {
			throw new WrongUserProfileException(UserSessionData.localizeText(
					"Вы не выбрали профиль", messageSource), request.getPathInfo(), messageSource);
		}
		Users profileUser = userDAO.getByProfile(profile);
		if (profileUser != null && profileUser.getId() != userId) {
			throw new WrongUserProfileException(UserSessionData.localizeText(
					"exception.WrongUserProfile.exists", messageSource), request.getPathInfo(), messageSource);
		}
	}

	@RequestMapping(path = "/users", method = RequestMethod.GET)
	public String users(Model model) {
		model.addAttribute("_VIEW_TITLE", "users.management.title");
		model.addAttribute("_FORCE_CSRF", true);
		model.addAttribute("usersList", getUsersData(userDAO.getAll()));
		return "users";
	}

	private List<UserData> getUsersData(List<Users> users) {
		List<UserData> res = new ArrayList<>();
		Collections.sort(users);
		for (Users user : users) {
			res.add(new UserData(user));
			Long pid = user.getProfileId();
			if (profileDAO != null && pid != null && pid > 0) {
				Profile profile = profileDAO.getOne(pid);
				if (profile != null)
					res.get(res.size() - 1).setProfileName(profile.getPresentationName());
			}
		}
		return res;
	}

	@RequestMapping(path = { "/delete" }, method = RequestMethod.GET)
	public String profileDelete(Model model, @RequestParam(name = "userId", required = true) Long id) {
		Users user = userDAO.getByUserID(id);
		for (MessageCenter messageCenter : user.getInMessages()) {
			Message message = messageCenter.getMessage();
			message.removeRecipient(messageCenter);
			messageDAO.update(message);
		}
		user = userDAO.getByUserID(id);
		userDAO.deleteUser(user);
		return "redirect:/security/users";
	}

	private void initialize() {
		sites = new HashMap<>();
		urls = new ArrayList<>();
		oldUrls = new ArrayList<>();
		wordsMap = new HashMap<>();
		keys = new HashMap<>();
		corpus = "";
		keyWords = "";
		words.clear();
	}

	@RequestMapping(path = "/login", method = RequestMethod.GET)
	public String login(Model model,
			@RequestParam(name = "error", required = false) String error) throws Exception {
		model.addAttribute("_VIEW_TITLE", "title.login");
		if (error != null)
			error = UserSessionData.localizeText("exception.login", messageSource);
		else error = "";
		model.addAttribute("error", error);
		return "login";
	}
	
	private void clearDocDublicates() {
		oldUrls = new ArrayList<>();
		List<Term> toDelete = new ArrayList<>();
		Map<String, Term> termMap = new HashMap<>();
		for (Term term : termDAO.getAll())
			if (termMap.containsKey(term.getPref().toLowerCase())) {
				Term term2 = termMap.get(term.getPref().toLowerCase());
				if (term.getStem().length() > term2.getStem().length()) {
					toDelete.add(term);
				} else {
					toDelete.add(term2);
					termMap.put(term.getPref().toLowerCase(), term);
				}
			} else
				termMap.put(term.getPref().toLowerCase(), term);
		List<Keyword> toUpdate = new ArrayList<>();
		for (Keyword key : keyDAO.getAll()) {
			if (toDelete.contains(key.getTerm())) {
				Term newTerm = termMap.get(key.getTerm().getPref().toLowerCase());
				if (newTerm != null) {
					key.setTerm(newTerm);
					toUpdate.add(key);
				}
			}
		}
		if (!toUpdate.isEmpty())
			keyDAO.updateAll(toUpdate);
		termDAO.deleteAll(toDelete);
	}
	
	private Map<String, String> getPosts(String url, String postMask) {
		String initUrl = url;
		for (int i = 10; i < 20; i++) {
			if (i > 0) url = initUrl + "page/" + i + "/";
		Document doc = getDoc(url);
		if (doc == null) continue;
		// find posts
		Elements newsHeadlines = doc.select(postMask);
		//Map<String, String> sites = new HashMap<>();
		for (Element headline : newsHeadlines) {
			//System.out.print(String.format("\n%s\n\t%s", 
		    	//headline.attr("title"), headline.absUrl("href")));
			String curUrl = headline.absUrl("href");
			if (oldUrls.contains(curUrl) || urls.contains(curUrl)) continue;
			urls.add(curUrl);
			String title = headline.attr("title");
			sites.put(curUrl, title);
		}
		}
		return sites;
	}
	
	private Article getPosts2(String url, String articleMask) {
		Document curDoc = getDoc(url);
		if (curDoc == null) return null;
		
		Article newDoc = new Article();
		newDoc.setUrl(url);
		newDoc.setTitle(sites.get(url));
		String text = curDoc.select(articleMask).first().text();
		newDoc.setText(text);
		// get date
		//Elements times = curDoc.select("time");
		Elements times = curDoc.select("div[class=published-info]");
		Date minDate = null;
		SimpleDateFormat df = new SimpleDateFormat("MMMM d',' yyyy", Locale.ENGLISH);
		df.applyPattern("yyyy-MM-dd'T'hh:mm:ss");
		for (Element time : times) {
			String attr = time.attr("datetime");
			//String attr = time.text().replace("Published", "").replace("\n", "").trim();
			try {
				Date parse = df.parse(attr);
				if (minDate == null) minDate = parse;
				if (minDate.after(parse)) minDate = parse;
			} catch (ParseException e) {
				df.applyPattern("MMM'.' d',' yyyy");
				Date parse;
				try {
					parse = df.parse(attr);
					if (minDate == null) minDate = parse;
					if (minDate.after(parse)) minDate = parse;
				} catch (ParseException e1) {
					e1.printStackTrace();
				} finally {
					df.applyPattern("MMMM d',' yyyy");
				}
			}
		}
		if (minDate != null)
			newDoc.setCreationDate(minDate);
		
		//corpus += (corpus.isEmpty()?"":docsSep) + text;
		/*for (Element key : curDoc.select(articleMask + " em"))
			keyWords += String.format("%s\t", key.text());*/
		for (Element key : curDoc.select(articleMask + " strong"))
			keyWords += String.format("%s\t", key.text());
		
		keyWords += "\n";
		
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

	private String getUnstemmed(String stemmed) {
		if (wordsMap.containsKey(stemmed))
			stemmed = wordsMap.get(stemmed);
		else {
			String[] curWords = stemmed.split(" ");
			for (int ind = 0; ind < curWords.length; ind++)
				if (wordsMap.containsKey(curWords[ind])) {
					String fromReplace = curWords[ind], toReplace = wordsMap.get(curWords[ind]);
					if (ind > 0) { toReplace = " " + toReplace; fromReplace = " " + fromReplace; }
					if (ind < curWords.length - 1) { toReplace += " "; fromReplace += " "; }
					stemmed = stemmed.replace(fromReplace, toReplace);
				}
		}
		return stemmed;
	}
	
	private void tfIdf(String corpus) {
		String[] docs = prepareToTokenize(corpus).split(docsSep);
		// create vocabulary
		int wordsAmount, docsAmount = docs.length;
		//List<String> words = new ArrayList<>();
		List<Integer> docCount = new ArrayList<>();
		List<List<String>> tdocs = new ArrayList<>();
		
		PorterStemmer stemmer = new PorterStemmer();
		EnglishAnalyzer analyzer = new EnglishAnalyzer(Version.LUCENE_36);
		CharArraySet stopWords = (CharArraySet) analyzer.getStopwordSet();

		long curSec = System.currentTimeMillis();
		System.out.printf("\n---Start counting: %d", curSec - secs);
		for (int i = 0; i < docs.length; i++) {
			String[] curWords = docs[i].split(" ");
			List<String> tokens = new ArrayList<>();
			for (int j = 0; j < curWords.length; j++) {
				String word1 = curWords[j];
				if (stopWords.contains(word1.toLowerCase())) continue;
				word1 = stemTerm(word1, stemmer);
				tokens.add(word1);
				String word2 = "";
				if (j + 1 < curWords.length) {
					word2 = curWords[j + 1];
					if (!stopWords.contains(word2.toLowerCase())) {
						word2 = stemTerm(word2, stemmer);
						String newWord = word1 + " " + word2;
						tokens.add(newWord);
					}
				}
				if (j + 2 < curWords.length) {
					String word3 = curWords[j + 2];
					if (stopWords.contains(word3.toLowerCase())) continue;
					word3 = stemTerm(word3, stemmer);
					String newWord = word1 + " " +
							(stopWords.contains(word2.toLowerCase()) ? "" : word2 + " ") + word3;
					tokens.add(newWord);
				}
			}
			for (String token : tokens)
				if (!token.isEmpty())
					if (!words.contains(token)) {
						words.add(token);
						docCount.add(-1);
					} else {
						int index = words.indexOf(token);
						if (docCount.get(index) > 0)
							docCount.set(index, -docCount.get(index) - 1);
					}
			tdocs.add(tokens);
			for (int j = 0; j < docCount.size(); j++)
				if (docCount.get(j) < 0)
					docCount.set(j, -docCount.get(j));
		}
		wordsAmount = words.size();
		// count words in each doc
		List<List<Double>> //matTF = new ArrayList<>(), matIDF = new ArrayList<>(),
				matT = new ArrayList<>(), matD = new ArrayList<>(); 
				// matTFIDF = new ArrayList<>();
		List<String> kdocs = new ArrayList<>();
		String[] keySplit = keyWords.split("\n");
		for (int i = 0 ; i < tdocs.size(); i++) {
			if (i >= keySplit.length)
				kdocs.add("");
			else
				kdocs.add("\t" + prepareToTokenize(keySplit[i]).replace(" ", "\t") + "\t");
		}
		int docI = -1;
		int tali = words.indexOf("talent");
		for (List<String> tokens : tdocs) {
			docI++;
			List<Double> count = new ArrayList<>();
			for (int j = 0; j < words.size(); j++)
				count.add(0.0);
			for (String token : tokens) {
				if (token.isEmpty()) continue;
				int index = words.indexOf(token);
				if (index < 0) continue;
				String fullToken = wordsMap.containsKey(token)? fullToken = wordsMap.get(token) : token;
				count.set(index, count.get(index) + (kdocs.get(docI).contains("\t" + fullToken + "\t") ? 2 : 1));
			}
			//matrix.add(count);
			matTFIDF.add(new ArrayList<Double>(count));
		}
		//printMatrix(matrix, words, "count words");
		String output = "\n";
		//for (Integer c : docCount) output += String.format("%d\t", c);
		//System.out.print(output + "\n");
		curSec = System.currentTimeMillis();
		System.out.printf("\n---Removing tokens: %d", curSec - secs);
		// remove rear tokens
		List<Integer> rears = new ArrayList<>();
		for (int i = 0; i < matTFIDF.size(); i++)
			for (int j = 0; j < matTFIDF.get(i).size(); j++)
				if (matTFIDF.get(i).get(j) == 1.0 && docCount.get(j) == 1)
					rears.add(j);
		int mm = words.indexOf("select candidat");
		boolean is = rears.contains(mm);
		List<String> oldWords = new ArrayList<>(words);
		for (int j = rears.size() - 1; j >= 0; j--) {
			for (int i = 0; i < matTFIDF.size(); i++)
				matTFIDF.get(i).remove((int) rears.get(j));
			words.remove((int) rears.get(j));
			docCount.remove((int) rears.get(j));
		}
		for (List<Double> count : matTFIDF) {
			matTF.add(new ArrayList<Double>(count));
			matIDF.add(new ArrayList<Double>(count));
			matT.add(new ArrayList<Double>(count));
			matD.add(new ArrayList<Double>(count));
		}
		curSec = System.currentTimeMillis();
		System.out.printf("\n---TF-IDF: %d", curSec - secs);
		// calculate TF-IDF
		for (int i = 0; i < matTFIDF.size(); i++) {
			for (int j = 0; j < matTFIDF.get(i).size(); j++) {
				if (matTFIDF.get(i).get(j) == 0) continue;
				//matT.get(i).set(j, matTFIDF.get(i).get(j));
				matD.get(i).set(j, (double) docCount.get(j));
				double tf = matTFIDF.get(i).get(j) / (double) tdocs.get(i).size();
				//tf = Math.log(tf + 1);
				double idf = (double) tdocs.size() / ((double) docCount.get(j) + 1.0);
				idf = Math.log(idf);
				//matrix.get(i).set(j, tf * idf * idf);
				matTF.get(i).set(j, tf);
				matIDF.get(i).set(j, idf);
				matTFIDF.get(i).set(j, tf * idf);
			}
		}
		analyzer.close();
		// output
		//printMatrix(matrix, words, "TF-IDF-IDF");
		//printMatrix(matTF, words, "TF");
		//printMatrix(matIDF, words, "IDF");
		
		//get tops
		List<List<Double>> tops = new ArrayList<>();
		for (List<Double> line : matTFIDF) {
			if (line.size() <= 100) {
				tops.add(line);
				continue;
			}
			List<Double> sorted = new ArrayList<>(line);
			Collections.sort(sorted);
			tops.add(sorted.subList(sorted.size() - 100, sorted.size()));
		}
		curSec = System.currentTimeMillis();
		System.out.printf("\nFINISH! %d", curSec - secs);
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
		wordsMap.put(stemmed, value);
		return stemmed;
	}
	
	private String prepareToTokenize(String corpus) {
		// remove symbols and digits
		String[] noizeSymbs = {",", ";", ".", "[", "]", "\"", "{", "}", "/", "\\", "!", "?", "#", "$", "�", "�", "�",
				"�", "�", "(", ")", "<", ">", "- ", " -", "+", "%", "*", ":", "--", "�", "��", "&", "|", "~", "�"};
		for (int i = 0; i < noizeSymbs.length; i++)
			corpus = corpus.replace(noizeSymbs[i], " ");
		for (int i = 0; i < 10; i++)
			corpus = corpus.replace(i+"", "");
		while (corpus.indexOf("  ") >= 0)
			corpus = corpus.replace("  ", " ");
		return corpus;//.toLowerCase();
	}
	@RequestMapping(path = "/me")
	public String me(Model model, HttpServletRequest request, Principal principal) {
		model.addAttribute("_VIEW_TITLE", "title.user.information");
		model.addAttribute("residentsCount", profileDAO.count() + Viewer.generatedProfiles.size());
		model.addAttribute("requirementsCount", Viewer.generatedReqs.size());
		model.addAttribute("testConfig", new TestConfig());
		return "me";
	}

	@RequestMapping(path = { "/updateConfigs" }, method = RequestMethod.POST)
	public String updateConfigs(Model model, @ModelAttribute("testConfig") @Valid TestConfig testConfig) {
		testConfig.updateConfigs();
		return "redirect:/security/me";
	}
}
