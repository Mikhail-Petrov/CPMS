package com.cpms.dao.interfaces;

import java.util.List;

import com.cpms.data.entities.Profile;
import com.cpms.exceptions.DataAccessException;
import com.cpms.security.entities.Users;

/**
 * Interface for CRUD operations with User entities.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public interface IUserDAO {
	
	/**
	 * Finds resident User by profile associated with it.
	 * 
	 * @param profile resident's profile
	 * @return user entity representing profile's owner
	 */
	public Users getByProfile(Profile profile);

	/**
	 * Finds user by his username. Useful when working with Principal object.
	 * 
	 * @param username user's username
	 * @return first user found with this username
	 */
	public Users getByUsername(String username);

	/**
	 * Finds user by his user id. Useful when working with Principal object.
	 * 
	 * @param userId user's id
	 * @return user found with this userId
	 */
	public Users getByUserID(Long userId);
	
	/**
	 * Counts all users saved.
	 * 
	 * @return number of users saved
	 */
	public long count();
	
	/**
	 * Finds all users.
	 * 
	 * @return list containing all users
	 */
	public List<Users> getAll();
	
	/**
	 * Creates a new user. 
	 * 
	 * @throws DataAccessException if such user already exists.
	 * 
	 * @param user user to insert
	 */
	public void insertUser(Users user);
	
	/**
	 * Updates existing user.
	 * 
	 * @throws DataAccessException if such user does not exist.
	 * 
	 * @param user user to update
	 */
	public void updateUser(Users user);
	
	/**
	 * Deletes existing user.
	 * 
	 * @param user user to delete
	 */
	public void deleteUser(Users user);
	
}
