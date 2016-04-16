package com.flca.mda.codegen.ui.model;

public class ListNode
{
	private String label;
	private Boolean selected;
	private int id;
	private int parentId;
	
	private static int CURRENT_ID = 1;
	
	public ListNode(String label)
	{
		super();
		this.label = label;
		this.id = CURRENT_ID++;
		this.selected = false;
		this.parentId = -1;
	}
	
	public String getLabel()
	{
		return label;
	}
	public void setLabel(String label)
	{
		this.label = label;
	}
	public Boolean getSelected()
	{
		return selected;
	}
	public void setSelected(Boolean selected)
	{
		this.selected = selected;
	}
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public int getParentId()
	{
		return parentId;
	}
	public void setParentId(int parentId)
	{
		this.parentId = parentId;
	}
	
	
}
