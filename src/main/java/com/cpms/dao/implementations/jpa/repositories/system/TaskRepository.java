package com.cpms.dao.implementations.jpa.repositories.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Task;

@Repository(value = "Task")
public interface TaskRepository extends JpaRepository<Task, Long> {

}
