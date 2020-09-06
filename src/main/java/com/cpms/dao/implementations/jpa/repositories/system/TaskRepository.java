package com.cpms.dao.implementations.jpa.repositories.system;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Task;

@Repository(value = "Task")
public interface TaskRepository extends JpaRepository<Task, Long> {

	@Query("Select task from Task task where delDate is null")
	public List<Task> getAll();

	@Query("Select distinct id from Task where variant is not null and delDate is null")
	public List<Long> getInnovations();

	@Query("Select name from Task task where id = :id")
	public String getNameByID(@Param("id") long id);
}
