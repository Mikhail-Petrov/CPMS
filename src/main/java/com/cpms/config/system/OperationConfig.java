package com.cpms.config.system;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cpms.operations.implementations.BasicPossibilityAggregator;
import com.cpms.operations.implementations.BasicProfileComparator;
import com.cpms.operations.implementations.BasicProfileCompetencySearcher;
import com.cpms.operations.implementations.BasicProfileRanger;
import com.cpms.operations.implementations.BasicSubprofiler;
import com.cpms.operations.implementations.BasicTaskComparator;
import com.cpms.operations.interfaces.IPossibilityAggregator;
import com.cpms.operations.interfaces.IProfileComparator;
import com.cpms.operations.interfaces.IProfileCompetencySearcher;
import com.cpms.operations.interfaces.IProfileRanger;
import com.cpms.operations.interfaces.ISubprofiler;
import com.cpms.operations.interfaces.ITaskComparator;

/**
 * Creates java beans for operation implementations
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Configuration
public class OperationConfig {

	/**
	 * @return implementation of IPossibilityAggregator
	 * @see IPossibilityAggregator
	 */
	@Bean(name = "possibilityAggregator")
	public IPossibilityAggregator getPossibilityAggregator() {
		BasicPossibilityAggregator target = new BasicPossibilityAggregator();
		target.setComparator(getTaskComparator());
		return target;
	}

	/**
	 * @return implementation of IProfileComparator
	 * @see IProfileComparator
	 */
	@Bean(name = "profileComparator")
	public IProfileComparator getProfileComparator() {
		return new BasicProfileComparator();
	}

	/**
	 * @return implementation of IProfileCompetencySearcher
	 * @see IProfileCompetencySearcher
	 */
	@Bean(name = "profileCompetencySearcher")
	public IProfileCompetencySearcher getProfileCompetencySearcher() {
		BasicProfileCompetencySearcher target = new BasicProfileCompetencySearcher();
		target.setComparator(getProfileComparator());
		return target;
	}

	/**
	 * @return implementation of IProfileRanger
	 * @see IProfileRanger
	 */
	@Bean(name = "profileRanger")
	public IProfileRanger getProfileRanger() {
		BasicProfileRanger target = new BasicProfileRanger();
		target.setComparator(getProfileComparator());
		return target;
	}

	/**
	 * @return implementation of ISubprofiles
	 * @see ISubprofiler
	 */
	@Bean(name = "subprofiler")
	public ISubprofiler getSubprofiler() {
		return new BasicSubprofiler();
	}

	/**
	 * @return implementation of ITaskComparator
	 * @see ITaskComparator
	 */
	@Bean(name = "taskComparator")
	public ITaskComparator getTaskComparator() {
		return new BasicTaskComparator();
	}

}
