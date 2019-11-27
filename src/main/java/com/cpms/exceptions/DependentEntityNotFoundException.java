package com.cpms.exceptions;

import org.springframework.context.MessageSource;

import com.cpms.data.DomainObject;
import com.cpms.web.UserSessionData;

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
			String path,
			MessageSource messageSource) {
		super(
				String.format(UserSessionData.localizeText("exception.DependentEntityNotFound", messageSource),
						dependent.getCanonicalName(), owner.getCanonicalName()),
				String.format(UserSessionData.localizeText("exception.DependentEntityNotFound.explanation", messageSource),
						dependent.getCanonicalName(), dependentId, owner.getCanonicalName(), ownerId),
				cause,
				path);
					
	}
	
	public DependentEntityNotFoundException(
			Class<? extends DomainObject> owner,
			Class<? extends DomainObject> dependent,
			long ownerId,
			long dependentId,
			String path,
			MessageSource messageSource) {
		super(
				String.format(UserSessionData.localizeText("exception.DependentEntityNotFound", messageSource),
						dependent.getCanonicalName(), owner.getCanonicalName()),
				String.format(UserSessionData.localizeText("exception.DependentEntityNotFound.explanation", messageSource),
						dependent.getCanonicalName(), dependentId, owner.getCanonicalName(), ownerId),
				path);
					
	}

}
