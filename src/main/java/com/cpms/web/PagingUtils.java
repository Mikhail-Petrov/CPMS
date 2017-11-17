package com.cpms.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;

import com.cpms.dao.interfaces.IDAO;
import com.cpms.data.AbstractDomainObject;
import com.cpms.data.DomainObject;
import com.cpms.exceptions.WrongPageException;

/**
 * Static utility class which holds pagination boilerplate code.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public final class PagingUtils {
	
	public static final int PAGE_SIZE = 10;

	private PagingUtils() {}
	
	/**
	 * Uses {@link IDAO} to prepare a single page of objects.
	 * 
	 * @param page page number, starting from 0
	 * @param dao {@link IDAO} implementation to get objects from
	 * @param type entity class of objects paginated
	 * @param pagingAddress address that should be placed on "Next Page",
	 * "Previous Page" and "Search" buttons
	 * @param model active Spring model instance
	 * @param request active HttpServletRequest instance
	 * @param useSearch true if search bar needs to be displayed
	 * @param search search request
	 * @param objectAddress address that should be placed on links placed on
	 * each objects title
	 * @param title page title
	 * @param objectAddAddress address that should be placed on "Add new" button
	 * 
	 * @return string with a value of "page"
	 */
	public static <T extends AbstractDomainObject> String preparePageFromDao(
			Integer page,
			IDAO<T> dao,
			Class<? extends T> type,
			String pagingAddress,
			Model model,
			HttpServletRequest request,
			boolean useSearch,
			String search,
			String objectAddress,
			String title,
			String objectAddAddress) {
		long totalObjects = preparePageCount(dao, search, page, type);
		int totalPages = totalPages(PAGE_SIZE, totalObjects);
		if (totalPages == 0) {
			totalPages++;
		}
		if (page < 0 || page > totalPages) {
			throw new WrongPageException("Page " + page + " out of " + totalPages,
					request.getPathInfo());
		}
		model.addAttribute("objectsList", 
				preparePageObjects(dao, search, page, type));
		model.addAttribute("total", totalPages);
		model.addAttribute("current", page);
		model.addAttribute("address", pagingAddress);
		model.addAttribute("search", search);
		model.addAttribute("useSearch", useSearch);
		model.addAttribute("objectAddress", objectAddress);
		model.addAttribute("title", title);
		model.addAttribute("objectAddAddress", objectAddAddress);
		model.addAttribute("canAdd", true);
		model.addAttribute("backPath", "/");
		return "page";
	}
	
	/**
	 * Uses list of object to prepare a single page of objects.
	 * 
	 * @param page page number, starting from 0
	 * @param objectsCount number of objects in the collection
	 * @param objectsThisPage objects that should be displayed on current page
	 * @param pagingAddress address that should be placed on "Next Page",
	 * "Previous Page" and "Search" buttons
	 * @param model active Spring model instance
	 * @param request active HttpServletRequest instance
	 * @param objectAddress address that should be placed on links placed on
	 * each objects title
	 * @param title page title
	 * 
	 * @return string with a value of "page"
	 */
	public static String preparePageFromList(
			Integer page,
			int objectsCount,
			List<? extends DomainObject> objectsThisPage,
			String pagingAddress,
			Model model,
			HttpServletRequest request,
			String objectAddress,
			String title) {
		int totalPages = totalPages(PAGE_SIZE, objectsCount);
		if (totalPages == 0) {
			totalPages++;
		}
		if (page < 0 || page > totalPages) {
			throw new WrongPageException("Page " + page + " out of " + totalPages,
					request.getPathInfo());
		}
		model.addAttribute("objectsList", objectsThisPage);
		model.addAttribute("total", totalPages);
		model.addAttribute("current", page);
		model.addAttribute("address", pagingAddress);
		model.addAttribute("useSearch", false);
		model.addAttribute("objectAddress", objectAddress);
		model.addAttribute("title", title);
		model.addAttribute("canAdd", false);
		model.addAttribute("backPath", "/");
		return "page";
	}
	
	/**
	 * Counts, how many there are pages in total.
	 * 
	 * @param pageSize object per page
	 * @param count total amount of objects
	 * 
	 * @return total amount of pages (might return 0)
	 */
	private static int totalPages(int pageSize, long count) {
		return (int)(count / (long)pageSize + (count % pageSize > 0 ? 1 : 0));
	}
	
	/**
	 * Prepares a list of objects to be displayed on the page.
	 * 
	 * @param dao - {@link IDAO} implementation to be queried
	 * @param search search request (will be ignored if null or empty)
	 * @param page page to look up, size of page will be constant value PAGE_SIZE
	 * @param type entity class to look up
	 * @return
	 */
	private static <T extends AbstractDomainObject> List<T> preparePageObjects(
			IDAO<T> dao, 
			String search,
			int page,
			Class<? extends T> type) {
		List<T> pageObjects;
		if (search == null || search.equals("")) {
			pageObjects = dao
					.getRange((page - 1) * PAGE_SIZE, page * PAGE_SIZE);
		} else {
			pageObjects = dao
					.searchRange(search, type,
							(page - 1) * PAGE_SIZE, page * PAGE_SIZE);
		}
		return pageObjects;
	}
	
	/**
	 * Counts how many total objects are there in total.
	 * 
	 * @param dao {@link IDAO} implementation to be quried
	 * @param search search request
	 * @param page page number
	 * @param type entity class
	 * @return
	 */
	private static <T extends AbstractDomainObject> long preparePageCount(
			IDAO<T> dao, 
			String search,
			int page,
			Class<? extends T> type) {
		long count = 0;
		if (search == null || search.equals("")) {
			count = dao.count();
		} else {
			count = dao.searchCount(search, type);
		}
		return count;
	}
	
}
