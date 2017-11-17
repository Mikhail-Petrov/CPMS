package com.cpms.operations.implementations;

import java.util.List;
import java.util.stream.Collectors;

import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Task;
import com.cpms.operations.interfaces.IPossibilityAggregator;
import com.cpms.operations.interfaces.ITaskComparator;

/**
 * Implementation of {@link IPossibilityAggregator} that will naively stream
 * through profiles list and add all matching to the return list.
 * 
 * @see IPossibilityAggregator
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class BasicPossibilityAggregator implements IPossibilityAggregator{
	
	private ITaskComparator comparator;

	public void setComparator(ITaskComparator comparator) {
		this.comparator = comparator;
	}

	@Override
	public List<Task> aggregatePossibilities(Profile profile, List<Task> tasks) {
		return tasks
				.stream()
				.filter(x -> comparator.taskCompare(profile, x))
				.collect(Collectors.toList());
	}

}
