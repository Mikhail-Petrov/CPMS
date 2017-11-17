package com.cpms.operations.implementations;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.cpms.data.entities.Company;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Profile;
import com.cpms.operations.interfaces.IProfileComparator;
import com.cpms.operations.interfaces.IProfileRanger;

/**
 * Implementation of {@link IProfileRanger}. Uses sorting of a stream.
 * 
 * @see IProfileRanger
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class BasicProfileRanger implements IProfileRanger {
	
	private class Pair {
		
		private Profile profile;
		
		private double val;
		
		public Pair(Profile profile, double val) {
			this.profile = profile;
			this.val = val;
		}

		public Profile getProfile() {
			return profile;
		}

		public double getVal() {
			return val;
		}
		
	}

	private IProfileComparator comparator;
	
	public void setComparator(IProfileComparator comparator) {
		this.comparator = comparator;
	}

	@Override
	public List<Profile> rangeProfiles(List<Profile> profiles,
			Set<Competency> criteria,
			boolean closestFirst) {
		Profile resource = new Company();
		resource.setCompetencies(criteria);
		List<Profile> result = profiles
				.stream()
				.map(x -> new Pair(x, comparator.compareProfiles(x, resource)))
				.sorted((x1, x2) -> (closestFirst ? -1 : 1) *
						Double.compare(x1.getVal(), x2.getVal()))
				.map(x -> x.getProfile())
				.collect(Collectors.toList());
		return result;
	}

}
