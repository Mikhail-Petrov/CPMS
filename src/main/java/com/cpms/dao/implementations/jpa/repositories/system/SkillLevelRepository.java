package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.SkillLevel;

@Repository(value = "SkillLevel")
public interface SkillLevelRepository extends JpaRepository<SkillLevel, Long> {

}
