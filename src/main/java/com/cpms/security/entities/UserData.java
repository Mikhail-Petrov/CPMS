package com.cpms.security.entities;

import java.util.List;

import com.cpms.security.RoleTypes;

/**
 * Entity that contains user data for users table
 * 
 * @author Михаил
 *
 */

public class UserData {
	public long id, profileId;
	public String name, password, profileName = null;
	public boolean isAdmin, isResident;
	public List<Role> roles;

	public UserData(User user) {
		this.id = user.getId();
		this.name = user.getUsername();
		this.password = user.getPassword();
		this.roles = user.getRoles();

		this.isAdmin = user.checkRole(RoleTypes.MANAGER);
		this.isResident= user.checkRole(RoleTypes.EXPERT);
		
		if (this.isResident)
			//this.profileId = user.getProfileId();
			this.profileId = 0;
		else
			this.profileId = 0;
	}
	
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
}
