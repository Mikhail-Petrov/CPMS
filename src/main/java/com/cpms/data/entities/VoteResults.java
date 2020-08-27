package com.cpms.data.entities;

public class VoteResults implements Comparable<VoteResults> {

	private String name;
	private int value, percent = 0;
	
	@Override
	public int compareTo(VoteResults o) {
		return Integer.compare(o.getValue(), getValue());
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public int getPercent() {
		return percent;
	}
	public void setPercent(int percent) {
		this.percent = percent;
	}
	
}
