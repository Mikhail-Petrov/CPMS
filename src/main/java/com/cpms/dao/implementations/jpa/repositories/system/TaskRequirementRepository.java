package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.TaskRequirement;

@Repository(value = "TaskRequirement")
public interface TaskRequirementRepository extends JpaRepository<TaskRequirement, Long> {

}
