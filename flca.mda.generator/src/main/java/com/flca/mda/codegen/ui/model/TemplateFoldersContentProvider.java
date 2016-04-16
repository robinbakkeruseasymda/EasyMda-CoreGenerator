package com.flca.mda.codegen.ui.model;

import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TemplateFoldersContentProvider implements IStructuredContentProvider
{

	@Override
	public Object[] getElements(Object inputElement)
	{
		@SuppressWarnings("unchecked")
		Set<String> folders = (Set<String>) inputElement;
		return folders.toArray();
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

}
