package com.cpms.dao.implementations.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.implementations.jpa.repositories.system.RewardsRepository;
import com.cpms.dao.interfaces.AbstractDAO;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.Reward;
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
public class JPARewardsDAO extends AbstractDAO<Reward> implements ICleanable {
	
	private RewardsRepository rewardsRepo;
	private EntityManager entityManager;
	
	
	@Autowired
	@Qualifier(value = "Reward")
	public void setRewardRepo(RewardsRepository rewardsRepo) {
		this.rewardsRepo = rewardsRepo;
	}

	@PersistenceContext(unitName = "entityManagerFactory")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public long count() {
		return rewardsRepo.count();
	}

	@Override
	public List<Reward> getRange(long from, long to) {
		return super.getPage(rewardsRepo, from, to);
	}

	@Override
	public Reward getOne(long id) {
		Reward target = rewardsRepo.findOne(id);
		return target;
	}

	@Override
	public Reward update(Reward updateInstance) {
		if (updateInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (!rewardsRepo.exists(updateInstance.getId())) {
			throw new DataAccessException("Cannot update, such reward doesn't exist",
					null);
		}
		return persist(updateInstance, rewardsRepo);
	}

	@Override
	public Reward insert(Reward newInstance) {
		if (newInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (rewardsRepo.exists(newInstance.getId())) {
			throw new DataAccessException("Cannot insert, such reward already exists",
					null);
		}
		return persist(newInstance, rewardsRepo);
	}

	@Override
	public void delete(Reward oldInstance) {
		if (oldInstance == null) {
			throw new DataAccessException("Attempt to delete null", null);
		}
		rewardsRepo.delete(oldInstance);
	}

	@Override
	public void cleanAndReset() {
		rewardsRepo.deleteAll();
	}

	@Override
	public List<Reward> getAll() {
		return rewardsRepo.findAll();
	}

	@Override
	public List<Reward> search(String request, Class<? extends Reward> type) {
		return super.useSearch(request,
				entityManager,
				Reward.class,
				"name");
	}

	@Override
	public List<Reward> searchRange(String request, Class<? extends Reward> type,
			int from, int to) {
		return super.useSearchRange(request,
				entityManager,
				Reward.class,
				from, to,
				"name");
	}

	@Override
	public int searchCount(String request, Class<? extends Reward> type) {
		return super.searchAndCount(request,
				entityManager,
				Reward.class,
				"name");
	}

	@Override
	public void rebuildIndex() {
		super.rebuildIndex(entityManager, Reward.class);
	}

}
