package com.cpms.facade;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Assert;

import com.cpms.dao.interfaces.*;
import com.cpms.data.entities.Article;
import com.cpms.data.entities.Category;
import com.cpms.data.entities.Keyword;
import com.cpms.data.entities.Language;
import com.cpms.data.entities.Message;
import com.cpms.data.entities.Motivation;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Reward;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.Term;
import com.cpms.data.entities.Topic;
import com.cpms.data.entities.Trend;
import com.cpms.data.entities.Website;
import com.cpms.operations.interfaces.*;


@Configurable
public class BasicFacade implements ICPMSFacade, InitializingBean {
	
	@Autowired
	@Qualifier(value = "profileDAO")
	private IDAO<Profile> profileDAO;
	
	@Autowired
	@Qualifier(value = "skillDAO")
	private IDAO<Skill> skillDAO;
	
	@Autowired
	@Qualifier(value = "taskDAO")
	private IDAO<Task> taskDAO;
	
	@Autowired
	@Qualifier(value = "termDAO")
	private IDAO<Term> termDAO;
	
	@Autowired
	@Qualifier(value = "keywordDAO")
	private IDAO<Keyword> keywordDAO;
	
	@Autowired
	@Qualifier(value = "docDAO")
	private IDAO<Article> docDAO;
	
	@Autowired
	@Qualifier(value = "motivationDAO")
	private IDAO<Motivation> motivationDAO;
	
	@Autowired
	@Qualifier(value = "rewardsDAO")
	private IDAO<Reward> rewardsDAO;
	
	@Autowired
	@Qualifier(value = "messageDAO")
	private IDAO<Message> messageDAO;
	
	@Autowired
	@Qualifier(value = "languageDAO")
	private IDAO<Language> languageDAO;
	
	@Autowired
	@Qualifier(value = "topicDAO")
	private IDAO<Topic> topicDAO;
	
	@Autowired
	@Qualifier(value = "categoryDAO")
	private IDAO<Category> categoryDAO;
	
	@Autowired
	@Qualifier(value = "trendDAO")
	private IDAO<Trend> trendDAO;
	
	@Autowired
	@Qualifier(value = "websiteDAO")
	private IDAO<Website> websiteDAO;
	
	@Autowired
	@Qualifier(value = "subprofiler")
	private ISubprofiler subprofiler;
	
	@Autowired
	@Qualifier(value = "taskComparator")
	private ITaskComparator taskComparator;
	
	@Autowired
	@Qualifier(value = "profileComparator")
	private IProfileComparator profileComparator;
	
	@Autowired
	@Qualifier(value = "profileCompetencySearcher")
	private IProfileCompetencySearcher profileCompetencySearcher;
	
	@Autowired
	@Qualifier(value = "profileRanger")
	private IProfileRanger profileRanger;
	
	@Autowired
	@Qualifier(value = "possibilityAggregator")
	private IPossibilityAggregator possibilityAggregator;

	public void setMessageDAO(IDAO<Message> messageDAO) {
		this.messageDAO = messageDAO;
	}

	public void setMotivationDAO(IDAO<Motivation> motivationDAO) {
		this.motivationDAO = motivationDAO;
	}

	public void setRewardsDAO(IDAO<Reward> rewardsDAO) {
		this.rewardsDAO = rewardsDAO;
	}

	public void setProfileDAO(IDAO<Profile> profileDAO) {
		this.profileDAO = profileDAO;
	}

	public void setSkillDAO(IDAO<Skill> skillDAO) {
		this.skillDAO = skillDAO;
	}

	public void setTaskDAO(IDAO<Task> taskDAO) {
		this.taskDAO = taskDAO;
	}

	public void setCategoryDAO(IDAO<Category> categoryDAO) {
		this.categoryDAO = categoryDAO;
	}

	public void setTrendDAO(IDAO<Trend> trendDAO) {
		this.trendDAO = trendDAO;
	}

	public void setWebsiteDAO(IDAO<Website> websiteDAO) {
		this.websiteDAO = websiteDAO;
	}

	public void setTermDAO(IDAO<Term> termDAO) {
		this.termDAO = termDAO;
	}

	public void setDocumentDAO(IDAO<Article> documentDAO) {
		this.docDAO = documentDAO;
	}

	public void setKeywordDAO(IDAO<Keyword> keywordDAO) {
		this.keywordDAO = keywordDAO;
	}
	
	public void setSubprofiler(ISubprofiler subprofiler) {
		this.subprofiler = subprofiler;
	}

	public void setTaskComparator(ITaskComparator taskComparator) {
		this.taskComparator = taskComparator;
	}

	public void setProfileComparator(IProfileComparator profileComparator) {
		this.profileComparator = profileComparator;
	}

	public void setProfileCompetencySearcher(IProfileCompetencySearcher profileCompetencySearcher) {
		this.profileCompetencySearcher = profileCompetencySearcher;
	}

	public void setProfileRanger(IProfileRanger profileRanger) {
		this.profileRanger = profileRanger;
	}

	public void setPossibilityAggregator(IPossibilityAggregator possibilityAggregator) {
		this.possibilityAggregator = possibilityAggregator;
	}

	@Override
	public IDAO<Message> getMessageDAO() {
		return messageDAO;
	}

	@Override
	public IDAO<Profile> getProfileDAO() {
		return profileDAO;
	}

	@Override
	public IDAO<Skill> getSkillDAO() {
		return skillDAO;
	}

	@Override
	public IDAO<Task> getTaskDAO() {
		return taskDAO;
	}

	@Override
	public IDAO<Term> getTermDAO() {
		return termDAO;
	}

	@Override
	public IDAO<Category> getCategoryDAO() {
		return categoryDAO;
	}

	@Override
	public IDAO<Trend> getTrendDAO() {
		return trendDAO;
	}

	@Override
	public IDAO<Website> getWebsiteDAO() {
		return websiteDAO;
	}

	@Override
	public IDAO<Keyword> getKeywordDAO() {
		return keywordDAO;
	}

	@Override
	public IDAO<Article> getDocumentDAO() {
		return docDAO;
	}

	@Override
	public IDAO<Motivation> getMotivationDAO() {
		return motivationDAO;
	}

	@Override
	public IDAO<Reward> getRewardDAO() {
		return rewardsDAO;
	}

	@Override
	public IDAO<Language> getLanguageDAO() {
		return languageDAO;
	}

	@Override
	public IDAO<Topic> getTopicDAO() {
		return topicDAO;
	}

	@Override
	public ISubprofiler getSubprofiler() {
		return subprofiler;
	}
	
	@Override
	public ITaskComparator getTaskComparator() {
		return taskComparator;
	}

	@Override
	public IProfileComparator getProfileComparator() {
		return profileComparator;
	}

	@Override
	public IProfileCompetencySearcher getProfileCompetencySearcher() {
		return profileCompetencySearcher;
	}

	@Override
	public IProfileRanger getProfileRanger() {
		return profileRanger;
	}

	@Override
	public IPossibilityAggregator getPossibilityAggregator() {
		return possibilityAggregator;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(profileDAO, "Facade must be configured with data objects.");
		Assert.notNull(skillDAO, "Facade must be configured with data objects.");
		Assert.notNull(taskDAO, "Facade must be configured with data objects.");
		Assert.notNull(subprofiler, "Facade must be configured with operations.");
		Assert.notNull(taskComparator, "Facade must be configured with operations.");
		Assert.notNull(profileComparator, "Facade must be configured with operations.");
		Assert.notNull(profileCompetencySearcher, "Facade must be configured with operations.");
		Assert.notNull(profileRanger, "Facade must be configured with operations.");
		Assert.notNull(possibilityAggregator, "Facade must be configured with operations.");
	}
}
