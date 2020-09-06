package com.cpms.dao.implementations.jpa.repositories.system;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Profile;

@Repository(value = "Profile")
public interface ProfileRepository extends JpaRepository<Profile, Long>  {

	@Query("Select distinct profileId from Users where profileId is not null")
	public List<Long> getExperts();

	@Query("Select name from Profile where id = :id")
	public String getNameByID(@Param("id") long id);
	
}
