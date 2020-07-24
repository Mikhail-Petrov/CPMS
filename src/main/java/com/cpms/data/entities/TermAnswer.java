package com.cpms.data.entities;

import java.math.BigInteger;

public class TermAnswer implements Comparable<TermAnswer> {
	private String preferabletext;
	private int N_new;
	private int N_old;
	private long term_id;
	
	private double val = 0;
	
	public TermAnswer() {
		N_new = 0;
		N_old = 0;
	}
	
	public TermAnswer(Object[] o) {
		preferabletext = (String) o[0];
		N_new = (int) o[1];
		N_old = (int) o[2];
		term_id = ((BigInteger) o[3]).longValue();
	}
	public void calcVal(double old_docs, double new_docs, double sensitivity) {
		double nNew = (double) N_new, nOld = (double) N_old;
		if (nOld == 0) nOld = sensitivity;
		setVal(old_docs * nNew / nOld / new_docs);
	}
	public String getPreferabletext() {
		return preferabletext;
	}
	public void setPreferabletext(String preferabletext) {
		this.preferabletext = preferabletext;
	}
	public int getN_new() {
		return N_new;
	}
	public void setN_new(int n_new) {
		N_new = n_new;
	}
	public int getN_old() {
		return N_old;
	}
	public void setN_old(int n_old) {
		N_old = n_old;
	}
	public long getTerm_id() {
		return term_id;
	}
	public void setTerm_id(long term_id) {
		this.term_id = term_id;
	}
	@Override
	public int compareTo(TermAnswer o) {
		return -Double.compare(getVal(), o.getVal());
	}
	public double getVal() {
		return val;
	}
	public void setVal(double val) {
		this.val = val;
	}
}
