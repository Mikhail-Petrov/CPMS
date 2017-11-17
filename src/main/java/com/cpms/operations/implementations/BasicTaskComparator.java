package com.cpms.operations.implementations;

import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Task;
import com.cpms.operations.interfaces.ITaskComparator;

/**
 * Implementation of {@link ITaskComparator}. For each task requirement in the
 * task, looks for a competency in the profile that doesn't have a required skill
 * or competency's skill level is lover that requirement's. If it finds any,
 * it returns false. Otherwise it returns true.
 * 
 * @see ITaskComparator
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class BasicTaskComparator implements ITaskComparator {

	@Override
	public boolean taskCompare(Profile profile, Task task) {
		return !task.getRequirements().stream().anyMatch(x -> {
			Competency y = profile.getCompetencies().stream()
					.filter(z -> z.getSkill().equals(x.getSkill()))
					.findFirst()
					.orElse(null);
			if (y == null || y.getLevel() < x.getLevel()) {
				return true;
			} else {
				return false;
			}
		});
	}
	
}
