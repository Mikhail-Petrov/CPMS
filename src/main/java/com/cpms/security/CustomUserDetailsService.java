package com.cpms.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.security.entities.Users;

/**
 * Connector of {@link IUserDAO} that allows Spring Security to use it
 * to validate users.
 * 
 * @see IUserDAO
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Component
public class CustomUserDetailsService implements UserDetailsService {
	
	@Autowired
	@Qualifier("userDAO")
	private IUserDAO userDAO;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Users user = userDAO.getByUsername(username);
        if(user == null){
            throw new UsernameNotFoundException("UserName " + username +
            		" not found");
        }
        return new SecurityUser(user);
	}
	
	
}
