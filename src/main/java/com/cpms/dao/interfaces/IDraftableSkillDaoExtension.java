package com.cpms.dao.interfaces;

import java.util.List;

import com.cpms.data.entities.Skill;

/**
 * Extension for Skill entity {@link IDAO} which allows for drafts.
 * What it means is that now residents can create their own skill but only
 * they could see it unless administrators approve those skills.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public interface IDraftableSkillDaoExtension {

	/**
	 * Return not only normal skills, but also draft ones.
	 * 
	 * @return list containing all skills including drafts
	 */
	public List<Skill> getAllIncludingDrafts();
	
	/**
	 * Finds all drafts of a specific user.
	 * 
	 * @param id id of resident user
	 * @return list containing all drafts created by specified user
	 */
	public List<Skill> getDraftsOfUser(Long id);
	
}
