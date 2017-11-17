package com.cpms.operations.interfaces;

import java.util.List;
import java.util.Set;

import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Profile;

/**
 * Algorithmic operation interface for sorting profiles by how much
 * they match competency criteria.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public interface IProfileRanger {

	/**
	 * Executes sorting.
	 * 
	 * @param profiles profiles to sort
	 * @param criteria criteria by which sorting should happen
	 * @param closestFirst if true, profiles will be sorted by decreasing of
	 * match, else increasing
	 * @return
	 */
	public List<Profile> rangeProfiles(List<Profile> profiles,
			Set<Competency> criteria,
			boolean closestFirst);
	
}
