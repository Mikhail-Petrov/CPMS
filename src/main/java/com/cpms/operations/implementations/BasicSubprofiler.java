package com.cpms.operations.implementations;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.operations.interfaces.ISubprofiler;

/**
 * Implementation of {@link ISubprofiler}. Copies both predicate and
 * competencies of profile and then iterates through them to remove all
 * that are not in the predicate.
 * 
 * @see ISubprofiler
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class BasicSubprofiler implements ISubprofiler {

	@Override
	public Profile subprofile(Profile profile, Set<Skill> predicate) {
		Set<Skill> predicateClone = new HashSet<Skill>(predicate);
		Set<Competency> newCompetencies = profile.getCompetencies()
			.stream()
			.filter(x -> predicateClone.removeIf(y -> y.equals(x.getSkill())))
			.collect(Collectors.toSet());
		predicateClone.forEach(x -> newCompetencies.add(new Competency(x, 0)));
		Profile returnProfile = profile.clone();
		returnProfile.setCompetencies(newCompetencies);
		returnProfile.setId(profile.getId());
		return returnProfile;
	}

}
