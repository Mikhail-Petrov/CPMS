package com.cpms.dao.implementations.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.implementations.jpa.repositories.system.CategoryRepository;
import com.cpms.dao.implementations.jpa.repositories.system.TrendRepository;
import com.cpms.dao.interfaces.AbstractDAO;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.Category;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.Trend;
import com.cpms.exceptions.DataAccessException;

/**
 * Implementation of {@link IDAO} interface for Category entity.
 * 
 * @see IDAO
 */
@Service
@Transactional("transactionManager")
public class JPATrendDAO extends AbstractDAO<Trend> implements ICleanable {
	
	private TrendRepository trendRepo;
	private EntityManager entityManager;
	
	
	@Autowired
	@Qualifier(value = "Trend")
	public void setTrendRepo(TrendRepository categoryRepo) {
		this.trendRepo = categoryRepo;
	}

	@PersistenceContext(unitName = "entityManagerFactory")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public long count() {
		return trendRepo.count();
	}

	@Override
	public List<Trend> getRange(long from, long to) {
		return super.getPage(trendRepo, from, to);
	}

	@Override
	public Trend getOne(long id) {
		Trend target = trendRepo.findOne(id);
		return target;
	}

	@Override
	public Trend update(Trend updateInstance) {
		if (updateInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (!trendRepo.exists(updateInstance.getId())) {
			throw new DataAccessException("Cannot update, such category doesn't exist",
					null);
		}
		return persist(updateInstance, trendRepo);
	}

	@Override
	public Trend insert(Trend newInstance) {
		if (newInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (trendRepo.exists(newInstance.getId())) {
			throw new DataAccessException("Cannot insert, such trend already exists",
					null);
		}
		return persist(newInstance, trendRepo);
	}

	@Override
	public void delete(Trend oldInstance) {
		if (oldInstance == null) {
			throw new DataAccessException("Attempt to delete null", null);
		}
		trendRepo.delete(oldInstance);
	}

	@Override
	public void cleanAndReset() {
		trendRepo.deleteAll();
	}

	@Override
	public List<Trend> getAll() {
		return trendRepo.findAll();
	}

	@Override
	public List<Trend> getChildren(Trend parent) {
		if (parent == null)
			return trendRepo.getRoots();
		return trendRepo.getChildren(parent);
	}

	@Override
	public int getInt(Trend parent) {
		Integer res;
		if (parent == null)
			res = trendRepo.countRoots();
		else
			res = trendRepo.countChildren(parent);
		return res == null ? 0 : res;
	}

	@Override
	public List<Trend> search(String request, Class<? extends Trend> type) {
		return super.useSearch(request,
				entityManager,
				Trend.class,
				"name");
	}

	@Override
	public List<Trend> searchRange(String request, Class<? extends Trend> type,
			int from, int to) {
		return super.useSearchRange(request,
				entityManager,
				Trend.class,
				from, to,
				"name");
	}

	@Override
	public int searchCount(String request, Class<? extends Trend> type) {
		return super.searchAndCount(request,
				entityManager,
				Trend.class,
				"name");
	}

	@Override
	public void rebuildIndex() {
		super.rebuildIndex(entityManager, Trend.class);
	}

}
