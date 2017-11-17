package com.cpms.web;

import java.util.ArrayList;
import java.util.List;

/**
 * Form user by administrator's applications interface for faster and more
 * user friendly validation. Stores nodes which represent application id and
 * required action.
 * 
 * @see ApplicationsPostFormNode
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class ApplicationsPostForm {
	
	private List<ApplicationsPostFormNode> nodes;
	
	public ApplicationsPostForm() {
		nodes = new ArrayList<ApplicationsPostFormNode>();
	}

	public List<ApplicationsPostFormNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<ApplicationsPostFormNode> nodes) {
		this.nodes = nodes;
	}

}
