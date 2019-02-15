package com.cpms.operations.implementations;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Profile;
import com.cpms.operations.interfaces.IProfileComparator;
import com.cpms.operations.interfaces.IProfileCompetencySearcher;

/**
 * Implementation of {@link IProfileCompetencySearcher}. Will naively stream
 * through each profile, returning only matching ones.
 * 
 * @see IProfileCompetencySearcher
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class BasicProfileCompetencySearcher implements IProfileCompetencySearcher{

	private IProfileComparator comparator;
	
	public void setComparator(IProfileComparator comparator) {
		this.comparator = comparator;
	}

	@Override
	public List<Profile> searchForProfiles(List<Profile> profiles, Set<Competency> competencies,
			double acceptableDifference) {
		Profile resource = new Profile();
		resource.setCompetencies(competencies);
		return profiles
			.stream()
			.filter(x -> Math.abs(comparator.compareProfiles(x, resource))
					> Math.abs(comparator.getPerfectValue() - acceptableDifference))
			.collect(Collectors.toList());
	}

}
