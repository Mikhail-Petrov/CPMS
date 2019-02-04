package com.cpms.dao.applications;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.implementations.jpa.repositories.applications.CompetencyApplicationsRepository;
import com.cpms.dao.interfaces.IApplicationsService;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.data.applications.CompetencyApplication;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.exceptions.DataAccessException;
import com.cpms.exceptions.DependentEntityNotFoundException;
import com.cpms.facade.ICPMSFacade;

/**
 * Service that manages applications and implements operations with it.
 * Data was supposed to be stored in the different database, that's why
 * entity relationships are handled manually.
 * 
 * @see IApplicationsService
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Service
@Configurable
@Transactional("applicationsTransactionManager")
public class ApplicationsService implements IApplicationsService, ICleanable {

	private CompetencyApplicationsRepository competencyApplicationsRepo;
	private ICPMSFacade facade;
	
	@Autowired
	@Qualifier("CompetencyApplication")
	public void setCompetencyApplicationsRepo(CompetencyApplicationsRepository competencyApplicationsRepo) {
		this.competencyApplicationsRepo = competencyApplicationsRepo;
	}

	@Autowired
	@Qualifier("facade")
	public void setFacade(ICPMSFacade facade) {
		this.facade = facade;
	}

	@Override
	public void suggestCompetency(CompetencyApplication application) {
		if (application == null ||
				retrieveDependantProfile(application.getOwnerId()) == null ||
				retrieveDependantSkill(application.getSkillId()) == null) {
			throw new DependentEntityNotFoundException(
					CompetencyApplication.class, 
					Profile.class,
					application.getId(),
					application.getSkillId(),
					null);
		}
		competencyApplicationsRepo.save(application);
	}
	
	@Override
	public void deleteSuggestedCompetency(long id) {
		CompetencyApplication application = retrieveSuggestedCompetencyById(id);
		fillInCompetencyApplication(application);
		competencyApplicationsRepo.delete(application);
	}
	
	@Override
	public List<CompetencyApplication> retrieveSuggestedCompetencies() {
		List<CompetencyApplication> result = competencyApplicationsRepo.findAll();
		for (CompetencyApplication application : result) {
			fillInCompetencyApplication(application);
		}
		return result;
	}
	
	@Override
	public List<CompetencyApplication> retrieveSuggestedCompetenciesOfUser(
			long ownerId) {
		List<CompetencyApplication> result = competencyApplicationsRepo
				.findByOwnerId(ownerId);
		for (CompetencyApplication application : result) {
			fillInCompetencyApplication(application);
		}
		return result;
	}
	
	/**
	 * Boilerplate reduction method that retrieves some properties of
	 * CompetencyApplication entity from DAO and sets this application with
	 * them.
	 * 
	 * @param CompetencyApplication which properties will be filled in
	 */
	private void fillInCompetencyApplication(CompetencyApplication application) {
		application.setOwner(retrieveDependantProfile(application.getOwnerId()));
		application.setSkill(retrieveDependantSkill(application.getSkillId()));
	}
	
	@Override
	public CompetencyApplication retrieveSuggestedCompetencyById(long id) {
		CompetencyApplication application = 
				competencyApplicationsRepo.findOne(id);
		if (application == null) {
			throw new DataAccessException("Competency application not found.");
		}
		fillInCompetencyApplication(application);
		return application;
	}

	@Override
	public CompetencyApplication retrieveDependantCompetency(long id) {
		return competencyApplicationsRepo.getOne(id);
	}
	
	@Override
	public Profile retrieveDependantProfile(long id) {
		return facade.getProfileDAO().getOne(id);
	}
	
	@Override
	public Skill retrieveDependantSkill(long id) {
		return facade.getSkillDAO().getOne(id);
	}
	
	@Override
	public void approveCompetencyApplication(long id) {
		CompetencyApplication application = competencyApplicationsRepo.findOne(id);
		if (application == null) {
			throw new DataAccessException("Competency application with id " + id
					+ "does not exist.");
		}
		fillInCompetencyApplication(application);
		if (application.getOwner() == null
				|| application.getSkill() == null) {
			throw new DataAccessException("Application does not have required data");
		}
		Competency competency = new Competency(application.getSkill(), 
				application.getLevel());
		competency.setOwner(application.getOwner());
		boolean update = false;
		for (Competency sourceCompetency : application.getOwner()
				.getCompetencies()) {
			if (sourceCompetency.duplicates(competency)) {
				competency.setOwner(null);
				sourceCompetency.setLevel(competency.getLevel());
				competency = sourceCompetency;
				update = true;
				break;
			}
		}
		if (!update) {
			competency.getOwner().addCompetency(competency);
		}
		facade.getProfileDAO().update(competency.getOwner());
		competencyApplicationsRepo.delete(application);
		
	}

	@Override
	public void cleanAndReset() {
		competencyApplicationsRepo.deleteAll();
	}
}
