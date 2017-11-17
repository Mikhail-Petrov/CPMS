package com.cpms.dao.interfaces;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cpms.data.AbstractDomainObject;
import com.cpms.exceptions.DataAccessException;
import com.cpms.exceptions.NumericalOverflowException;

/**
 * Intermediate DAO class that accumulates common boilerplate code.
 * 
 * @see IDAO
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public abstract class AbstractDAO<T extends AbstractDomainObject>
														implements IDAO<T> {
	
	/**
	 * Wraps try-catch block around saving an entity. Catches 
	 * DataIntegrityViolationException and throws DataAccessException instead.
	 * 
	 * @param entity entity to persist
	 * @param repository repository for entity to persist
	 * @return entity returned by "save" operation
	 */
	protected T persist(T entity, JpaRepository<T, Long> repository) {
		T returnEntity;
		try {
			returnEntity = repository.save(entity);
			repository.flush();
		} catch (DataIntegrityViolationException divException) {
			throw new DataAccessException("Insertion error.", divException);
		}
		return returnEntity;
	}

	/**
	 * Uses full text search.
	 * 
	 * @param request request to find
	 * @param manager active EntityManager instance
	 * @param entityClass class of entity requested
	 * @param fields list of properties' fields that are supposed to be used by
	 * search
	 * @return A list of entities found by this request.
	 */
	@SuppressWarnings("unchecked")
	protected List<T> useSearch(String request,
			EntityManager manager,
			Class<? extends T> entityClass,
			String... fields) {
		FullTextQuery jpaQuery = getFullTextQuery(request,
				manager,
				entityClass,
				fields);
		
		List<T> results = jpaQuery.getResultList();

		return results;
	}
	
	/**
	 * Uses full text search and filters a range of found items.
	 * 
	 * @param request request to find
	 * @param manager active EntityManager instance
	 * @param entityClass class of entity requested
	 * @param from index of the first element of the range, inclusive
	 * @param to index of the last element of the range, exclusive (element
	 * with this index will not be returned)
	 * @param fields list of properties' fields that are supposed to be used by
	 * search
	 * @return A list of entities found by this request, trimmed to specified
	 * range.
	 */
	protected List<T> useSearchRange(String request,
			EntityManager manager,
			Class<? extends T> entityClass,
			int from,
			int to,
			String... fields) {
		if (to <= from || from < 0) {
			throw new DataAccessException("Wrong range specified", null);
		}
		
		FullTextQuery jpaQuery = getFullTextQuery(request,
				manager,
				entityClass,
				fields);
		
		jpaQuery.setFirstResult(from);
		jpaQuery.setMaxResults(to - from);
		
		@SuppressWarnings("unchecked")
		List<T> results = jpaQuery.getResultList();

		return results;
	}
	
	/**
	 * Uses full text search to count all elements that match request.
	 * 
	 * @param request request to find
	 * @param manager active EntityManager instance
	 * @param entityClass class of entity requested
	 * @param fields list of properties' fields that are supposed to be used by
	 * search
	 * @return Number representing how many entities match request.
	 */
	protected int searchAndCount(String request,
			EntityManager manager,
			Class<? extends T> entityClass,
			String... fields) {
		FullTextQuery jpaQuery = getFullTextQuery(request,
				manager,
				entityClass,
				fields);
		
		return jpaQuery.getResultSize();
	}
	
	/**
	 * Creates a search query object from specified parameters.
	 * 
	 * @param request request to find
	 * @param manager active EntityManager instance
	 * @param entityClass class of entity requested
	 * @param fields list of properties' fields that are supposed to be used by
	 * search
	 * @return Query created.
	 */
	private FullTextQuery getFullTextQuery(String request,
			EntityManager manager,
			Class<? extends T> entityClass,
			String... fields) {
		FullTextEntityManager fullTextEntityManager =
				Search.getFullTextEntityManager(manager);
		   
		QueryBuilder queryBuilder = 
				fullTextEntityManager.getSearchFactory()
				.buildQueryBuilder().forEntity(entityClass).get();
		
		TermMatchingContext context = queryBuilder.keyword().wildcard()
				.onField(fields[0]);
		for(int i = 1; i < fields.length; i++) {
			context = context.andField(fields[i]);
		}
		
		Query query = context.matching(wildcardizeRequest(request))
				.createQuery();
		return fullTextEntityManager.createFullTextQuery(query, entityClass);
	}
	
	/**
	 * Splits request to words and fits wildcards between them.
	 * This is made so that the query will find fields with different
	 * combinations of words specified.
	 * 
	 * @param request request to find
	 * @return request with wildcards
	 */
	private String wildcardizeRequest(String request) {
		String[] tokens = request.split("\\s+");
		StringBuilder result = new StringBuilder("");
		result.append("*");
		for (String token : tokens) {
			result
				.append(token
						.replaceAll("\\*+", "")
						.replaceAll("\\?+", "")
						.toLowerCase())
				.append("*");
		}
		return result.toString();
	}
	
	/**
	 * Utility method for pagination. Checks if params are correct and returns
	 * all entities in a page.
	 * 
	 * @param repository repository to use
	 * @param from starting index, inclusive
	 * @param to ending index, exclusive
	 * 
	 * @throws DataAccessException if range is incorrect
	 * @throws NumericalOverflowException if (to-from) can't fit into int.
	 * 
	 * @return List of objects that match request.
	 */
	protected List<T> getPage(JpaRepository<T, Long> repository,
			long from, long to) {
		if (to <= from || from < 0) {
			throw new DataAccessException("Wrong range specified", null);
		}
		long page = to / (to - from) - 1;
		if ((to-from) > Integer.MAX_VALUE || page > Integer.MAX_VALUE ) {
			throw new NumericalOverflowException("Numbers specified as input"
					+ " arguments for paging were too large for int.");
		}
		Page<T> profiles = repository.findAll(new PageRequest((int)page, (int)(to-from)));
		return profiles.getContent();
	}
	
	/**
	 * Uses mass indexer to rebuild full text search index from scratch.
	 * Might take a lot of time.
	 * 
	 * @param manager active entity manager
	 * @param entityClass class of entity to rebuild for
	 */
	protected void rebuildIndex(EntityManager manager,
			Class<? extends T> entityClass) {
		FullTextEntityManager fullTextEntityManager =
				Search.getFullTextEntityManager(manager);
		
		fullTextEntityManager
			.createIndexer(entityClass)
			.start();
	}
	
}
