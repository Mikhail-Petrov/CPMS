package com.cpms.facade;

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
import com.cpms.data.entities.VotingSession;
import com.cpms.data.entities.Website;
import com.cpms.operations.interfaces.*;

/**
 * Facade pattern implementation which encapsulates most system components.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public interface ICPMSFacade {
	
	public IDAO<Profile> getProfileDAO();
	
	public IDAO<Message> getMessageDAO();
	
	public IDAO<Language> getLanguageDAO();
	
	public IDAO<Topic> getTopicDAO();
	
	public IDAO<Skill> getSkillDAO();
	
	public IDAO<Task> getTaskDAO();
	
	public IDAO<Article> getDocumentDAO();
	
	public IDAO<Term> getTermDAO();
	
	public IDAO<Keyword> getKeywordDAO();
	
	public IDAO<VotingSession> getVotingSessionDAO();
	
	public IDAO<Motivation> getMotivationDAO();
	
	public IDAO<Reward> getRewardDAO();
	
	public IDAO<Category> getCategoryDAO();
	
	public IDAO<Trend> getTrendDAO();
	
	public IDAO<Website> getWebsiteDAO();
	
	public ISubprofiler getSubprofiler();
	
	public ITaskComparator getTaskComparator();
	
	public IProfileComparator getProfileComparator();
	
	public IProfileCompetencySearcher getProfileCompetencySearcher();
	
	public IProfileRanger getProfileRanger();
	
	public IPossibilityAggregator getPossibilityAggregator();
}
