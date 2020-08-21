package com.cpms.dao.implementations.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.implementations.jpa.repositories.system.TaskRepository;
import com.cpms.dao.implementations.jpa.repositories.system.TaskRequirementRepository;
import com.cpms.dao.interfaces.AbstractDAO;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IDAO;
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
public class JPATaskDAO extends AbstractDAO<Task> implements ICleanable {
	
	private TaskRepository taskRepo;
	private TaskRequirementRepository taskRequirementRepo;
	private EntityManager entityManager;
	
	@Autowired
	@Qualifier(value = "Task")
	public void setTaskRepo(TaskRepository taskRepo) {
		this.taskRepo = taskRepo;
	}

	@Autowired
	@Qualifier(value = "TaskRequirement")
	public void setTaskRequirementRepo(TaskRequirementRepository taskRequirementRepo) {
		this.taskRequirementRepo = taskRequirementRepo;
	}
	
	@PersistenceContext(unitName = "entityManagerFactory")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public List<Task> getAll() {
		return taskRepo.getAll();
	}

	@Override
	public Task update(Task newTask) {
		if (newTask == null || newTask.getRequirements() == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (!taskRepo.exists(newTask.getId())) {
			throw new DataAccessException("Cannot update, such task doesn't exist",
					null);
		}
		return persist(newTask, taskRepo);
	}

	@Override
	public Task insert(Task newTask) {
		if (newTask == null || newTask.getRequirements() == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (taskRepo.exists(newTask.getId())) {
			throw new DataAccessException("Cannot insert, such task already exists",
					null);
		}
		return persist(newTask, taskRepo);
	}

	@Override
	public void delete(Task oldTask) {
		if (oldTask == null) {
			throw new DataAccessException("Attempt to delete null.", null);
		}
		taskRepo.delete(oldTask);
	}
	
	@Override
	public void cleanAndReset() {
		taskRepo.deleteAll();
		taskRequirementRepo.deleteAll();
	}

	@Override
	public Task getOne(long id) {
		Task target = taskRepo.findOne(id);
		if (target != null) {
			Hibernate.initialize(target.getVariants());
			//Hibernate.initialize(target.getTrends());
			//Hibernate.initialize(target.getCategories());
		}
		return target;
	}

	@Override
	public long count() {
		return taskRepo.count();
	}

	@Override
	public List<Task> getRange(long from, long to) {
		return super.getPage(taskRepo, from, to);
	}

	@Override
	public List<Task> search(String request, Class<? extends Task> type) {
		return super.useSearch(request,
				entityManager,
				Task.class,
				"name", "name_RU");
	}

	@Override
	public List<Task> searchRange(String request, Class<? extends Task> type,
			int from, int to) {
		return super.useSearchRange(request,
				entityManager,
				Task.class,
				from, to,
				"name", "name_RU");
	}

	@Override
	public int searchCount(String request, Class<? extends Task> type) {
		return super.searchAndCount(request,
				entityManager,
				Task.class,
				"name", "name_RU");
	}

	@Override
	public void rebuildIndex() {
		super.rebuildIndex(entityManager, Task.class);
	}

}
