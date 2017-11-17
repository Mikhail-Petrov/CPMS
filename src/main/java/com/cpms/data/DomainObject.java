package com.cpms.data;

import java.io.Serializable;
import java.util.Locale;

/**
 * Common interface for all stored entities.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public interface DomainObject extends Serializable {

	/**
	 * Returns entities class. Used for equality check.
	 * 
	 * @return class this entity represents
	 */
	public Class<?> getEntityClass();
	
	/**
	 * Returns entities' id.
	 * 
	 * @return id of an entity
	 */
	public long getId();
	
	/**
	 * Alternative toString, which returns string to represent object within gui.
	 * 
	 * @return representation of object for guis.
	 */
	public String getPresentationName();
	
	/**
	 * Sets primary properties of this entity with values matching locale.
	 * Works only for locals "ru" and "en". Also localizes dependent entities,
	 * but not of the same class.
	 * 
	 * <p>Example: if entity has fields "name" and "name_RU", and locale is
	 * "ru", returned entity will have it's "name" set with source entitie's
	 * "name_RU". Implementations are usually aware of empty fields and ignore
	 * this operation in that case.
	 * 
	 * @param locale current active locale
	 * @return translated entity
	 */
	public <T extends DomainObject> T localize(Locale locale);
	
}
