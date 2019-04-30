package com.cpms.dao.implementations.jpa.repositories.system;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Skill;

@Repository(value = "Skill")
public interface SkillRepository extends JpaRepository<Skill, Long> {

	@Query("Select skill from Skill skill")
	public List<Skill> findByDraft();
	
	@Query("Select skill from Skill skill where skill.owner = :owner")
	public List<Skill> findByDraftAndOwner(@Param("owner") Long owner);
	
}
