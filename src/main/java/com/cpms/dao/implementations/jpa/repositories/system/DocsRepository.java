package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Article;

@Repository(value = "Document")
public interface DocsRepository  extends JpaRepository<Article, Long> {
	@Query("Select sum(k.count) from Keyword k where k.doc.id = :id")
	public Integer getSumCount(@Param("id") long id);

}
