package com.cpms.dao.implementations.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.implementations.jpa.repositories.system.TopicRepository;
import com.cpms.dao.interfaces.AbstractDAO;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.Topic;
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
public class JPATopicDAO extends AbstractDAO<Topic> implements ICleanable {
	
	private TopicRepository topicRepo;
	private EntityManager entityManager;
	
	
	@Autowired
	@Qualifier(value = "Topic")
	public void setTopicRepo(TopicRepository topicRepo) {
		this.topicRepo = topicRepo;
	}

	@PersistenceContext(unitName = "entityManagerFactory")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public long count() {
		return topicRepo.count();
	}

	@Override
	public List<Topic> getRange(long from, long to) {
		return super.getPage(topicRepo, from, to);
	}

	@Override
	public Topic getOne(long id) {
		Topic target = topicRepo.findOne(id);
		return target;
	}

	@Override
	public Topic update(Topic updateInstance) {
		if (updateInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (!topicRepo.exists(updateInstance.getId())) {
			throw new DataAccessException("Cannot update, such profile doesn't exist",
					null);
		}
		return persist(updateInstance, topicRepo);
	}

	@Override
	public Topic insert(Topic newInstance) {
		if (newInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (topicRepo.exists(newInstance.getId())) {
			throw new DataAccessException("Cannot insert, such profile already exists",
					null);
		}
		return persist(newInstance, topicRepo);
	}

	@Override
	public void delete(Topic oldInstance) {
		if (oldInstance == null) {
			throw new DataAccessException("Attempt to delete null", null);
		}
		topicRepo.delete(oldInstance);
	}

	@Override
	public void cleanAndReset() {
		topicRepo.deleteAll();
	}

	@Override
	public List<Topic> getAll() {
		return topicRepo.findAll();
	}

	@Override
	public List<Topic> search(String request, Class<? extends Topic> type) {
		return super.useSearch(request,
				entityManager,
				Topic.class,
				"name");
	}

	@Override
	public List<Topic> searchRange(String request, Class<? extends Topic> type,
			int from, int to) {
		return super.useSearchRange(request,
				entityManager,
				Topic.class,
				from, to,
				"name");
	}

	@Override
	public int searchCount(String request, Class<? extends Topic> type) {
		return super.searchAndCount(request,
				entityManager,
				Topic.class,
				"name");
	}

	@Override
	public void rebuildIndex() {
		super.rebuildIndex(entityManager, Topic.class);
	}

}
