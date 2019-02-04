package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Motivation;

@Repository(value = "Motivation")
public interface MotivationRepository  extends JpaRepository<Motivation, Long> {

}
