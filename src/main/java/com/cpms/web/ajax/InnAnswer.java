package com.cpms.web.ajax;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cpms.data.entities.Category;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.Task_Category;
import com.cpms.data.entities.Task_Trend;
import com.cpms.data.entities.Term;
import com.cpms.data.entities.TermVariant;
import com.cpms.data.entities.Trend;

public class InnAnswer implements IAjaxAnswer {

	private List<List<String>> variants;
	private List<String> terms;
	private List<String> flags;
	private List<Long> kids;
	private List<Long> ids;
	
	private List<Task> tasks;
	
	private long id;

	public InnAnswer() {
		setVariants(new ArrayList<>());
		setTerms(new ArrayList<>());
		setFlags(new ArrayList<>());
		setIds(new ArrayList<>());
		setKids(new ArrayList<>());
		setTasks(new ArrayList<>());
		setId(0);
	}
	
	public void addVariant(TermVariant var) {
		ids.add(var.getId());
		terms.add(var.getTerm().getId() + "");
		flags.add(var.getText());
	}
	
	public void addTerm(Term term, Map<Long, Task> tasks) {
		terms.add(term.getPref());
		String flag = "";
		Long kids = 0L;
		Task task = new Task();
		if (tasks.containsKey(term.getId())) {
			Task innTask = tasks.get(term.getId());
			kids = innTask.getId();
			task.setOriginal(innTask.getOriginal());
			task.setName(task.getName());
			task.setId(innTask.getId());
			task.setImpact(innTask.getImpact());
			// add trends and categories
			List<Long> categs = new ArrayList<>();
			flag = "tr";
			for (Task_Category tc : innTask.getCategories()) {
				Category cat = new Category();
				cat.setId(tc.getCategory().getId());
				cat.setName(tc.getCategory().getName());
				task.addCategory(new Task_Category(cat, null));
				if (!categs.contains(cat.getId())) {
					categs.add(cat.getId());
					if (tc.getCategory().getParent() != null)
						categs.add(tc.getCategory().getParent().getId());
				}
			}
			for (Long catId : categs)
				flag += "id" + catId + "id";
			for (Task_Category tc : task.getCategories())
				tc.setTask(null);
			for (Task_Trend tt : innTask.getTrends()) {
				Trend tr = new Trend();
				tr.setId(tt.getTrend().getId());
				tr.setName(tt.getTrend().getName());
				task.addTrend(new Task_Trend(tr, null));
			}
			for (Task_Trend tt : task.getTrends())
				tt.setTask(null);
		}
		this.getTasks().add(task);
		flags.add(flag);
		this.kids.add(kids);
		//flags.add(term.getCategory());
		ids.add(term.getId());
		List<String> vars = new ArrayList<>();
		for (TermVariant var : term.getVariants())
			vars.add(var.getPresentationName());
		variants.add(vars);
	}
	
	public List<List<String>> getVariants() {
		return variants;
	}
	public void setVariants(List<List<String>> variants) {
		this.variants = variants;
	}
	public List<String> getTerms() {
		return terms;
	}
	public void setTerms(List<String> terms) {
		this.terms = terms;
	}

	public List<String> getFlags() {
		return flags;
	}

	public void setFlags(List<String> flags) {
		this.flags = flags;
	}

	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public List<Long> getKids() {
		return kids;
	}

	public void setKids(List<Long> kids) {
		this.kids = kids;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
	
}
