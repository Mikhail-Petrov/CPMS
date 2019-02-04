package com.cpms.dao.implementations.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.implementations.jpa.repositories.system.CompetencyRepository;
import com.cpms.dao.implementations.jpa.repositories.system.MotivationRepository;
import com.cpms.dao.implementations.jpa.repositories.system.ProfileRepository;
import com.cpms.dao.interfaces.AbstractDAO;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.Company;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Motivation;
import com.cpms.data.entities.Person;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Task;
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
public class JPAMotivationDAO extends AbstractDAO<Motivation> implements ICleanable {
	
	private MotivationRepository motivationRepo;
	private EntityManager entityManager;
	
	
	@Autowired
	@Qualifier(value = "Motivation")
	public void setMotivationRepo(MotivationRepository motivationRepo) {
		this.motivationRepo = motivationRepo;
	}

	@PersistenceContext(unitName = "entityManagerFactory")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public long count() {
		return motivationRepo.count();
	}

	@Override
	public List<Motivation> getRange(long from, long to) {
		return super.getPage(motivationRepo, from, to);
	}

	@Override
	public Motivation getOne(long id) {
		Motivation target = motivationRepo.findOne(id);
		return target;
	}

	@Override
	public Motivation update(Motivation updateInstance) {
		if (updateInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (!motivationRepo.exists(updateInstance.getId())) {
			throw new DataAccessException("Cannot update, such profile doesn't exist",
					null);
		}
		return persist(updateInstance, motivationRepo);
	}

	@Override
	public Motivation insert(Motivation newInstance) {
		if (newInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (motivationRepo.exists(newInstance.getId())) {
			throw new DataAccessException("Cannot insert, such profile already exists",
					null);
		}
		return persist(newInstance, motivationRepo);
	}

	@Override
	public void delete(Motivation oldInstance) {
		if (oldInstance == null) {
			throw new DataAccessException("Attempt to delete null", null);
		}
		motivationRepo.delete(oldInstance);
	}

	@Override
	public void cleanAndReset() {
		motivationRepo.deleteAll();
	}

	@Override
	public List<Motivation> getAll() {
		return motivationRepo.findAll();
	}

	@Override
	public List<Motivation> search(String request, Class<? extends Motivation> type) {
		return super.useSearch(request,
				entityManager,
				Motivation.class,
				"name");
	}

	@Override
	public List<Motivation> searchRange(String request, Class<? extends Motivation> type,
			int from, int to) {
		return super.useSearchRange(request,
				entityManager,
				Motivation.class,
				from, to,
				"name");
	}

	@Override
	public int searchCount(String request, Class<? extends Motivation> type) {
		return super.searchAndCount(request,
				entityManager,
				Motivation.class,
				"name");
	}

	@Override
	public void rebuildIndex() {
		super.rebuildIndex(entityManager, Motivation.class);
	}

}
