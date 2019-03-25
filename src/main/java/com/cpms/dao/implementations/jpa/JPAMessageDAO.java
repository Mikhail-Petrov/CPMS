package com.cpms.dao.implementations.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.implementations.jpa.repositories.system.MessageCenterRepository;
import com.cpms.dao.implementations.jpa.repositories.system.MessageRepository;
import com.cpms.dao.interfaces.AbstractDAO;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.Message;
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
public class JPAMessageDAO extends AbstractDAO<Message> implements ICleanable {
	
	private MessageRepository messageRepo;
	private MessageCenterRepository messageCenterRepo;
	private EntityManager entityManager;
	
	
	@Autowired
	@Qualifier(value = "Message")
	public void setMessageRepo(MessageRepository messageRepo) {
		this.messageRepo = messageRepo;
	}

	@Autowired
	@Qualifier(value = "MessageCenter")
	public void setMessageCenterRepo(MessageCenterRepository messageCenterRepo) {
		this.messageCenterRepo = messageCenterRepo;
	}
	
	@PersistenceContext(unitName = "entityManagerFactory")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public long count() {
		return messageRepo.count();
	}

	@Override
	public List<Message> getRange(long from, long to) {
		return super.getPage(messageRepo, from, to);
	}

	@Override
	public Message getOne(long id) {
		Message target = messageRepo.findOne(id);
		return target;
	}

	@Override
	public Message update(Message updateInstance) {
		if (updateInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (!messageRepo.exists(updateInstance.getId())) {
			throw new DataAccessException("Cannot update, such profile doesn't exist",
					null);
		}
		return persist(updateInstance, messageRepo);
	}

	@Override
	public Message insert(Message newInstance) {
		if (newInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (messageRepo.exists(newInstance.getId())) {
			throw new DataAccessException("Cannot insert, such profile already exists",
					null);
		}
		return persist(newInstance, messageRepo);
	}

	@Override
	public void delete(Message oldInstance) {
		if (oldInstance == null) {
			throw new DataAccessException("Attempt to delete null", null);
		}
		messageRepo.delete(oldInstance);
	}

	@Override
	public void cleanAndReset() {
		messageRepo.deleteAll();
		messageCenterRepo.deleteAll();
	}

	@Override
	public List<Message> getAll() {
		return messageRepo.findAll();
	}

	@Override
	public List<Message> search(String request, Class<? extends Message> type) {
		return super.useSearch(request,
				entityManager,
				Message.class,
				"name");
	}

	@Override
	public List<Message> searchRange(String request, Class<? extends Message> type,
			int from, int to) {
		return super.useSearchRange(request,
				entityManager,
				Message.class,
				from, to,
				"name");
	}

	@Override
	public int searchCount(String request, Class<? extends Message> type) {
		return super.searchAndCount(request,
				entityManager,
				Message.class,
				"name");
	}

	@Override
	public void rebuildIndex() {
		super.rebuildIndex(entityManager, Message.class);
	}

}
