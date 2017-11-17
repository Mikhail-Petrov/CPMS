package com.cpms.operations.interfaces;

import com.cpms.data.entities.Profile;

/**
 * Algorithmic operation interface. Compares two profile and returns a metric
 * comparison result.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public interface IProfileComparator {

	/**
	 * Executes comparison.
	 * 
	 * @param profile1 profile to be compared with second profile
	 * @param profile2 profile to be compared with first profile
	 * @return numeric result of comparison
	 */
	public double compareProfiles(Profile profile1, Profile profile2);
	
	/**
	 * Returns perfect value - a value that is returned by this algorithm implementation
	 * when two profiles specified are equal in terms of competencies.
	 * 
	 * @return perfect value
	 */
	public double getPerfectValue();
	
	/**
	 * Explains how implementation works.
	 * 
	 * @return explanation of implementation
	 */
	public String getHintMessage();
	
}
