package com.flca.mda.codegen.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.flca.mda.codegen.engine.data.CartridgeClasspathData;
import com.flca.mda.codegen.helpers.CartridgeHelper;
import com.flca.mda.codegen.ui.model.TemplateFolderLabelProvider;
import com.flca.mda.codegen.ui.model.TemplateFoldersContentProvider;

public class SelectTemplateFoldersPage extends WizardPage {
	private CheckboxTableViewer tv;

	protected SelectTemplateFoldersPage(String pageName) {
		super(pageName);
		setTitle("Template folders");
		setDescription("Please add classpath(s) to project(s) with jet templates");
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout gridLayout = new GridLayout(1, false);
		composite.setLayout(gridLayout);
		tv = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		createColumns(tv);
		tv.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		tv.setContentProvider(new TemplateFoldersContentProvider());
		tv.setLabelProvider(new TemplateFolderLabelProvider());
		tv.setInput(getCartridgeNames());
		tv.addCheckStateListener(new TblListener());

		for (CartridgeClasspathData data : CartridgeHelper.getInstance().getAllCartridgeClassPaths()) {
			tv.setGrayed(data.getKey(), !data.getFile().exists());
			tv.setChecked(data.getKey(), data.isEnabled());
		}

		setControl(composite);
	}

	private Set<String> getCartridgeNames() {
		Set<String> result = new HashSet<String>();
		
		for (CartridgeClasspathData data : CartridgeHelper.getInstance().getAllCartridgeClassPaths()) {
			result.add(data.getKey());
		}
			
		return result;
	}
	
	@Override
	public boolean canFlipToNextPage() {
		int n = 0;
		for (CartridgeClasspathData data : CartridgeHelper.getInstance().getAllCartridgeClassPaths()) {
			if (data.isEnabled()) {
				n++;
			}
		}
		return n == 1;
	}

	// This will create the columns for the table
	private void createColumns(TableViewer viewer) {
		String[] titles = { "folder" };
		int[] bounds = { 600 };

		for (int i = 0; i < titles.length; i++) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.FULL_SELECTION, i);
			column.getColumn().setText(titles[i]);
			column.getColumn().setWidth(bounds[i]);
			column.getColumn().setResizable(false);
			column.getColumn().setMoveable(false);
		}
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}
	
	
	private void disableOthers(String path) {
		for (int i=0; i < getTableRowCount(); i++) {
			Object o = tv.getElementAt(i);
			System.out.println(o);
			if (!path.equals(o)) {
				tv.setChecked(o, false);
			}
		}

		for (CartridgeClasspathData data : CartridgeHelper.getInstance().getAllCartridgeClassPaths()) {
			if (!data.getKey().equals(path)) {
				data.setEnabled(false);
			}
		}

	}
	
	private int getTableRowCount() {
		return CartridgeHelper.getInstance().getAllCartridgeClassPaths().size();
	}

	// --- event listeners
	private class TblListener implements ICheckStateListener {
		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			String path = event.getElement().toString();
			CartridgeHelper.getInstance().setCartridgeEnableStatus(path, event.getChecked());
			if (event.getChecked()) {
				disableOthers(path);
			}
			setPageComplete(canFlipToNextPage());
		}
	}

}
