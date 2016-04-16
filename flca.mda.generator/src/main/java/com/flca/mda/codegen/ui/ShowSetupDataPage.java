package com.flca.mda.codegen.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.flca.mda.codegen.helpers.CartridgeHelper;

import flca.mda.codegen.data.DataStore;
import flca.mda.codegen.data.ITemplate;
import flca.mda.codegen.data.SubsValue;
import flca.mda.codegen.data.SubsValueType;

public class ShowSetupDataPage extends WizardPage {
	private Composite container;
	private Composite parent;
	private Collection<SubsValue> subsvalues;
	private List<CtrlData> controls;
	private Button saveSettingsCheckbox;

	protected ShowSetupDataPage(String pageName) {
		super(pageName);
		setTitle("SubsValues");
		setDescription("Please check if the subsvalues are correct");
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite aParent) {
		parent = aParent;
		// we start with an empty container which will be populated in the
		// update triggered after the second page
		container = new Composite(parent, SWT.NULL);
		GridLayout gridLayout = new GridLayout(4, false);
		container.setLayout(gridLayout);
		setControl(container);
	}

	/**
	 * here we add a cehckbox on a new grid row
	 */
	private void addSaveCheckbox() {
		int ndummy = (controls.size() % 2 == 0) ? 2 : 4;

		for (int i = 0; i < ndummy; i++) {
			new Label(container, SWT.NULL).setText("");
		}
		saveSettingsCheckbox = new Button(container, SWT.CHECK);
		saveSettingsCheckbox.setSelection(true);
		new Label(container, SWT.NULL).setText("Save these settings");
	}

	private void addSubstituteControls(SubsValue subsvalues[], Composite aComposite) {
		for (SubsValue subsval : subsvalues) {
			if (subsval.getType() != null && !subsval.getType().equals(SubsValueType.NONE)) {
				controls.add(new CtrlData(getShell(), aComposite, subsval));
			}
		}
	}

	/**
	 */
	public void update() {
		if (controls == null) {
			controls = new ArrayList<ShowSetupDataPage.CtrlData>();
			subsvalues = filterSubsValues(DataStore.getInstance().getSubsvalues());
			SubsValue values[] = (SubsValue[]) subsvalues.toArray(new SubsValue[subsvalues.size()]);
			Arrays.sort(values);
			addSubstituteControls(values, container);
			addSaveCheckbox();
			container.layout(true);
		}
		setPageComplete(inputOk());
	}

	private CtrlData findControl(SubsValue aSubsval) {
		for (CtrlData ctrl : controls) {
			if (ctrl.subsval.getName().equals(aSubsval.getName())) {
				return ctrl;
			}
		}
		return null;
	}

	@Override
	public boolean canFlipToNextPage() {
		return false;
	}

	// public boolean canFinish() {
	public boolean inputOk() {
		boolean result = true;

		for (Control c : container.getChildren()) {
			if (c instanceof Text) {
				Text txt = (Text) c;
				// System.out.println(txt.getMessage() + " = " + txt.getText());
				if (txt.getText() == null || txt.getText().trim().length() == 0) {
					result = false;
					break;
				}
			}
		}

		return result;
	}

	public boolean saveNeeded() {
		return saveSettingsCheckbox.getSelection();
	}

	public void saveSettings() {
		if (subsvalues != null && subsvalues.size() > 0) {
			for (SubsValue subsval : subsvalues) {
				CtrlData ctrl = findControl(subsval);
				if (ctrl != null) {
					if (SubsValueType.CHECKBOX.equals(subsval.getType())) {
						DataStore.getInstance().editSubstitute(subsval.getName(), ctrl.checkbox.getSelection());
					} else if (SubsValueType.COMBOBOX.equals(subsval.getType())) {
						DataStore.getInstance().editSubstitute(subsval.getName(), ctrl.combobox.getText().trim());
					} else {
						DataStore.getInstance().editSubstitute(subsval.getName(), ctrl.textbox.getText().trim());
					}
				}
			}
		}
	}

	private List<SubsValue> filterSubsValues(Collection<SubsValue> subsvalues) {
		List<SubsValue> result = new ArrayList<SubsValue>();

		Set<ITemplate> selctemplates = CartridgeHelper.getInstance().getSelectedTemplates();

		for (SubsValue subsval : DataStore.getInstance().getSubsvalues()) {
			if (showThisSubsValue(selctemplates, subsval)) {
				result.add(subsval);
			}
		}

		return result;
	}

	private boolean showThisSubsValue(Set<ITemplate> selctemplates, SubsValue subsval) {
		if (subsval.getUseForCartridges() == null || subsval.getUseForCartridges().length == 0) {
			return true;
		} else {
			for (String cart : subsval.getUseForCartridges()) {
				for (ITemplate t : selctemplates) {
					if (t.getCartridgeName().equals(cart))
						return true;
				}
			}
			return false;
		}
	}

	static class CbListener implements SelectionListener {

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
		}
	}

	private ModifyListener listener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			setPageComplete(inputOk());
		}
	};

	// --------------------------------------------------
	class CtrlData {
		SubsValue subsval;
		Label label;
		Text textbox;
		Button checkbox;
		Combo combobox;
		Button fileSelect;
		Shell shell;

		CtrlData(Shell aShell, Composite composite, SubsValue aSubsValue) {
			shell = aShell;
			subsval = aSubsValue;

			if (isFileOrFolderInput(aSubsValue.getType())) {
				addFileInput(composite, aSubsValue.getType(), aSubsValue);
			} else if (SubsValueType.CHECKBOX.equals(subsval.getType())) {
				addCheckbox(composite, aSubsValue);
			} else if (SubsValueType.COMBOBOX.equals(subsval.getType())) {
				addCombobox(composite, aSubsValue);
			} else {
				addTextInput(composite, aSubsValue);
			}
		}

		private boolean isFileOrFolderInput(SubsValueType aType) {
			return SubsValueType.FOLDER.equals(aType) || SubsValueType.FOLDERS.equals(aType)
					|| SubsValueType.FILE.equals(aType) || SubsValueType.FILES.equals(aType);
		}

		private void addTextInput(Composite composite, SubsValue aSubsValue) {
			String value = subsval.getValue() != null ? subsval.getValue() : "";
			String labelString = subsval.getLabel();
			if (aSubsValue.isRequired()) labelString += " *";
			label = new Label(composite, SWT.NULL);
			label.setText(labelString);

			textbox = new Text(composite, SWT.BORDER);
			textbox.setSize(150, 10);
			textbox.setText(value);
			textbox.setMessage(subsval.getName());
			textbox.addModifyListener(listener);
			if (subsval.getHelp() != null) {
				textbox.setToolTipText(subsval.getHelp());
			}
		}

		private void addFileInput(Composite composite, SubsValueType type, SubsValue aSubsValue) {
			String value = subsval.getValue() != null ? subsval.getValue() : "";
			label = new Label(composite, SWT.NULL);
			label.setText(subsval.getLabel());

			Composite cell = new Composite(composite, SWT.NONE);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.widthHint = 500;
			cell.setLayoutData(data);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			cell.setLayout(layout);

			textbox = new Text(cell, SWT.BORDER);
			textbox.setText(value);
			textbox.setSize(150, 10);
			textbox.setMessage(subsval.getName());
			textbox.addModifyListener(listener);
			if (subsval.getHelp() != null) {
				textbox.setToolTipText(subsval.getHelp());
			}

			fileSelect = new Button(cell, SWT.PUSH);
			fileSelect.setSize(16, 10);
			fileSelect.setText("..");
			fileSelect.setData(subsval.getName());

			if (SubsValueType.FOLDER.equals(type)) {
				fileSelect.addSelectionListener(new FileSelc(FOLDER_SELECT, SINGLE));
			} else if (SubsValueType.FOLDERS.equals(type)) {
				fileSelect.addSelectionListener(new FileSelc(FOLDER_SELECT, MULTIPLE));
			} else if (SubsValueType.FILES.equals(type)) {
				fileSelect.addSelectionListener(new FileSelc(FILE_SELECT, MULTIPLE));
			} else {
				fileSelect.addSelectionListener(new FileSelc(FILE_SELECT, SINGLE));
			}
		}

		private void addCheckbox(Composite composite, SubsValue aSubsValue) {
			checkbox = new Button(composite, SWT.CHECK);
			checkbox.setSelection(false);
			checkbox.addSelectionListener(new CbListener());
			checkbox.setData(subsval.getName());
			if (subsval.getHelp() != null) {
				checkbox.setToolTipText(subsval.getHelp());
			}

			label = new Label(composite, SWT.NULL);
			label.setText(subsval.getLabel());
		}

		private void addCombobox(Composite composite, SubsValue aSubsValue) {
			String value = subsval.getValue() != null ? subsval.getValue() : "";
			label = new Label(composite, SWT.NULL);
			label.setText(subsval.getLabel());

			combobox = new Combo(composite, SWT.CHECK);
			combobox.setData(subsval.getName());
			combobox.addSelectionListener(new CbListener());
			combobox.setItems(subsval.getComboList());
			combobox.addModifyListener(listener);
			if (subsval.getHelp() != null) {
				combobox.setToolTipText(subsval.getHelp());
			}
			combobox.setText(value);
		}

		private static final boolean FILE_SELECT = true;
		private static final boolean FOLDER_SELECT = false;
		private static final boolean MULTIPLE = true;
		private static final boolean SINGLE = false;

		class FileSelc implements SelectionListener {
			boolean fileselect;
			boolean multiple;

			public FileSelc(boolean fileselect, boolean multiple) {
				super();
				this.fileselect = fileselect;
				this.multiple = multiple;
			}

			public void widgetSelected(SelectionEvent event) {
				String selected = null;
				if (fileselect) {
					FileDialog dialog = new FileDialog(shell);
					dialog.setText("Open");
					selected = dialog.open();
				} else {
					DirectoryDialog dialog = new DirectoryDialog(shell);
					dialog.setText("Open");
					selected = dialog.open();
				}

				if (multiple) {
					String txt = textbox.getText();
					if (txt != null && txt.trim().length() > 0) {
						textbox.setText(txt + ";" + selected);
					} else {
						textbox.setText(selected);
					}
				} else {
					textbox.setText(selected);
				}
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		}
	}

}
