package com.cpms.web;

import java.util.ArrayList;
import java.util.List;

import com.cpms.dao.interfaces.IDraftableSkillDaoExtension;
import com.cpms.data.entities.Skill;
import com.cpms.web.controllers.Skills;

/**
 * Static utility class which holds utility boilerplate code for skills.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public abstract class SkillUtils {
	
	private static String INDENT_SINGULAR = "--";
	private static String INDENT_SEPARATOR = " ";
	
	/**
	 * Sorts a list of skills and indents their titles to look better in web app
	 * in "select" elements.
	 * 
	 * @param unsorted list of skills to be sorted
	 * @return sorted list of skills with indents added to titles
	 */

	public static List<Skill> sortAndAddIndents(List<Skill> unsorted, IDraftableSkillDaoExtension skillDao) {
		List<Skill> sorted = new ArrayList<Skill>();
		unsorted.forEach(x -> {
			if (x.getParent() == null) {
				dfs(x, sorted, unsorted, 0, skillDao);
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
	private static void dfs(Skill parent, List<Skill> sorted, 
				List<Skill> unsorted, long depth, IDraftableSkillDaoExtension skillDao) {
			if (unsorted.contains(parent)) {
				Skill parentClone = new Skill();
				parentClone.setId(parent.getId());
				parentClone
					.setName(getIndents(depth) + parent.getPresentationName());
				sorted.add(parentClone);
				parent.getChildrenSorted(skillDao)
					.forEach(x -> dfs(x, sorted, unsorted, depth + 1, skillDao));
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
