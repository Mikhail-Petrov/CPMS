package com.cpms.operations.interfaces;

import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Task;

/**
 * Algorithmic operation interface for comparing a profile with a task.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public interface ITaskComparator {

	/**
	 * Executes comparing.
	 * 
	 * @param profile a profile to compare with a task
	 * @param task a task to compare with a profile
	 * @return true if specified profile can execute specified task,
	 * otherwise false
	 */
	public boolean taskCompare(Profile profile, Task task);
	
}
