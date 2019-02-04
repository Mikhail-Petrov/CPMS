package com.cpms.dao.interfaces;

import java.util.List;

import com.cpms.data.applications.CompetencyApplication;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;

/**
 * Interface for service operation competency and evidence applications.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public interface IApplicationsService {

	/**
	 * Creates a competency application.
	 * 
	 * @param application application to be saved
	 */
	public void suggestCompetency(CompetencyApplication application);
	
	/**
	 * Deletes suggested competency.
	 * 
	 * @param id id of competency application to be deleted
	 */
	public void deleteSuggestedCompetency(long id);
	
	/**
	 * Retrieve all suggested competencies.
	 * 
	 * @return list representing all saved competency applications
	 */
	public List<CompetencyApplication> retrieveSuggestedCompetencies();
	
	/**
	 * Retrieves all competencies suggested by a user.
	 * 
	 * @param ownerId id of a user
	 * @return list representing all competencies suggested by this user
	 */
	public List<CompetencyApplication> retrieveSuggestedCompetenciesOfUser(long ownerId);
	
	/**
	 * Retrieves a competency application by it's id.
	 * 
	 * @param id id of a competency application
	 * @return application requested
	 */
	public CompetencyApplication retrieveSuggestedCompetencyById(long id);
	
	/**
	 * Approves specified competency application along with all of it's evidence.
	 * <p>Specified application will be converted to a competency entity.
	 * If competency profile mentioned by it already has a competency with the 
	 * same skill, this will only update it. If competency with such skill
	 * does not exist, this competency will be added to the profile.
	 * 
	 * @param id id of competency to be approved
	 */
	public void approveCompetencyApplication(long id);
	
	/**
	 * Only retrieves an application without filling it's dependencies.
	 * 
	 * @param id id of an application to be retrieved
	 * @return request competency application
	 */
	public CompetencyApplication retrieveDependantCompetency(long id);
	
	/**
	 * Retrieves profile by it's id.
	 * <p>Simply redirects to Profile IDAO
	 * 
	 * @param id id of a profile
	 * @return requested profile
	 */
	public Profile retrieveDependantProfile(long id);
	
	/**
	 * Retrieves skill by it's id.
	 * <p>Simply redirects to Skill IDAO
	 * 
	 * @param id id of a skill
	 * @return requested skill
	 */
	public Skill retrieveDependantSkill(long id);
	
}
