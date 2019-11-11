package com.cpms.web.ajax;

import java.util.ArrayList;
import java.util.List;

import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Language;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Proofreading;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TaskRequirement;
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
		//int curSize = 1, curInd = 0;
		List<Profile> profiles = facade.getProfileDAO().getAll();
		String[] names = {"Most effective group", "Most available group", "Group for learning"};
		/*while ((curInd + curSize) <= all.size()) {
			List<Proofreader> curSolution = new ArrayList<>();
			for (int i = 0; i < curSize; i++) {
				curSolution.add(new Proofreader(all.get(curInd++), facade, userDAO));
			}
			int index = (curSize < names.length ? curSize : names.length) - 1;
			groupNames.add(names[index]);
			curSize++;
			solutions.add(curSolution);
		}*/
		int curIndex = 0;
		List<Integer> covered = new ArrayList<>();
		//for (TaskRequirement req : task.getRequirements())
			//covered.add(false);
		String[] langCodes = task.getTarget().split(";");
		List<Language> targets = new ArrayList<>(), langs = facade.getLanguageDAO().getAll();
		for (int i = 0; i < langCodes.length; i++) {
			targets.add(Language.findByCode(langCodes[i], langs));
			covered.add(0);
		}
		List<Proofreader> all = new ArrayList<>();
		while (!profiles.isEmpty()) {
			// expert assessment
			boolean isGood = true;
			for (TaskRequirement req : task.getRequirements()) {
				boolean match = false;
				for (Competency comp : profiles.get(curIndex).getCompetencies())
					if (comp.getSkill().getId() == req.getSkill().getId())
						match = true;
				if (!match) {
					isGood = false;
					break;
				}
			}
			if (isGood) {
				isGood = false;
				for (Language lan : targets) {
					for (Proofreading pr : profiles.get(curIndex).getProofs())
						if (pr.getTo().equals(lan)) {
							isGood = true;
							break;
						}
				}
			}
			if (!isGood) {
				profiles.remove(curIndex);
				continue;
			}
			
			// language cover check
			for (int i = 0; i < targets.size(); i++) {
				for (Proofreading pr : profiles.get(curIndex).getProofs())
					if (pr.getTo().equals(targets.get(i))) {
						covered.set(i, covered.get(i) + 1);
						break;
					}
			}
			
			// need to add
			Proofreader toAdd = new Proofreader(profiles.get(curIndex), facade, userDAO);
			all.add(toAdd);
			profiles.remove(curIndex);
		}
		
		solutions.add(new ArrayList<>(all));
		groupNames.add(names[0]);
		solutions.add(new ArrayList<>(all));
		groupNames.add(names[1]);
		// remove extra experts
		double[] bestCoefs = {0,0,1,1,1,1}, worseCoefs = {1,1,0,0,0,0,0};
		optimizeGroup(0, bestCoefs, covered, targets);
		optimizeGroup(1, worseCoefs, covered, targets);
	}
	
	private void optimizeGroup(int groupIndex, double[] coefs, List<Integer> covered, List<Language> targets) {
		ArrayList<Integer> covers = new ArrayList<>(covered);
		// find extra experts
		while (true) {
			List<Proofreader> toRemove = new ArrayList<>();
			for (Proofreader expert : solutions.get(groupIndex)) {
				boolean remove = true;
				for (int i = 0; i < covers.size(); i++) {
					if (covers.get(i) == 1 && expert.getTargets().contains(targets.get(i).getCode())) {
						remove = false;
						break;
					}
				}
				if (remove)
					toRemove.add(expert);
			}
			if (toRemove.isEmpty())
				break;
			// detect the worst one
			double worstVal= Double.MAX_VALUE;
			int worstIndex = 0;
			for (int i = 0; i < toRemove.size(); i++) {
				double optimality = toRemove.get(i).getOptimality(coefs);
				if (optimality < worstVal) {
					worstVal = optimality;
					worstIndex = i;
				}
			}
			// remove the worst
			for (int i = 0; i < targets.size(); i++)
				if (toRemove.get(worstIndex).getTargets().contains(targets.get(i).getCode()))
					covers.set(i, covers.get(i) - 1);
			solutions.get(groupIndex).remove(toRemove.get(worstIndex));
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
