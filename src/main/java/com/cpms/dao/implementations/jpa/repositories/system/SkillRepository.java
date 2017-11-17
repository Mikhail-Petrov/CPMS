package com.cpms.dao.implementations.jpa.repositories.system;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Skill;

@Repository(value = "Skill")
public interface SkillRepository extends JpaRepository<Skill, Long> {

	@Query("Select skill from Skill skill where skill.draft = :draft")
	public List<Skill> findByDraft(@Param("draft") boolean draft);
	
	@Query("Select skill from Skill skill where skill.draft = :draft and skill.owner = :owner")
	public List<Skill> findByDraftAndOwner(@Param("draft") boolean draft, @Param("owner") Long owner);
	
}
