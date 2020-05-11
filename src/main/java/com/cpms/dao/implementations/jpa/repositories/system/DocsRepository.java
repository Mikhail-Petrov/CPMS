package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Article;

@Repository(value = "Document")
public interface DocsRepository  extends JpaRepository<Article, Long> {

}
