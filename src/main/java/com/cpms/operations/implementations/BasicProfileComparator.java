package com.cpms.operations.implementations;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.operations.interfaces.IProfileComparator;

/**
 * Implementation of {@link IProfileComparator} that executes the following
 * algorithm:
 * <p>
 * <p>1)Concatenate streams of competencies of two profiles to recieve
 * common skills list
 * <p>2)For each skill compute 
 * ((double)Math.abs(level1 - level2) / Math.max(level1, level2)),
 * where level1 and level2 are first and second profile's skill level for this skill.
 * <p>3)Summarize all of results.
 * <p>4)Use formula (1 - (result / skills.size())) on the sum of results.
 * This is the value that will be returned.
 * <p>
 * <p>The result will be in the range from 0 to 1, where 1 is the perfect value
 * and 0 is reached when profiles have no common skills.
 * 
 * @see IProfileComparator
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class BasicProfileComparator implements IProfileComparator {

	@Override
	public double compareProfiles(Profile profile1, Profile profile2) {
		if (profile1.equals(profile2)) {
			return 1;
		}
		if (profile1.getCompetencies().size() == 0) {
			if (profile2.getCompetencies().size() == 0) {
				return 1;
			} else {
				return 0;
			}
		}
		Set<Skill> skills = Stream.concat(profile1.getCompetencies().stream(),
					profile2.getCompetencies().stream())
				.map(x -> x.getSkill())
				.collect(Collectors.toCollection(HashSet::new));
		double result = skills.stream().mapToDouble(x -> {
			int level1 = profile1.getCompetencies().stream()
					.filter(k -> k.getSkill().equals(x))
					.map(k -> k.getLevel())
					.findFirst()
					.orElse(0);
			int level2 = profile2.getCompetencies().stream()
					.filter(k -> k.getSkill().equals(x))
					.map(k -> k.getLevel())
					.findFirst()
					.orElse(0);
			return ((double)Math.abs(level1 - level2) / Math.max(level1, level2));
		}).sum();
		return (1 - (result / skills.size()));
	}

	@Override
	public double getPerfectValue() {
		return 1;
	}

	@Override
	public String getHintMessage() {
		return "";
	}
	
}
