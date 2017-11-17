package com.cpms.dao.implementations.jpa.repositories.applications;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cpms.data.applications.CompetencyApplication;

@Repository(value = "CompetencyApplication")
public interface CompetencyApplicationsRepository extends JpaRepository<CompetencyApplication, Long> {

	@Query("select c from CompetencyApplication c where c.ownerId = :ownerId")
	public List<CompetencyApplication> findByOwnerId(@Param("ownerId") Long ownerId);
	
}
