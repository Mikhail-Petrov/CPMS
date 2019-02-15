package com.cpms.dao.implementations.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.implementations.jpa.repositories.system.CompetencyRepository;
import com.cpms.dao.implementations.jpa.repositories.system.ProfileRepository;
import com.cpms.dao.interfaces.AbstractDAO;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.Profile;
import com.cpms.exceptions.DataAccessException;

/**
 * Implementation of {@link IDAO} interface for Profile entity.
 * 
 * @see IDAO
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Service
@Transactional("transactionManager")
public class JPAProfileDAO extends AbstractDAO<Profile> implements ICleanable {

	private ProfileRepository profilesRepo;
	private CompetencyRepository competencyRepo;
	private EntityManager entityManager;

	@Autowired
	@Qualifier(value = "Profile")
	public void setProfilesRepo(ProfileRepository profilesRepo) {
		this.profilesRepo = profilesRepo;
	}

	@Autowired
	@Qualifier(value = "Competency")
	public void setCompetencyRepo(CompetencyRepository competencyRepo) {
		this.competencyRepo = competencyRepo;
	}

	@PersistenceContext(unitName = "entityManagerFactory")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public long count() {
		return profilesRepo.count();
	}

	@Override
	public List<Profile> getRange(long from, long to) {
		return super.getPage(profilesRepo, from, to);
	}

	@Override
	public Profile getOne(long id) {
		Profile target = profilesRepo.getOne(id);
		target.getId();
		return target;
	}

	@Override
	public Profile update(Profile updateInstance) {
		if (updateInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (!profilesRepo.exists(updateInstance.getId())) {
			throw new DataAccessException("Cannot update, such profile doesn't exist", null);
		}
		return persist(updateInstance, profilesRepo);
	}

	@Override
	public Profile insert(Profile newInstance) {
		if (newInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (profilesRepo.exists(newInstance.getId())) {
			throw new DataAccessException("Cannot insert, such profile already exists", null);
		}
		return persist(newInstance, profilesRepo);
	}

	@Override
	public void delete(Profile oldInstance) {
		if (oldInstance == null) {
			throw new DataAccessException("Attempt to delete null", null);
		}
		profilesRepo.delete(oldInstance);
	}

	@Override
	public void cleanAndReset() {
		profilesRepo.deleteAll();
		competencyRepo.deleteAll();
	}

	@Override
	public List<Profile> getAll() {
		return profilesRepo.findAll();
		// TODO data streaming
		// http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-streaming
	}

	@Override
	public List<Profile> search(String request, Class<? extends Profile> type) {
		return super.useSearch(request, entityManager, Profile.class, "name");
	}

	@Override
	public List<Profile> searchRange(String request, Class<? extends Profile> type, int from, int to) {
		return super.useSearchRange(request, entityManager, Profile.class, from, to, "name");
	}

	@Override
	public int searchCount(String request, Class<? extends Profile> type) {
		return super.searchAndCount(request, entityManager, Profile.class, "name");
	}

	@Override
	public void rebuildIndex() {
		super.rebuildIndex(entityManager, Profile.class);
	}

}
