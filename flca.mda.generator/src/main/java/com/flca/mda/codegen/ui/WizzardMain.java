package com.flca.mda.codegen.ui;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class WizzardMain extends ApplicationWindow {

	public WizzardMain(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createContents(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText("Test Codegen wizzard");
		button.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				CodegenWizzard wizard = new CodegenWizzard();

				WizardDialog dialog = new CodegenWizardDialog(getShell(), wizard);
				dialog.setBlockOnOpen(true);

				@SuppressWarnings("unused")
				int returnCode = dialog.open();
			}
		});
		return button;
	}

}
