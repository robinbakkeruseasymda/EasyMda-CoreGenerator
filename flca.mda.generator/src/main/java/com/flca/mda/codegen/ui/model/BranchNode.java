package com.flca.mda.codegen.ui.model;

import java.util.ArrayList;
import java.util.List;

public class BranchNode extends ListNode
{
	public BranchNode(String label, int aParentId)
	{
		super(label);
		setParentId(aParentId);
	}

	List<TemplateNode> templates;

	public List<TemplateNode> getTemplates()
	{
		return templates;
	}

	public void setTemplates(List<TemplateNode> templates)
	{
		this.templates = templates;
	}
	
	public void addTemplate(TemplateNode aTemplate)
	{
		if (templates == null) {
			templates = new ArrayList<TemplateNode>();
		}
		templates.add(aTemplate);
	}
}
