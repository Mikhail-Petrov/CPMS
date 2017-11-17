package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Evidence;

@Repository(value = "Evidence")
public interface EvidenceRepository  extends JpaRepository<Evidence, Long> {

}
