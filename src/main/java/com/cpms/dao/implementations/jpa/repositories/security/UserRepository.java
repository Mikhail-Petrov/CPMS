package com.cpms.dao.implementations.jpa.repositories.security;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cpms.security.entities.Users;

@Repository(value = "Users")
public interface UserRepository extends JpaRepository<Users, Long> {

	@Query("Select u from Users u where u.username = :username")
	public List<Users> retrieveUserByUsername(@Param("username") String username);
	
	@Query("Select u from Users u where u.profileId = :profileId")
	public Users retrieveUserByProfileId(@Param("profileId") long profileId);
	
	@Query("Select u from Users u where u.id = :userId")
	public Users retrieveUserByUserId(@Param("userId") long userId);
	
}
