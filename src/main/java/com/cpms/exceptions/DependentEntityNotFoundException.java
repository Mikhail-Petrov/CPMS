package com.cpms.exceptions;

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
			String path) {
		super(
				dependent.getCanonicalName()
					+ UserSessionData.localizeText(" ", " of ")
					+ owner.getCanonicalName()
					+ UserSessionData.localizeText(", который вы запрашивали, не был найден.",
							" that you have requested was not found."),
					UserSessionData.localizeText("Сущность типа ", "Entity of type ") 
					+ dependent.getCanonicalName()
					+ UserSessionData.localizeText(" с идентификатором ", " with id of ")
					+ dependentId
					+ UserSessionData.localizeText(" владельца ", " of owner ")
					+ owner.getCanonicalName()
					+ UserSessionData.localizeText(" с идентификатором ", " with id of ")
					+ ownerId
					+ UserSessionData.localizeText(", которая была запрошена, не найдена",
							" requested and not found."),
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
				+ UserSessionData.localizeText(" ", " of ")
				+ owner.getCanonicalName()
				+ UserSessionData.localizeText(", который вы запрашивали, не был найден.",
						" that you have requested was not found."),
				UserSessionData.localizeText("Сущность типа ", "Entity of type ") 
				+ dependent.getCanonicalName()
				+ UserSessionData.localizeText(" с идентификатором ", " with id of ")
				+ dependentId
				+ UserSessionData.localizeText(" владельца ", " of owner ")
				+ owner.getCanonicalName()
				+ UserSessionData.localizeText(" с идентификатором ", " with id of ")
				+ ownerId
				+ UserSessionData.localizeText(", которая была запрошена, не найдена",
						" requested and not found."),
					path);
					
	}

}
