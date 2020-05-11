package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.TermVariant;

@Repository(value = "Termvariant")
public interface TermVariantRepository extends JpaRepository<TermVariant, Long> {

}
