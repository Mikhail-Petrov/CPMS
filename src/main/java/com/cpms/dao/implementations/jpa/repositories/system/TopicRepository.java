package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Topic;

@Repository(value = "Topic")
public interface TopicRepository  extends JpaRepository<Topic, Long> {

}
