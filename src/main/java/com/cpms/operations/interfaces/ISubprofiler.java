package com.cpms.operations.interfaces;

import java.util.Set;

import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;

/**
 * Algorithmic operation interface for executing subprofiling -
 * returning a product profile which only has competencies that are included
 * by the predicate.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public interface ISubprofiler {

	/**
	 * Executes search
	 * 
	 * @param profile profile to subprofile
	 * @param predicate list of skills that should be included in the subprofile's
	 * skills list
	 * @return subprofile of a source profile matched to a predicate
	 */
	public Profile subprofile(Profile profile, Set<Skill> predicate);
	
}
