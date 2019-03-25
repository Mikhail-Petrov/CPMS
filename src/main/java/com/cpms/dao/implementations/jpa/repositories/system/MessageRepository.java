package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Message;

@Repository(value = "Message")
public interface MessageRepository  extends JpaRepository<Message, Long> {

}
