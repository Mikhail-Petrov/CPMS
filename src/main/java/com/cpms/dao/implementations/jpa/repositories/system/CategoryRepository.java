package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Category;

@Repository(value = "Category")
public interface CategoryRepository  extends JpaRepository<Category, Long> {

}
