package com.flca.mda.codegen.ui.model;

import java.util.ArrayList;
import java.util.List;

public class TreeNode extends ListNode
{
	public TreeNode(String label)
	{
		super(label);
	}

	List<BranchNode> branches = new ArrayList<BranchNode>();

	public List<BranchNode> getBranches()
	{
		return branches;
	}

	public void setBranches(List<BranchNode> branches)
	{
		this.branches = branches;
	}
	
	public void addTemplate(BranchNode aTemplate)
	{
		if (branches == null) {
			branches = new ArrayList<BranchNode>();
		}
		branches.add(aTemplate);
	}
}
