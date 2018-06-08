package com.cpms.data.entities;

import java.util.HashMap;

public class Competencies {
	private String data = "";
	private long profileId;

	public Competencies(long profileId) {
		this.profileId = profileId;
	}
	
	public Competencies() {};
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public long getProfileId() {
		return profileId;
	}

	public void setProfileId(long profileId) {
		this.profileId = profileId;
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
