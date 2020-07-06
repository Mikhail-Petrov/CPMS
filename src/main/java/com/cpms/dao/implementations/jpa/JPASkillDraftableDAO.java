package com.cpms.dao.implementations.jpa;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.interfaces.IDraftableSkillDaoExtension;
import com.cpms.data.entities.Skill;

/**
 * Extension of {@link JPASkillDAO} that allows working with skill drafts.
 * 
 * @see IDraftableSkillDaoExtension
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Service
@Transactional("transactionManager")
public class JPASkillDraftableDAO extends JPASkillDAO 
					implements IDraftableSkillDaoExtension {
	
	@Override
	public List<Skill> getAll() {
		return skillRepo.findByDraft();
	}
	
	@Override
	public List<Skill> getAllIncludingDrafts() {
		return skillRepo.findAll();
	}
	
	@Override
	public List<Skill> getDraftsOfUser(Long id) {
		return skillRepo.findByDraftAndOwner(id);
	}

	@Override
	public List<Skill> getChildren(Skill parent) {
		if (parent == null)
			return skillRepo.getRoots();
		return skillRepo.getChildren(parent);
	}

	@Override
	public int countChildren(Skill parent) {
		Integer res;
		if (parent == null)
			res = skillRepo.countRoots();
		else
			res = skillRepo.countChildren(parent);
		return res == null ? 0 : res;
	}

	@Override
	public List<Skill> findByName(String name) {
		return skillRepo.findByName(name);
	}

	@Override
	public List<Skill> findByAlternative(String alternative) {
		return skillRepo.findByAlternative(alternative);
	}

}
