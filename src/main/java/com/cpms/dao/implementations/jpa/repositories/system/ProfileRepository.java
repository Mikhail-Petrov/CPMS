package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Profile;

@Repository(value = "Profile")
public interface ProfileRepository extends JpaRepository<Profile, Long>  {
	
}
