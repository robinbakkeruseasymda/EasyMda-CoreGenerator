package com.flca.mda.codegen.ui.model;

import flca.mda.codegen.data.ITemplate;

public class TemplateNode extends ListNode {
	public TemplateNode(String label, int aParentId, ITemplate aTemplate) {
		super(label);
		setParentId(aParentId);
		setTemplate(aTemplate);
	}

	private ITemplate template;

	public void setTemplate(ITemplate template) {
		this.template = template;
	}

	public ITemplate getTemplate() {
		return template;
	}

	@Override
	public String toString() {
		return template.getName();
	}

}
