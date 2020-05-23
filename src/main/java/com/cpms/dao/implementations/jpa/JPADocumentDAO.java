package com.cpms.dao.implementations.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.implementations.jpa.repositories.system.DocsRepository;
import com.cpms.dao.interfaces.AbstractDAO;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.Article;
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
public class JPADocumentDAO extends AbstractDAO<Article> implements ICleanable {
	
	private DocsRepository docsRepo;
	private EntityManager entityManager;
	
	
	@Autowired
	@Qualifier(value = "Document")
	public void setDocumentRepo(DocsRepository docsRepo) {
		this.docsRepo = docsRepo;
	}

	@PersistenceContext(unitName = "entityManagerFactory")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public long count() {
		return docsRepo.count();
	}

	@Override
	public List<Article> getRange(long from, long to) {
		return super.getPage(docsRepo, from, to);
	}

	@Override
	public Article getOne(long id) {
		Article target = docsRepo.findOne(id);
		return target;
	}

	@Override
	public Article update(Article updateInstance) {
		if (updateInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (!docsRepo.exists(updateInstance.getId())) {
			throw new DataAccessException("Cannot update, such profile doesn't exist",
					null);
		}
		return persist(updateInstance, docsRepo);
	}

	@Override
	public Article insert(Article newInstance) {
		if (newInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (docsRepo.exists(newInstance.getId())) {
			throw new DataAccessException("Cannot insert, such profile already exists",
					null);
		}
		return persist(newInstance, docsRepo);
	}
	
	@Override
	public List<Article> insertAll(List<Article> docs) {
		int limit = 900;
		int size = docs.size();
		if (size <= limit)
			return docsRepo.save(docs);
		List<Article> save = new ArrayList<>();
		int i;
		for (i = 0; i < size - limit; i += limit)
			save.addAll(docsRepo.save(docs.subList(i, i + limit)));
		if (i < size) {
			save.addAll(docsRepo.save(docs.subList(i, size)));
		}
		return save;
	}

	@Override
	public void delete(Article oldInstance) {
		if (oldInstance == null) {
			throw new DataAccessException("Attempt to delete null", null);
		}
		docsRepo.delete(oldInstance);
	}

	@Override
	public void deleteAll(List<Article> docs) {
		docsRepo.delete(docs);
	}

	@Override
	public void cleanAndReset() {
		docsRepo.deleteAll();
	}

	@Override
	public List<Article> getAll() {
		return docsRepo.findAll();
	}

	@Override
	public List<Article> search(String request, Class<? extends Article> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Article> searchRange(String request, Class<? extends Article> type, int from, int to) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int searchCount(String request, Class<? extends Article> type) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void rebuildIndex() {
		super.rebuildIndex(entityManager, Article.class);
	}
	
	@Override
	public int getInt(Article doc) {
		Integer ret = docsRepo.getSumCount(doc.getId());
		return ret == null ? 0 : ret;
	}

}
