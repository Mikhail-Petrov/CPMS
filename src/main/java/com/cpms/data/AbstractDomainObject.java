package com.cpms.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Intermediate class for main entities which accumulates boilerplate code.
 * 
 * @see DomainObject
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
public abstract class AbstractDomainObject implements DomainObject {
	
	/**
	 * Workaround for removing entity from unstable Hibernate collections.
	 * 
	 * @param entity entity to remove from collection
	 * @param collection collection to remove entity from
	 */
	protected <T extends AbstractDomainObject> void removeEntityFromManagedCollection(
			T entity, Set<T> collection) {
		List<T> backup = new ArrayList<T>(collection);
		backup.remove(entity);
		collection.clear();
		for(T backupEntity : backup) {
			collection.add(backupEntity);
		}
	}
	
	/**
	 * Selects field to match active locale.
	 * 
	 * @param value_eng english version of the field
	 * @param value_ru russian version of the field
	 * @param locale active locale
	 * @return field which matches locale
	 */
	protected String localizeBilingualField(String value_eng, 
			String value_ru, Locale locale) {
		boolean rusLocale = false;
		if (locale.getLanguage().equals("ru")) {
			rusLocale = true;
		}
		if (value_eng == null || 
				value_eng.isEmpty() ||
				(rusLocale && value_ru != null && !value_ru.isEmpty()) ){
			return value_ru;
		} else {
			return value_eng;
		}
	}

	/**
	 * Common comparator for entity objects.
	 * <p>Returns true if both entities are of the same class and have the same id.
	 * <p>Returns false if ids are 0
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null) {
			return false;
		}
		return (DomainObject.class.isAssignableFrom(that.getClass())
				&& ((DomainObject)that).getEntityClass().equals(this.getEntityClass())
				&& ((DomainObject)that).getId() == this.getId()
				&& this.getId() != 0);
	}
	
	/**
	 * Common hash code function for entity objects. Uses Appache Commons
	 * HashCodeBuilder. Fields used are id and entity class name.
	 * For some reason doesn't work with Hibernate collections well.
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31)
				.append(getId())
				.append(getEntityClass().getCanonicalName())
				.toHashCode();
	}
	
	/**
	 * Common debug toString method for entity objects. Returns
	 * "entity.class#id".
	 */
	@Override
	public String toString() {
		return getEntityClass().getSimpleName() + "#" + getId();
	}
	
}
