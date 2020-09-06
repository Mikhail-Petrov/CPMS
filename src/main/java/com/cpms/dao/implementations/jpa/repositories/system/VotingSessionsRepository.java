package com.cpms.dao.implementations.jpa.repositories.system;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.VotingSession;

@Repository(value = "VotingSession")
public interface VotingSessionsRepository  extends JpaRepository<VotingSession, Long> {

	@Query("Select max(id) from VotingSession where endDate >= :start_date")
	public List<Long> getLastSession(@Param("start_date") Date start_date);
}
