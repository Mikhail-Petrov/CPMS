package com.cpms.exceptions;

import com.cpms.data.DomainObject;

/**
 * Thrown in case an entity was requested to find it's child entity, but
 * finds out such child does not exist.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public class DependentEntityNotFoundException extends WebException {
	
	public DependentEntityNotFoundException(
			Class<? extends DomainObject> owner,
			Class<? extends DomainObject> dependent,
			long ownerId,
			long dependentId,
			Exception cause,
			String path) {
		super(
				dependent.getCanonicalName()
					+ " of "
					+ owner.getCanonicalName()
					+ " that you have requested was not found.",
				"Entity of type " 
					+ dependent.getCanonicalName()
					+ " with id of "
					+ dependentId
					+ " of owner "
					+ owner.getCanonicalName()
					+ " with id of "
					+ ownerId
					+ " requested and not found.",
					cause,
					path);
					
	}
	
	public DependentEntityNotFoundException(
			Class<? extends DomainObject> owner,
			Class<? extends DomainObject> dependent,
			long ownerId,
			long dependentId,
			String path) {
		super(
				dependent.getCanonicalName()
					+ " of "
					+ owner.getCanonicalName()
					+ " that you have requested was not found.",
				"Entity of type " 
					+ dependent.getCanonicalName()
					+ " with id of "
					+ dependentId
					+ " of owner "
					+ owner.getCanonicalName()
					+ " with id of "
					+ ownerId
					+ " requested and not found.",
					path);
					
	}

}
