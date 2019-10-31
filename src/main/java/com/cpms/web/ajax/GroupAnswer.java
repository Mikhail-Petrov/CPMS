package com.cpms.web.ajax;

import java.util.ArrayList;
import java.util.List;

import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Task;
import com.cpms.facade.ICPMSFacade;
import com.cpms.web.Proofreader;

/**
 * AJAX answer that returns information about a certain skill.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class GroupAnswer implements IAjaxAnswer {

	private List<List<Proofreader>> solutions;
	private List<String> groupNames;

	public GroupAnswer() {
		solutions = new ArrayList<>();
		setGroupNames(new ArrayList<>());
	}
	public GroupAnswer(ICPMSFacade facade, IUserDAO userDAO, Task task) {
		solutions = new ArrayList<>();
		setGroupNames(new ArrayList<>());
		int curSize = 1, curInd = 0;
		List<Profile> all = facade.getProfileDAO().getAll();
		String[] names = {"Most effective group", "Most available group", "Group for learning"};
		while ((curInd + curSize) <= all.size()) {
			List<Proofreader> curSolution = new ArrayList<>();
			for (int i = 0; i < curSize; i++) {
				curSolution.add(new Proofreader(all.get(curInd++), facade, userDAO));
			}
			int index = (curSize < names.length ? curSize : names.length) - 1;
			groupNames.add(names[index]);
			curSize++;
			solutions.add(curSolution);
		}
	}

	public List<List<Proofreader>> getSolutions() {
		return solutions;
	}

	public void setSolutions(List<List<Proofreader>> solutions) {
		this.solutions = solutions;
	}

	public List<String> getGroupNames() {
		return groupNames;
	}

	public void setGroupNames(List<String> groupNames) {
		this.groupNames = groupNames;
	}
	
}
