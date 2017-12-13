package com.cpms.data.entities;


/**
 * Entity that contains user data for users table
 * 
 * @author Михаил
 *
 */

public class ProfileData {
	public long id = 0;
	public String name = null;
	public boolean selected = false;

	public ProfileData(Profile profile, boolean selected) {
		id = profile.getId();
		name = profile.getPresentationName();
		this.selected = selected;
	}
}
