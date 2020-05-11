package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Term;

@Repository(value = "Term")
public interface TermsRepository  extends JpaRepository<Term, Long> {

}
