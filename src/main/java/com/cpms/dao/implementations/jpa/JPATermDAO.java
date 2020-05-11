package com.cpms.dao.implementations.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.implementations.jpa.repositories.system.TermVariantRepository;
import com.cpms.dao.implementations.jpa.repositories.system.TermsRepository;
import com.cpms.dao.interfaces.AbstractDAO;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IDAO;
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
public class JPATermDAO extends AbstractDAO<Term> implements ICleanable {
	
	private TermsRepository termRepo;
	private EntityManager entityManager;
	private TermVariantRepository variantRepo;
	

	@Autowired
	@Qualifier(value = "Term")
	public void setTermRepo(TermsRepository termRepo) {
		this.termRepo = termRepo;
	}
	
	@Autowired
	@Qualifier(value = "Termvariant")
	public void setVariantRepo(TermVariantRepository variantRepo) {
		this.variantRepo = variantRepo;
	}

	@PersistenceContext(unitName = "entityManagerFactory")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public long count() {
		return termRepo.count();
	}

	@Override
	public List<Term> getRange(long from, long to) {
		return super.getPage(termRepo, from, to);
	}

	@Override
	public Term getOne(long id) {
		Term target = termRepo.findOne(id);
		return target;
	}

	@Override
	public Term update(Term updateInstance) {
		if (updateInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (!termRepo.exists(updateInstance.getId())) {
			throw new DataAccessException("Cannot update, such profile doesn't exist",
					null);
		}
		return persist(updateInstance, termRepo);
	}
	
	@Override
	public List<Term> updateAll(List<Term> terms) {
		return termRepo.save(terms);
	}

	@Override
	public Term insert(Term newInstance) {
		if (newInstance == null) {
			throw new DataAccessException("Attempt to insert null", null);
		}
		if (termRepo.exists(newInstance.getId())) {
			throw new DataAccessException("Cannot insert, such profile already exists",
					null);
		}
		return persist(newInstance, termRepo);
	}
	
	@Override
	public List<Term> insertAll(List<Term> terms) {
		int limit = 900;
		int size = terms.size();
		if (size <= limit)
			return termRepo.save(terms);
		List<Term> save = new ArrayList<>();
		int i;
		for (i = 0; i < size - limit; i += limit)
			save.addAll(termRepo.save(terms.subList(i, i + limit)));
		if (i < size) {
			save.addAll(termRepo.save(terms.subList(i, size)));
		}
		return save;
	}

	@Override
	public void delete(Term oldInstance) {
		if (oldInstance == null) {
			throw new DataAccessException("Attempt to delete null", null);
		}
		termRepo.delete(oldInstance);
	}
	
	@Override
	public void deleteAll(List<Term> terms) {
		termRepo.delete(terms);
	}

	@Override
	public void cleanAndReset() {
		termRepo.deleteAll();
		variantRepo.deleteAll();
	}

	@Override
	public List<Term> getAll() {
		return termRepo.findAll();
	}

	@Override
	public List<Term> search(String request, Class<? extends Term> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Term> searchRange(String request, Class<? extends Term> type, int from, int to) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int searchCount(String request, Class<? extends Term> type) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void rebuildIndex() {
		super.rebuildIndex(entityManager, Term.class);
	}

}
