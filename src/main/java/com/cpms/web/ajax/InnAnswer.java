package com.cpms.web.ajax;

import java.util.ArrayList;
import java.util.List;

import com.cpms.data.entities.Term;
import com.cpms.data.entities.TermVariant;

public class InnAnswer implements IAjaxAnswer {

	private List<List<String>> variants;
	private List<String> terms;
	private List<String> flags;
	private List<Integer> kids;
	private List<Long> ids;
	
	private long id;

	public InnAnswer() {
		setVariants(new ArrayList<>());
		setTerms(new ArrayList<>());
		setFlags(new ArrayList<>());
		setIds(new ArrayList<>());
		setKids(new ArrayList<>());
		setId(0);
	}
	
	public void addVariant(TermVariant var) {
		ids.add(var.getId());
		terms.add(var.getTerm().getId() + "");
		flags.add(var.getText());
	}
	
	public void addTerm(Term term) {
		terms.add(term.getPref());
		//flags.add(term.isInn());
		flags.add(term.getCategory());
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

	public List<Integer> getKids() {
		return kids;
	}

	public void setKids(List<Integer> kids) {
		this.kids = kids;
	}
	
}
