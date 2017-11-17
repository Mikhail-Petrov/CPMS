package com.cpms.web;

/**
 * Element of {@link ApplicationsPostFormNode} for storing evidence.
 * 
 * @see ApplicationsPostFormNode
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class ApplicationsPostFormNodeEvidence {

	private long evidenceId = 0;
	private boolean approved = false;
	
	public ApplicationsPostFormNodeEvidence() {}
	
	public ApplicationsPostFormNodeEvidence(long evidenceId, boolean approved) {
		this.evidenceId = evidenceId;
		this.approved = approved;
	}

	public long getEvidenceId() {
		return evidenceId;
	}

	public void setEvidenceId(long evidenceId) {
		this.evidenceId = evidenceId;
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}
	
}
