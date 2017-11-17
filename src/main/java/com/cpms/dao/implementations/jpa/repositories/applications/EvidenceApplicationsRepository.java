package com.cpms.dao.implementations.jpa.repositories.applications;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cpms.data.applications.EvidenceApplication;

@Repository(value = "EvidenceApplication")
public interface EvidenceApplicationsRepository extends JpaRepository<EvidenceApplication, Long> {
	
	@Query("select e from EvidenceApplication e where e.competencyId = :competencyId")
	public List<EvidenceApplication> findByCompetencyId(
			@Param("competencyId") long competencyId);

	@Query("select e from EvidenceApplication e where e.ownerId = :ownerId")
	public List<EvidenceApplication> findByOwnerId(
			@Param("ownerId") long ownerId);

}
