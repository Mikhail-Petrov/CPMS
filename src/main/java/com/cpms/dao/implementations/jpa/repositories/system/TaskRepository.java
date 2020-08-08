package com.cpms.dao.implementations.jpa.repositories.system;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Task;

@Repository(value = "Task")
public interface TaskRepository extends JpaRepository<Task, Long> {

	@Query("Select task from Task task where delDate is null")
	public List<Task> getAll();
}
