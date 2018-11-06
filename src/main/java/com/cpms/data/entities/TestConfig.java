package com.cpms.data.entities;

import com.cpms.web.controllers.Viewer;

public class TestConfig {

	private int testIteration;
	private int testLength;
	private int genReqAmount;
	private int genResAmount;
	
	public TestConfig() {
		this.testIteration = Viewer.testIteration;
		this.testLength = Viewer.testLength;
		this.genReqAmount = Viewer.genReqAmount;
		this.genResAmount = Viewer.genResAmount;
	}
	
	public void updateConfigs() {
		Viewer.testIteration = this.testIteration;
		Viewer.testLength = this.testLength;
		Viewer.genReqAmount = this.genReqAmount;
		Viewer.genResAmount = this.genResAmount;
	}
	public int getTestIteration() {
		return testIteration;
	}
	public void setTestIteration(int testIteration) {
		this.testIteration = testIteration;
	}
	public int getTestLength() {
		return testLength;
	}
	public void setTestLength(int testLength) {
		this.testLength = testLength;
	}
	public int getGenReqAmount() {
		return genReqAmount;
	}
	public void setGenReqAmount(int genReqAmount) {
		this.genReqAmount = genReqAmount;
	}
	public int getGenResAmount() {
		return genResAmount;
	}
	public void setGenResAmount(int genResAmount) {
		this.genResAmount = genResAmount;
	}
}
