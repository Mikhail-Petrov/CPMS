package com.cpms.dao.implementations.jpa.repositories.system;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Category;

@Repository(value = "Category")
public interface CategoryRepository  extends JpaRepository<Category, Long> {
	
	@Query("Select cat from Category cat where cat.parent is null")
	public List<Category> getRoots();
	
	@Query("Select cat from Category cat where cat.parent = :parent")
	public List<Category> getChildren(@Param("parent") Category parent);
	
	@Query("Select count(id) from Category cat where cat.parent is null")
	public Integer countRoots();
	
	@Query("Select count(id) from Category cat where cat.parent = :parent")
	public Integer countChildren(@Param("parent") Category parent);

}
