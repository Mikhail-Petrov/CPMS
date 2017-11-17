package com.cpms.operations.interfaces;

import java.util.List;
import java.util.Set;

import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Profile;

/**
 * Algorithmic operation interface for finding profiles using competency set
 * as a search criteria.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public interface IProfileCompetencySearcher {
	
	/**
	 * Executes search.
	 * 
	 * @param profiles profiles to match against competencies
	 * @param competencies competencies criteria
	 * @param acceptableDifference acceptable difference between perfect value
	 * and value recieved as a result of comparing profile from list and criteria
	 * @return list of profiles that match search within acceptable difference
	 */
	public List<Profile> searchForProfiles(List<Profile> profiles, 
			Set<Competency> competencies,
			double acceptableDifference);
	
}
