package com.flca.mda.codegen.ui;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.flca.mda.codegen.helpers.CartridgeHelper;
import com.flca.mda.codegen.ui.model.BranchNode;
import com.flca.mda.codegen.ui.model.ListNode;
import com.flca.mda.codegen.ui.model.TemplateLabelProvider;
import com.flca.mda.codegen.ui.model.TemplateNode;
import com.flca.mda.codegen.ui.model.TemplatesContentProvider;
import com.flca.mda.codegen.ui.model.TemplatesProvider;

public class SelectTemplatesPage extends WizardPage {
	private CheckboxTableViewer ctv1;
	private CheckboxTableViewer ctv2;

	public SelectTemplatesPage(String pageName) {
		super(pageName);
		setTitle("Templates");
		setDescription("Please select one on more template(s)");
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout gridLayout = new GridLayout(2, false);
		composite.setLayout(gridLayout);

		new Label(composite, SWT.NULL).setText("Template tree's and branches");
		new Label(composite, SWT.NULL).setText("Templates");

		ctv1 = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		createColumns(ctv1);
		ctv1.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		ctv1.setContentProvider(new TemplatesContentProvider());
		ctv1.setLabelProvider(new TemplateLabelProvider());
		ctv1.addCheckStateListener(new Table1Listener());

		ctv2 = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		createColumns(ctv2);
		ctv2.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		ctv2.setLabelProvider(new TemplateLabelProvider());
		ctv2.setContentProvider(new TemplatesContentProvider());
		ctv2.addCheckStateListener(new Table2Listener());

		setControl(composite);
	}

	public void update() {
		ctv1.setInput(TemplatesProvider.getInstance().getTemplateBranches());
	}

	// This will create the columns for the table
	private void createColumns(TableViewer viewer) {

		String[] titles = { "", "name" }; // note column 0 is used for color
		int[] bounds = { 20, 200 };

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

	@Override
	public boolean canFlipToNextPage() {
		return CartridgeHelper.getInstance().getSelectedTemplates().size() > 0;
	}

	private void handleBranchNodeEvent(BranchNode node, boolean selected) {
		TemplatesProvider.getInstance().updateTemplates(node.getId(), selected);
		ctv2.setInput(TemplatesProvider.getInstance().getTemplates());
	}

	private void handleTemplateNodeEvent(TemplateNode node, boolean selected) {
		CartridgeHelper.getInstance().updateSelectedTemplate(node.getTemplate(), selected);
		getWizard().getContainer().updateButtons();
	}

	private void handleBranchNode2Event(BranchNode bnode, boolean selected) {
		for (int i = 0; i < ctv2.getTable().getItemCount(); i++) {
			ListNode item = (ListNode) ctv2.getElementAt(i);
			if (item instanceof TemplateNode && item.getParentId() == bnode.getId()) {
				ctv2.setChecked(item, selected);
				handleTemplateNodeEvent((TemplateNode) item, selected);
			}
		}
		getWizard().getContainer().updateButtons();
	}

	// --- event listeners
	private class Table1Listener implements ICheckStateListener {
		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			ListNode node = (ListNode) event.getElement();
			if (node instanceof BranchNode) {
				handleBranchNodeEvent((BranchNode) node, event.getChecked());
			}
		}
	}

	private class Table2Listener implements ICheckStateListener {
		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			ListNode node = (ListNode) event.getElement();
			if (node instanceof TemplateNode) {
				handleTemplateNodeEvent((TemplateNode) node, event.getChecked());
			} else if (node instanceof BranchNode) {
				handleBranchNode2Event((BranchNode) node, event.getChecked());
			}
		}
	}
}
