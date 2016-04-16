package com.flca.mda.codegen.ui.model;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class TemplateLabelProvider extends StyledCellLabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		ListNode item = (ListNode) element;
		switch (columnIndex) {
		case 0:
			return "";
		case 1:
			if (item instanceof BranchNode) {
				return "  <" + item.getLabel() + ">";
			} else {
				return item.getLabel();
			}

		default:
			// throw new RuntimeException("Should not happen");
			return ("Should not happen");
		}
	}

	@Override
	public void update(ViewerCell cell) {
		Color c = new Color(Display.getCurrent(), 200, 200, 200);
		cell.setBackground(c);
		super.update(cell);
	}

}
