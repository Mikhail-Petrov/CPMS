package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Keyword;

@Repository(value = "Keyword")
public interface WordsRepository  extends JpaRepository<Keyword, Long> {

}
