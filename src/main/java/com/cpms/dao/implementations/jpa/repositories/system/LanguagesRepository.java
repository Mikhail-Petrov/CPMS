package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Language;

@Repository(value = "Language")
public interface LanguagesRepository  extends JpaRepository<Language, Long> {

}
