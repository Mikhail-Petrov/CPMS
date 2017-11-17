package com.cpms.dao.interfaces;

import java.util.List;

import com.cpms.data.AbstractDomainObject;
import com.cpms.exceptions.DataAccessException;

/**
 * Main interface for CRUD services.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public interface IDAO<T extends AbstractDomainObject> {

	/**
	 * Counts managed entities.
	 * 
	 * @return ammount of managed entities found
	 */
	public long count();
	
	/**
	 * Returns a range of entities.
	 * 
	 * @param from lower index, inclusive
	 * @param to upper index, exclusive
	 * @return list containing specified range of entities
	 */
	public List<T> getRange(long from, long to);
	
	/**
	 * Returns all entities found.
	 * 
	 * @return list containing all entities
	 */
	public List<T> getAll();
	
	/**
	 * Uses full text search and returns all matching entities.
	 * 
	 * @param request search request to match
	 * @param type entity type to look for
	 * @return list containing all entities that match search request
	 */
	public List<T> search(String request, Class<? extends T> type);
	
	/**
	 * Uses full text search and returns range of matching entities.
	 * 
	 * @param request search request to match
	 * @param type entity type to look for
	 * @param from lower index, inclusive
	 * @param to upper index, exclusive
	 * @return list containing all entities that match search request and range
	 */
	public List<T> searchRange(String request, Class<? extends T> type, 
			int from, int to);
	
	/**
	 * Counts all entities that match specified full text search request.
	 * 
	 * @param request search request to match
	 * @param type entity type to look for
	 * @return number of entities found that match request
	 */
	public int searchCount(String request, Class<? extends T> type);
	
	/**
	 * Finds a single entity by it's id.
	 * 
	 * @param id entity's id
	 * @return specified entity or null
	 */
	public T getOne(long id);
	
	/**
	 * Inserts specified entity.
	 * 
	 * @throws DataAccessException if specified entity already exists
	 * 
	 * @param newInstance instance to save
	 * @return saved instance
	 */
	public T insert(T newInstance);
	
	/**
	 * Updates saved entity to the state specified.
	 * 
	 * @throws DataAccessException if specified entity does not exist
	 * 
	 * @param updateInstance instance that should update it's copy persisted
	 * @return updated entity
	 */
	public T update(T updateInstance);
	
	/**
	 * Deletes specified entity from storage.
	 * 
	 * @param deleteInstance instance to delete
	 */
	public void delete(T deleteInstance);
	
	/**
	 * Rebuilds full text search index from scratch.
	 */
	public void rebuildIndex();
	
}
