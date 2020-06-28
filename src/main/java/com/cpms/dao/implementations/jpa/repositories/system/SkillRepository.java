package com.cpms.dao.implementations.jpa.repositories.system;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Skill;

@Repository(value = "Skill")
public interface SkillRepository extends JpaRepository<Skill, Long> {

	@Query("Select skill from Skill skill where delDate is null")
	public List<Skill> findByDraft();
	
	@Query("Select skill from Skill skill where skill.owner = :owner")
	public List<Skill> findByDraftAndOwner(@Param("owner") Long owner);
	
	@Query("Select skill from Skill skill where skill.parent = :parent and delDate is null")
	public List<Skill> getChildren(@Param("parent") Skill parent);
	
	@Query("Select skill from Skill skill where skill.parent is null and delDate is null")
	public List<Skill> getRoots();
	
	@Query("Select skill from Skill skill where skill.name like :name and delDate is null")
	public List<Skill> findByName(@Param("name") String name);
	
	@Query("Select skill from Skill skill where '|'+skill.alternative+'|' like :alternative and delDate is null")
	public List<Skill> findByAlternative(@Param("alternative") String alternative);
	
}
