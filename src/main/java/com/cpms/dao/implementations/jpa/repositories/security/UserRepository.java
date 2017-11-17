package com.cpms.dao.implementations.jpa.repositories.security;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cpms.security.entities.User;

@Repository(value = "User")
public interface UserRepository extends JpaRepository<User, Long> {

	@Query("Select user from User user where user.username = :username")
	public List<User> retrieveUserByUsername(@Param("username") String username);
	
	@Query("Select user from User user where user.profileId = :profileId")
	public User retrieveUserByProfileId(@Param("profileId") long profileId);
	
}
