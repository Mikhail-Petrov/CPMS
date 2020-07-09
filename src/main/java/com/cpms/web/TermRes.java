package com.cpms.web;

import com.cpms.data.entities.TermVariant;

public class TermRes implements Comparable<TermRes> {

	private double value;
	private TermVariant term;
	
	public TermRes() {}
	
	public TermRes(TermVariant term, double value) {
		this.setTerm(term);
		this.setValue(value);
	}
	@Override
	public int compareTo(TermRes o) {
		return -Double.compare(getValue(), o.getValue());
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public TermVariant getTerm() {
		return term;
	}

	public void setTerm(TermVariant term) {
		this.term = term;
	}

}
