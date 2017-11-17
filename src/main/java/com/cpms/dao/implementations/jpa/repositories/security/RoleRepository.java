package com.cpms.dao.implementations.jpa.repositories.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.security.entities.Role;

@Repository(value = "Role")
public interface RoleRepository extends JpaRepository<Role, Integer> {

}
