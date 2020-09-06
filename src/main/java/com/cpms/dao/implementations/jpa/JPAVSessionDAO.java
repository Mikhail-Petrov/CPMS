package com.cpms.dao.implementations.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.implementations.jpa.repositories.system.VotingSessionsRepository;
import com.cpms.dao.interfaces.AbstractDAO;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.VotingSession;
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
public class JPAVSessionDAO extends AbstractDAO<VotingSession> implements ICleanable {
	
	private VotingSessionsRepository votingSessionRepo;
	private EntityManager entityManager;
	
	
	@Autowired
	@Qualifier(value = "VotingSession")
	public void setVotingSessionRepo(VotingSessionsRepository sesRepo) {
		this.votingSessionRepo = sesRepo;
	}

	@PersistenceContext(unitName = "entityManagerFactory")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public long count() {
		return votingSessionRepo.count();
	}

	@Override
	public List<VotingSession> getRange(long from, long to) {
		return super.getPage(votingSessionRepo, from, to);
	}

	@Override
	public VotingSession getOne(long id) {
		VotingSession target = votingSessionRepo.findOne(id);
		if (target != null)
			Hibernate.initialize(target.getInnovations());
		return target;
	}

	@Override
	public VotingSession update(VotingSession updateInstance) {
		if (updateInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (!votingSessionRepo.exists(updateInstance.getId())) {
			throw new DataAccessException("Cannot update, such profile doesn't exist",
					null);
		}
		return persist(updateInstance, votingSessionRepo);
	}
	
	@Override
	public List<VotingSession> updateAll(List<VotingSession> sessions) {
		return votingSessionRepo.save(sessions);
	}

	@Override
	public VotingSession insert(VotingSession newInstance) {
		if (newInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (votingSessionRepo.exists(newInstance.getId())) {
			throw new DataAccessException("Cannot insert, such session already exists",
					null);
		}
		return persist(newInstance, votingSessionRepo);
	}
	
	@Override
	public List<VotingSession> insertAll(List<VotingSession> sessions) {
		int limit = 900;
		int size = sessions.size();
		if (size <= limit)
			return votingSessionRepo.save(sessions);
		List<VotingSession> save = new ArrayList<>();
		int i;
		for (i = 0; i < size - limit; i += limit)
			save.addAll(votingSessionRepo.save(sessions.subList(i, i + limit)));
		if (i < size) {
			save.addAll(votingSessionRepo.save(sessions.subList(i, size)));
		}
		return save;
	}

	@Override
	public void delete(VotingSession oldInstance) {
		if (oldInstance == null) {
			throw new DataAccessException("Attempt to delete null", null);
		}
		votingSessionRepo.delete(oldInstance);
	}

	@Override
	public void cleanAndReset() {
		votingSessionRepo.deleteAll();
	}

	@Override
	public List<VotingSession> getAll() {
		return votingSessionRepo.findAll();
	}

	@Override
	public List<VotingSession> search(String request, Class<? extends VotingSession> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<VotingSession> searchRange(String request, Class<? extends VotingSession> type, int from, int to) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int searchCount(String request, Class<? extends VotingSession> type) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void rebuildIndex() {
		super.rebuildIndex(entityManager, VotingSession.class);
	}

	@Override
	public List<Long> getIDs() {
		return votingSessionRepo.getLastSession(new Date(System.currentTimeMillis()));
	}

}
