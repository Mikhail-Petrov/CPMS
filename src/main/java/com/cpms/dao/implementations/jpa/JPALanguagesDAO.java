package com.cpms.dao.implementations.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.implementations.jpa.repositories.system.LanguagesRepository;
import com.cpms.dao.interfaces.AbstractDAO;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.Language;
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
public class JPALanguagesDAO extends AbstractDAO<Language> implements ICleanable {
	
	private LanguagesRepository languageRepo;
	private EntityManager entityManager;
	
	
	@Autowired
	@Qualifier(value = "Language")
	public void setMotivationRepo(LanguagesRepository languageRepo) {
		this.languageRepo = languageRepo;
	}

	@PersistenceContext(unitName = "entityManagerFactory")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public long count() {
		return languageRepo.count();
	}

	@Override
	public List<Language> getRange(long from, long to) {
		return super.getPage(languageRepo, from, to);
	}

	@Override
	public Language getOne(long id) {
		Language target = languageRepo.findOne(id);
		return target;
	}

	@Override
	public Language update(Language updateInstance) {
		if (updateInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (!languageRepo.exists(updateInstance.getId())) {
			throw new DataAccessException("Cannot update, such profile doesn't exist",
					null);
		}
		return persist(updateInstance, languageRepo);
	}

	@Override
	public Language insert(Language newInstance) {
		if (newInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (languageRepo.exists(newInstance.getId())) {
			throw new DataAccessException("Cannot insert, such profile already exists",
					null);
		}
		return persist(newInstance, languageRepo);
	}

	@Override
	public void delete(Language oldInstance) {
		if (oldInstance == null) {
			throw new DataAccessException("Attempt to delete null", null);
		}
		languageRepo.delete(oldInstance);
	}

	@Override
	public void cleanAndReset() {
		languageRepo.deleteAll();
	}

	@Override
	public List<Language> getAll() {
		return languageRepo.findAll();
	}

	@Override
	public List<Language> search(String request, Class<? extends Language> type) {
		return super.useSearch(request,
				entityManager,
				Language.class,
				"name");
	}

	@Override
	public List<Language> searchRange(String request, Class<? extends Language> type,
			int from, int to) {
		return super.useSearchRange(request,
				entityManager,
				Language.class,
				from, to,
				"name");
	}

	@Override
	public int searchCount(String request, Class<? extends Language> type) {
		return super.searchAndCount(request,
				entityManager,
				Language.class,
				"name");
	}

	@Override
	public void rebuildIndex() {
		super.rebuildIndex(entityManager, Language.class);
	}

}
