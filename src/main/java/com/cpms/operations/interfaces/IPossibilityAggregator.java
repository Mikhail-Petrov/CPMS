package com.cpms.operations.interfaces;

import java.util.List;

import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Task;

/**
 * Algorithmic operation interface for finding all tasks that a profile can 
 * perform.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public interface IPossibilityAggregator {

	/**
	 * Executes algorithm which finds all tasks that a profile can perform.
	 * 
	 * @param profile a profile that will perform tasks
	 * @param tasks a list of tasks that should be compared to profile
	 * @return all tasks from supplied list that supplied profile can perform
	 */
	public List<Task> aggregatePossibilities(Profile profile, List<Task> tasks);
	
}
