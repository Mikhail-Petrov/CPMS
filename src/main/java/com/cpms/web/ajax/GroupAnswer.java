package com.cpms.web.ajax;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Language;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Proofreading;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TaskCenter;
import com.cpms.data.entities.TaskRequirement;
import com.cpms.facade.ICPMSFacade;
import com.cpms.security.entities.Users;
import com.cpms.web.Proofreader;

/**
 * AJAX answer that returns information about a certain skill.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class GroupAnswer implements IAjaxAnswer {

	private List<List<Proofreader>> solutions;
	private Map<Long, Profile> profMap;
	private List<String> groupNames;
	private boolean success;
	private String res;

	public GroupAnswer() {
		this(false);
	}
	public GroupAnswer(String res) {
		setRes(res);
		setSuccess(false);
	}
	public GroupAnswer(boolean success) {
		solutions = new ArrayList<>();
		setGroupNames(new ArrayList<>());
		this.setSuccess(success);
	}
	public GroupAnswer(ICPMSFacade facade, IUserDAO userDAO, Task task) {
		solutions = new ArrayList<>();
		setGroupNames(new ArrayList<>());
		profMap = new HashMap<>();
		//int curSize = 1, curInd = 0;
		List<Profile> profiles = facade.getProfileDAO().getAll();
		String[] names = {"Most effective group", "Most available group", "Experienced group", "Group for learning"};
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
		/*String[] langCodes = task.getTarget().split(";");
		List<Language> targets = new ArrayList<>(), langs = facade.getLanguageDAO().getAll();
		for (int i = 0; i < langCodes.length; i++) {
			targets.add(Language.findByCode(langCodes[i], langs));
			covered.add(0);
		}*/
		List<TaskRequirement> reqs = new ArrayList<>();
		for (TaskRequirement req : task.getRequirements()) {
			reqs.add(req);
			covered.add(0);
		}
		List<Proofreader> all = new ArrayList<>();
		
		// create competency matrix
		List<List<Proofreader>> matrix = new ArrayList<>();
		for (int i = 0; i < reqs.size(); i++)
			matrix.add(new ArrayList<>());
		
		while (!profiles.isEmpty()) {
			// expert assessment
			boolean isGood = true;
			for (TaskRequirement req : task.getRequirements()) {
				for (Competency comp : profiles.get(curIndex).getCompetencies())
					if (comp.getSkill().getId() == req.getSkill().getId()){
						isGood = true;
						break;
					}
				if (isGood)
					break;
			}
			
			Users user = null;
			if (isGood) {
				user = userDAO.getByProfile(profiles.get(curIndex));
				if (user == null)
					isGood = false;
			}
			if (!isGood) {
				profiles.remove(curIndex);
				continue;
			}
			
			Set<TaskCenter> tasks = user.getTasks();
			
			// competency cover check
			for (int i = 0; i < reqs.size(); i++) {
				if (profiles.get(curIndex).hasCompetency(reqs.get(i).getSkill(), reqs.get(i).getLevel())) {
					covered.set(i, covered.get(i) + 1);
					matrix.get(i).add(new Proofreader(all.size(), tasks, null));
				}
			}
			
			// need to add
			Proofreader toAdd = new Proofreader(profiles.get(curIndex), facade, user);
			all.add(toAdd);
			profMap.put(toAdd.getId(), profiles.get(curIndex));
			profiles.remove(curIndex);
		}
		
		// if some target language is not covered than there is no solution
		for (Integer cov : covered)
			if (cov == 0) return;
		
		solutions.add(new ArrayList<>(all));
		groupNames.add(names[0]);
		solutions.add(new ArrayList<>(all));
		groupNames.add(names[1]);
		// remove extra experts
		double[] bestCoefs = {0,0,1,1,1,1}, worseCoefs = {1,1,0,0,0,0,0}, allCoefs = {1,1,1,1,1,1};
		optimizeGroup(0, bestCoefs, covered, reqs);
		optimizeGroup(1, worseCoefs, covered, reqs);

		solutions.add(new ArrayList<>());
		solutions.add(new ArrayList<>());
		groupNames.add(names[2]);
		groupNames.add(names[3]);
		List<Boolean> added1 = new ArrayList<>(), added2 = new ArrayList<>();
		for (int i = 0; i < all.size(); i++) {
			added1.add(false);
			added2.add(false);
		}
		for (List<Proofreader> line : matrix) {
			if (line.isEmpty()) {
				solutions.get(2).clear();
				solutions.get(3).clear();
				break;
			}
			int bestIndex1 = 0, bestIndex2 = 0;
			Date bestDate1 = line.get(0).getCompletedDate(), bestDate2 = bestDate1;
			for (int i = 1; i < line.size(); i++) {
				Date curDate = line.get(i).getCompletedDate();
				if (bestDate1 == null || curDate != null && curDate.after(bestDate1)) {
					bestIndex1 = i;
					bestDate1 = curDate;
				}
				if (bestDate2 != null && (curDate == null || curDate.before(bestDate2))) {
					bestIndex2 = i;
					bestDate2 = curDate;
				}
			}
			if (!added1.get(line.get(bestIndex1).getAllIndex())) {
				solutions.get(2).add(all.get(line.get(bestIndex1).getAllIndex()));
				added1.set(line.get(bestIndex1).getAllIndex(), true);
			}
			if (!added1.get(line.get(bestIndex2).getAllIndex())) {
				solutions.get(3).add(all.get(line.get(bestIndex2).getAllIndex()));
				added1.set(line.get(bestIndex2).getAllIndex(), true);
			}
		}
		optimizeGroup(2, allCoefs, null, reqs);
		optimizeGroup(3, allCoefs, null, reqs);
		
		for (int i = solutions.size() - 1; i >= 0; i--)
			if (solutions.get(i).isEmpty()) {
				solutions.remove(i);
				groupNames.remove(i);
			}
	}
	
	private void optimizeGroup(int groupIndex, double[] coefs, List<Integer> covered, List<TaskRequirement> reqs) {
		ArrayList<Integer> covers;
		if (covered == null) {
			covers = new ArrayList<>();
			for (int i = 0; i < reqs.size(); i++)
				covers.add(0);
			for (int i = 0; i < reqs.size(); i++) {
				for (Proofreader expert : solutions.get(groupIndex)) {
					if (!profMap.containsKey(expert.getId()))
						continue;
					if (profMap.get(expert.getId()).hasCompetency(reqs.get(i).getSkill(), reqs.get(i).getLevel())) {
						covers.set(i, covers.get(i) + 1);
						break;
					}
				}
			}
		} else covers = new ArrayList<>(covered);
		// find extra experts
		while (true) {
			List<Proofreader> toRemove = new ArrayList<>();
			for (Proofreader expert : solutions.get(groupIndex)) {
				boolean remove = true;
				if (profMap.containsKey(expert.getId()))
				for (int i = 0; i < covers.size(); i++) {
					if (covers.get(i) == 1 && profMap.get(expert.getId()).hasCompetency(
							reqs.get(i).getSkill(), reqs.get(i).getLevel())) {
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
			for (int i = 0; i < reqs.size(); i++)
				if (profMap.containsKey(toRemove.get(worstIndex).getId()) &&
						profMap.get(toRemove.get(worstIndex).getId()).hasCompetency(reqs.get(i).getSkill(), reqs.get(i).getLevel()))
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
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getRes() {
		return res;
	}
	public void setRes(String res) {
		this.res = res;
	}
	
}
