package com.cpms.dao.implementations.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.implementations.jpa.repositories.system.CategoryRepository;
import com.cpms.dao.interfaces.AbstractDAO;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.Category;
import com.cpms.data.entities.Skill;
import com.cpms.exceptions.DataAccessException;

/**
 * Implementation of {@link IDAO} interface for Category entity.
 * 
 * @see IDAO
 */
@Service
@Transactional("transactionManager")
public class JPACategoryDAO extends AbstractDAO<Category> implements ICleanable {
	
	private CategoryRepository categoryRepo;
	private EntityManager entityManager;
	
	
	@Autowired
	@Qualifier(value = "Category")
	public void setCategoryRepo(CategoryRepository categoryRepo) {
		this.categoryRepo = categoryRepo;
	}

	@PersistenceContext(unitName = "entityManagerFactory")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public long count() {
		return categoryRepo.count();
	}

	@Override
	public List<Category> getRange(long from, long to) {
		return super.getPage(categoryRepo, from, to);
	}

	@Override
	public Category getOne(long id) {
		Category target = categoryRepo.findOne(id);
		return target;
	}

	@Override
	public Category update(Category updateInstance) {
		if (updateInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (!categoryRepo.exists(updateInstance.getId())) {
			throw new DataAccessException("Cannot update, such category doesn't exist",
					null);
		}
		return persist(updateInstance, categoryRepo);
	}

	@Override
	public Category insert(Category newInstance) {
		if (newInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (categoryRepo.exists(newInstance.getId())) {
			throw new DataAccessException("Cannot insert, such category already exists",
					null);
		}
		return persist(newInstance, categoryRepo);
	}

	@Override
	public void delete(Category oldInstance) {
		if (oldInstance == null) {
			throw new DataAccessException("Attempt to delete null", null);
		}
		categoryRepo.delete(oldInstance);
	}

	@Override
	public void cleanAndReset() {
		categoryRepo.deleteAll();
	}

	@Override
	public List<Category> getAll() {
		return categoryRepo.findAll();
	}

	@Override
	public List<Category> getChildren(Category parent) {
		if (parent == null)
			return categoryRepo.getRoots();
		return categoryRepo.getChildren(parent);
	}

	@Override
	public int getInt(Category parent) {
		Integer res;
		if (parent == null)
			res = categoryRepo.countRoots();
		else
			res = categoryRepo.countChildren(parent);
		return res == null ? 0 : res;
	}

	@Override
	public List<Category> search(String request, Class<? extends Category> type) {
		return super.useSearch(request,
				entityManager,
				Category.class,
				"name");
	}

	@Override
	public List<Category> searchRange(String request, Class<? extends Category> type,
			int from, int to) {
		return super.useSearchRange(request,
				entityManager,
				Category.class,
				from, to,
				"name");
	}

	@Override
	public int searchCount(String request, Class<? extends Category> type) {
		return super.searchAndCount(request,
				entityManager,
				Category.class,
				"name");
	}

	@Override
	public void rebuildIndex() {
		super.rebuildIndex(entityManager, Category.class);
	}

}
