package com.cpms.facade;

import com.cpms.dao.interfaces.*;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.Task;
import com.cpms.operations.interfaces.*;

/**
 * Facade pattern implementation which encapsulates most system components.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public interface ICPMSFacade {
	
	public IDAO<Profile> getProfileDAO();
	
	public IDAO<Skill> getSkillDAO();
	
	public IDAO<Task> getTaskDAO();
	
	public ISubprofiler getSubprofiler();
	
	public ITaskComparator getTaskComparator();
	
	public IProfileComparator getProfileComparator();
	
	public IProfileCompetencySearcher getProfileCompetencySearcher();
	
	public IProfileRanger getProfileRanger();
	
	public IPossibilityAggregator getPossibilityAggregator();
}
