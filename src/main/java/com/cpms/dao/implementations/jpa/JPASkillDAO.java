package com.cpms.dao.implementations.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.implementations.jpa.repositories.system.SkillLevelRepository;
import com.cpms.dao.implementations.jpa.repositories.system.SkillRepository;
import com.cpms.dao.interfaces.AbstractDAO;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.entities.Skill;
import com.cpms.exceptions.DataAccessException;

/**
 * Implementation of {@link IDAO} interface for Skill entity.
 * 
 * @see IDAO
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Service
@Transactional("transactionManager")
public class JPASkillDAO extends AbstractDAO<Skill> implements ICleanable {
	
	protected SkillRepository skillRepo;
	protected SkillLevelRepository skillLevelRepo;
	protected EntityManager entityManager;
	
	@Autowired
	@Qualifier(value = "Skill")
	public void setSkillRepo(SkillRepository skillRepo) {
		this.skillRepo = skillRepo;
	}
	
	@Autowired
	@Qualifier(value = "SkillLevel")
	public void setSkillLevelRepo(SkillLevelRepository skillLevelRepo) {
		this.skillLevelRepo = skillLevelRepo;
	}
	
	@PersistenceContext(unitName = "entityManagerFactory")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public List<Skill> getAll() {
		return skillRepo.findAll();
	}

	@Override
	public Skill update(Skill newSkill) {
		if (newSkill == null) {
			throw new DataAccessException("Attempt to update null.", null);
		}
		newSkill.getLevels();
		//newSkill.getChildren();
		newSkill.getImplementers();
		newSkill.getImplementersTask();
		if (!skillRepo.exists(newSkill.getId())) {
			throw new DataAccessException("Cannot update, such skill doesn't exist",
					null);
		}
		Skill skill = persist(newSkill, skillRepo);
		return skill;
	}

	@Override
	public Skill insert(Skill newSkill) {
		if (newSkill == null) {
			throw new DataAccessException("Cannot insert null.", null);
		}
		if (skillRepo.exists(newSkill.getId())) {
			throw new DataAccessException("Cannot insert, such skill already exists",
					null);
		}
		Skill skill = persist(newSkill, skillRepo);
		return skill;
	}
	
	@Override
	public List<Skill> insertAll(List<Skill> skills) {
		int limit = 900;
		int size = skills.size();
		if (size <= limit)
			return skillRepo.save(skills);
		List<Skill> save = new ArrayList<>();
		int i;
		for (i = 0; i < size - limit; i += limit)
			save.addAll(skillRepo.save(skills.subList(i, i + limit)));
		if (i < size) {
			save.addAll(skillRepo.save(skills.subList(i, size)));
		}
		return save;
	}

	@Override
	public void delete(Skill oldSkill) {
		if (oldSkill == null) {
			throw new DataAccessException("Cannot delete null.", null);
		}
		oldSkill = getOne(oldSkill.getId());
		oldSkill.detachChildren(skillRepo);
		List<Skill> skills = skillRepo.getChildren(oldSkill);
		if (skills != null)
			for(Skill child : skills)
				update(child);
		oldSkill.setParent(null);
		update(oldSkill);
		skillRepo.flush();
		skillRepo.delete(oldSkill);
	}

	@Override
	public void cleanAndReset() {
		skillRepo.deleteAll();
		skillLevelRepo.deleteAll();
	}

	@Override
	public Skill getOne(long id) {
		Skill skill = skillRepo.findOne(id);
		return skill;
	}

	@Override
	public long count() {
		return skillRepo.count();
	}

	@Override
	public List<Skill> getRange(long from, long to) {
		List<Skill> range = super.getPage(skillRepo, from, to);
		return range;
	}

	@Override
	public List<Skill> search(String request, Class<? extends Skill> type) {
		List<Skill> range = super.useSearch(request,
				entityManager,
				Skill.class,
				"name");
		return range;
	}

	@Override
	public List<Skill> searchRange(String request, Class<? extends Skill> type,
			int from, int to) {
		List<Skill> range = super.useSearchRange(request,
				entityManager,
				Skill.class,
				from, to,
				"name");
		return range;
	}

	@Override
	public int searchCount(String request, Class<? extends Skill> type) {
		return super.searchAndCount(request,
				entityManager,
				Skill.class,
				"name");
	}

	@Override
	public void rebuildIndex() {
		super.rebuildIndex(entityManager, Skill.class);
	}
}
