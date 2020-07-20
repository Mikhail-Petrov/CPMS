package com.cpms.dao.implementations.jpa.repositories.system;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Trend;

@Repository(value = "Trend")
public interface TrendRepository  extends JpaRepository<Trend, Long> {
	
	@Query("Select cat from Trend cat where cat.parent is null")
	public List<Trend> getRoots();
	
	@Query("Select cat from Trend cat where cat.parent = :parent")
	public List<Trend> getChildren(@Param("parent") Trend parent);
	
	@Query("Select count(id) from Trend cat where cat.parent is null")
	public Integer countRoots();
	
	@Query("Select count(id) from Trend cat where cat.parent = :parent")
	public Integer countChildren(@Param("parent") Trend parent);

}
