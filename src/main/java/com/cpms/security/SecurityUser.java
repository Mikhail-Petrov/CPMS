package com.cpms.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.cpms.security.entities.Role;
import com.cpms.security.entities.User;

/**
 * Connector for {@link User} which allows Spring security to use it.
 * 
 * @see User
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings({ "serial" })
public class SecurityUser extends User implements UserDetails {

	public SecurityUser() {	}
	
	public SecurityUser(User user) {
		if (user != null) {
			setId(user.getId());
			setUsername(user.getUsername());
			setPassword(user.getPassword());
			setRoles(prefixRoles(user.getRoles()));
		}
	}
	
	private List<Role> prefixRoles(List<Role> roles) {
		List<Role> prefixRoles = new ArrayList<Role>();
		for (Role role : roles) {
			Role newRole = new Role();
			if (!role.getRolename().startsWith("ROLE_")) {
				newRole.setRolename("ROLE_" + role.getRolename());
			} else {
				newRole.setRolename(role.getRolename());
			}
			prefixRoles.add(newRole);
		}
		return prefixRoles;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities =
				new ArrayList<GrantedAuthority>();
		List<Role> roles = getRoles();
		if (roles != null) {
			for (Role role : roles) {
				SimpleGrantedAuthority authority =
						new SimpleGrantedAuthority(role.getRolename());
				authorities.add(authority);
			}
		}
		return authorities;
	}
	
	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	public String toString() {
		return username;
	}

}
