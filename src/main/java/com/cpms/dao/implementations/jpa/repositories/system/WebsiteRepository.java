package com.cpms.dao.implementations.jpa.repositories.system;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Website;

@Repository(value = "Website")
public interface WebsiteRepository  extends JpaRepository<Website, Long> {
	
	@Query("Select cat from Website cat where cat.parent is null")
	public List<Website> getRoots();
	
	@Query("Select cat from Website cat where cat.parent = :parent")
	public List<Website> getChildren(@Param("parent") Website parent);
	
	@Query("Select count(id) from Website cat where cat.parent is null")
	public Integer countRoots();
	
	@Query("Select count(id) from Website cat where cat.parent = :parent")
	public Integer countChildren(@Param("parent") Website parent);

}
