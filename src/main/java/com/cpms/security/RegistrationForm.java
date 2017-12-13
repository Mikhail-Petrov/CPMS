package com.cpms.security;

/**
 * Form used to register new users within web application
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class RegistrationForm {
	
	public Long id;
	public Long profileId;
	public String username;
	public String password;
	public boolean adminRole = false;
	public boolean residentRole = false;
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean isAdminRole() {
		return adminRole;
	}
	
	public void setAdminRole(boolean adminRole) {
		this.adminRole = adminRole;
	}
	
	public boolean isResidentRole() {
		return residentRole;
	}
	
	public void setResidentRole(boolean residentRole) {
		this.residentRole = residentRole;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the profileId
	 */
	public Long getProfileId() {
		return profileId;
	}

	/**
	 * @param profileId the profileId to set
	 */
	public void setProfileId(Long profileId) {
		this.profileId = profileId;
	}
	
}
