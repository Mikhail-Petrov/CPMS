package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.DocumentTrend;

@Repository(value = "DocumentTrend")
public interface DocumentTrendRepository  extends JpaRepository<DocumentTrend, Long> {

}
