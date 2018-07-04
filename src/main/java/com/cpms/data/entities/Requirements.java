package com.cpms.data.entities;

import java.util.HashMap;

public class Requirements {
	private String data = "";
	private long taskId;

	public Requirements(long taskId) {
		this.taskId = taskId;
	}
	
	public Requirements() {};
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}
	
	public HashMap<Long, Integer> getChanges() {
		HashMap<Long, Integer> result = new HashMap<>();
		String[] changes = data.split(";");
		for (String change : changes) {
			if (!change.isEmpty()) {
				String[] values = change.split(",");
				result.put(Long.valueOf(values[0]), Integer.valueOf(values[1]));
			}
		}
		return result;
	}
}
