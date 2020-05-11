package com.cpms.dao.implementations.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.implementations.jpa.repositories.system.WordsRepository;
import com.cpms.dao.interfaces.AbstractDAO;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.Keyword;
import com.cpms.data.entities.Term;
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
public class JPAKeywordDAO extends AbstractDAO<Keyword> implements ICleanable {
	
	private WordsRepository keywordRepo;
	private EntityManager entityManager;
	
	
	@Autowired
	@Qualifier(value = "Keyword")
	public void setKeywordRepo(WordsRepository keyRepo) {
		this.keywordRepo = keyRepo;
	}

	@PersistenceContext(unitName = "entityManagerFactory")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public long count() {
		return keywordRepo.count();
	}

	@Override
	public List<Keyword> getRange(long from, long to) {
		return super.getPage(keywordRepo, from, to);
	}

	@Override
	public Keyword getOne(long id) {
		Keyword target = keywordRepo.findOne(id);
		return target;
	}

	@Override
	public Keyword update(Keyword updateInstance) {
		if (updateInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (!keywordRepo.exists(updateInstance.getId())) {
			throw new DataAccessException("Cannot update, such profile doesn't exist",
					null);
		}
		return persist(updateInstance, keywordRepo);
	}
	
	@Override
	public List<Keyword> updateAll(List<Keyword> keywords) {
		return keywordRepo.save(keywords);
	}

	@Override
	public Keyword insert(Keyword newInstance) {
		if (newInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (keywordRepo.exists(newInstance.getId())) {
			throw new DataAccessException("Cannot insert, such profile already exists",
					null);
		}
		return persist(newInstance, keywordRepo);
	}
	
	@Override
	public List<Keyword> insertAll(List<Keyword> keywords) {
		int limit = 900;
		int size = keywords.size();
		if (size <= limit)
			return keywordRepo.save(keywords);
		List<Keyword> save = new ArrayList<>();
		int i;
		for (i = 0; i < size - limit; i += limit)
			save.addAll(keywordRepo.save(keywords.subList(i, i + limit)));
		if (i < size) {
			save.addAll(keywordRepo.save(keywords.subList(i, size)));
		}
		return save;
	}

	@Override
	public void delete(Keyword oldInstance) {
		if (oldInstance == null) {
			throw new DataAccessException("Attempt to delete null", null);
		}
		keywordRepo.delete(oldInstance);
	}

	@Override
	public void cleanAndReset() {
		keywordRepo.deleteAll();
	}

	@Override
	public List<Keyword> getAll() {
		return keywordRepo.findAll();
	}

	@Override
	public List<Keyword> search(String request, Class<? extends Keyword> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Keyword> searchRange(String request, Class<? extends Keyword> type, int from, int to) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int searchCount(String request, Class<? extends Keyword> type) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void rebuildIndex() {
		super.rebuildIndex(entityManager, Keyword.class);
	}

}
