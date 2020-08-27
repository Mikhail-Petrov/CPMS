package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.VotingSession;

@Repository(value = "VotingSession")
public interface VotingSessionsRepository  extends JpaRepository<VotingSession, Long> {

}
