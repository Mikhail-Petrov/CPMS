package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.MessageCenter;

@Repository(value = "MessageCenter")
public interface MessageCenterRepository extends JpaRepository<MessageCenter, Long> {
}
