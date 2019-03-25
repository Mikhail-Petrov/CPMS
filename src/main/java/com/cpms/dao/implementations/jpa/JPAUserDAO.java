package com.cpms.dao.implementations.jpa;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.implementations.jpa.repositories.security.RoleRepository;
import com.cpms.dao.implementations.jpa.repositories.security.UserRepository;
import com.cpms.dao.implementations.jpa.repositories.system.MessageCenterRepository;
import com.cpms.dao.interfaces.ICleanable;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Profile;
import com.cpms.exceptions.DataAccessException;
import com.cpms.security.entities.Role;
import com.cpms.security.entities.User;

/**
 * Service for CRUD operations with user entity.
 * 
 * @see IUserDAO
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Service
@Transactional("transactionManager")
public class JPAUserDAO implements IUserDAO, ICleanable {

	private UserRepository userRepository;
	private RoleRepository roleRepository;
	private MessageCenterRepository messageRepository;
	
	@Autowired
	@Qualifier(value = "User")
	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	@Autowired
	@Qualifier(value = "Role")
	public void setRoleRepository(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}
	
	@Autowired
	@Qualifier(value = "MessageCenter")
	public void setMessageRepository(MessageCenterRepository messageRepository) {
		this.messageRepository = messageRepository;
	}
	
	@Override
	public void cleanAndReset() {
		userRepository.deleteAll();
		roleRepository.deleteAll();
		messageRepository.deleteAll();
	}

	@Override
	public User getByUsername(final String username) {
		if (username == null) {
			throw new DataAccessException("Null value.", null);
		}
		User user = userRepository.retrieveUserByUsername(username)
				.stream().findFirst().orElse(null);
		return user;
	}

	@Override
	public void insertUser(User user) {
		if (user == null || user.getRoles() == null) {
			throw new DataAccessException("Null value.", null);
		}
		for (Role role : user.getRoles()) {
			if (role == null) {
				throw new DataAccessException("Null value.", null);
			}
		}
		if (userRepository.exists(user.getId())) {
			throw new DataAccessException("Cannot insert, such user already exists",
					null);
		}
		if (!user.isHashed()) {
			user.hashPassword();
		}
		userRepository.saveAndFlush(user);
	}

	@Override
	public void updateUser(User user) {
		if (user == null || user.getRoles() == null) {
			throw new DataAccessException("Null value.", null);
		}
		for (Role role : user.getRoles()) {
			if (role == null) {
				throw new DataAccessException("Null value.", null);
			}
		}
		if (!userRepository.exists(user.getId())) {
			throw new DataAccessException("Cannot update, such user doesn't exist",
					null);
		}
		if (!user.isHashed()) {
			user.hashPassword();
		}
		userRepository.saveAndFlush(user);
	}

	@Override
	public void deleteUser(User user) {
		if (user == null) {
			throw new DataAccessException("Attempt to delete null", null);
		}
		userRepository.delete(user);
	}

	@Override
	public User getByProfile(Profile profile) {
		if (profile == null) {
			throw new DataAccessException("Null value.");
		}
		if (profile.getId() <= 0) {
			throw new DataAccessException("This is not a persisted profile.");
		}
		return userRepository.retrieveUserByProfileId(profile.getId());
	}

	@Override
	public long count() {
		return userRepository.count();
	}

	@Override
	public List<User> getAll() {
		return userRepository.findAll();
	}

	@Override
	public User getByUserID(Long userId) {
		if (userId == null) {
			throw new DataAccessException("Null value.");
		}
		if (userId <= 0) {
			throw new DataAccessException("This is not a persisted user.");
		}
		return userRepository.retrieveUserByUserId(userId);
	}
	
}
