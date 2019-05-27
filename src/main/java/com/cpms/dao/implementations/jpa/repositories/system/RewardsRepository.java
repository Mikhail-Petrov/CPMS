package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Reward;

@Repository(value = "Reward")
public interface RewardsRepository  extends JpaRepository<Reward, Long> {

}
