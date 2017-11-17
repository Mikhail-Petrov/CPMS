package com.cpms.security;

/**
 * Form used to register new users within web application
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class RegistrationForm {
	
	private String username;
	private String password;
	private boolean adminRole = false;
	private boolean residentRole = false;
	
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
	
}
