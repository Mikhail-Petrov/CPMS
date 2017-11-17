package com.cpms.web;

import java.util.ArrayList;
import java.util.List;

/**
 * A single node of {@link ApplicationsPostForm}. Stored application's id and
 * required action, as well as connected evidence.
 * 
 * @see ApplicationsPostForm
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class ApplicationsPostFormNode {
	
	private long applicationId = 0;
	
	private ApplicationsPostFormNodeState state = ApplicationsPostFormNodeState.NONE;
	
	private List<ApplicationsPostFormNodeEvidence> evidence = new ArrayList<>();
	
	public ApplicationsPostFormNode(){}
	
	public ApplicationsPostFormNode(long applicationId,
			ApplicationsPostFormNodeState state) {
		this.applicationId = applicationId;
		this.state = state;
	}

	public long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(long applicationId) {
		this.applicationId = applicationId;
	}

	public ApplicationsPostFormNodeState getState() {
		return state;
	}

	public void setState(ApplicationsPostFormNodeState state) {
		this.state = state;
	}

	public List<ApplicationsPostFormNodeEvidence> getEvidence() {
		return evidence;
	}

	public void setEvidence(List<ApplicationsPostFormNodeEvidence> evidence) {
		this.evidence = evidence;
	}

}
