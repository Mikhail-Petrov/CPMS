package com.cpms.dao.implementations.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.implementations.jpa.repositories.system.WebsiteRepository;
import com.cpms.dao.interfaces.AbstractDAO;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.Website;
import com.cpms.exceptions.DataAccessException;

/**
 * Implementation of {@link IDAO} interface for Website entity.
 * 
 * @see IDAO
 */
@Service
@Transactional("transactionManager")
public class JPAWebsiteDAO extends AbstractDAO<Website> implements ICleanable {
	
	private WebsiteRepository websiteRepo;
	private EntityManager entityManager;
	
	
	@Autowired
	@Qualifier(value = "Website")
	public void setWebsiteRepo(WebsiteRepository websiteRepo) {
		this.websiteRepo = websiteRepo;
	}

	@PersistenceContext(unitName = "entityManagerFactory")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public long count() {
		return websiteRepo.count();
	}

	@Override
	public List<Website> getRange(long from, long to) {
		return super.getPage(websiteRepo, from, to);
	}

	@Override
	public Website getOne(long id) {
		Website target = websiteRepo.findOne(id);
		return target;
	}

	@Override
	public Website update(Website updateInstance) {
		if (updateInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (!websiteRepo.exists(updateInstance.getId())) {
			throw new DataAccessException("Cannot update, such website doesn't exist",
					null);
		}
		return persist(updateInstance, websiteRepo);
	}

	@Override
	public Website insert(Website newInstance) {
		if (newInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (websiteRepo.exists(newInstance.getId())) {
			throw new DataAccessException("Cannot insert, such website already exists",
					null);
		}
		return persist(newInstance, websiteRepo);
	}

	@Override
	public void delete(Website oldInstance) {
		if (oldInstance == null) {
			throw new DataAccessException("Attempt to delete null", null);
		}
		websiteRepo.delete(oldInstance);
	}

	@Override
	public void cleanAndReset() {
		websiteRepo.deleteAll();
	}

	@Override
	public List<Website> getAll() {
		return websiteRepo.findAll();
	}

	@Override
	public List<Website> getChildren(Website parent) {
		if (parent == null)
			return websiteRepo.getRoots();
		return websiteRepo.getChildren(parent);
	}

	@Override
	public int getInt(Website parent) {
		Integer res;
		if (parent == null)
			res = websiteRepo.countRoots();
		else
			res = websiteRepo.countChildren(parent);
		return res == null ? 0 : res;
	}

	@Override
	public List<Website> search(String request, Class<? extends Website> type) {
		return super.useSearch(request,
				entityManager,
				Website.class,
				"name");
	}

	@Override
	public List<Website> searchRange(String request, Class<? extends Website> type,
			int from, int to) {
		return super.useSearchRange(request,
				entityManager,
				Website.class,
				from, to,
				"name");
	}

	@Override
	public int searchCount(String request, Class<? extends Website> type) {
		return super.searchAndCount(request,
				entityManager,
				Website.class,
				"name");
	}

	@Override
	public void rebuildIndex() {
		super.rebuildIndex(entityManager, Website.class);
	}

}
