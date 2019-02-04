package com.cpms.web;

import java.util.ArrayList;
import java.util.List;

import com.cpms.data.entities.Motivation;
import com.cpms.data.entities.Skill;
import com.cpms.web.controllers.Skills;

/**
 * Static utility class which holds utility boilerplate code for skills.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public abstract class MotivationUtils {
	
	private static String INDENT_SINGULAR = "--";
	private static String INDENT_SEPARATOR = " ";
	
	/**
	 * Sorts a list of skills and indents their titles to look better in web app
	 * in "select" elements.
	 * 
	 * @param unsorted list of skills to be sorted
	 * @return sorted list of skills with indents added to titles
	 */
	public static List<Motivation> sortAndAddIndents(List<Motivation> unsorted) {
		List<Motivation> sorted = new ArrayList<Motivation>();
		unsorted.forEach(x -> {
			if (x.getParent() == null) {
				dfs(x, sorted, unsorted, 0);
			}
		});
		return sorted;
	}
	
	/**
	 * Uses depth first search to find children skill of a parent that are in
	 * the list.
	 * 
	 * @param parent parent skill
	 * @param sorted already sorted skills
	 * @param unsorted source skills list
	 * @param depth depth of search (how many times to indent)
	 */
	private static void dfs(Motivation parent, List<Motivation> sorted, 
				List<Motivation> unsorted, long depth) {
			if (unsorted.contains(parent)) {
				Motivation parentClone = new Motivation(parent);
				parentClone.setId(parent.getId());
				parentClone
					.setName(getIndents(depth) + parent.getPresentationName());
				sorted.add(parentClone);
				parent.getChildrenSorted()
					.forEach(x -> dfs(x, sorted, unsorted, depth + 1));
		}
	}
	
	/**
	 * Returns string of indents of selected depth.
	 * 
	 * @param depth number of indents
	 * @return string with selected number of indents
	 */
	private static String getIndents(long depth) {
		StringBuilder builder = new StringBuilder("");
		for (long i = 0; i < depth; i++) {
			builder.append(INDENT_SINGULAR);
			builder.append(INDENT_SEPARATOR);
		}
		return builder.toString();
	}

}
