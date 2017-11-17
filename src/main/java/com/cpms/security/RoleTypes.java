package com.cpms.security;

/**
 * Enumeration for user roles.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public enum RoleTypes {
	
	ADMIN ("ADMIN"),
	RESIDENT ("RESIDENT");
	
	private final String name;
	private final String roleName;
	
	private RoleTypes(String name) {
		this.name = name;
		roleName = "ROLE_" + name;
	}
	
	/**
	 * @return role with "ROLE_" prefix
	 */
	public String toRoleName() {
		return roleName;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
