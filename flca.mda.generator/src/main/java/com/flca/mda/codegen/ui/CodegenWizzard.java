package com.flca.mda.codegen.ui;

import java.io.File;

import org.eclipse.jface.wizard.Wizard;

import com.flca.mda.codegen.engine.data.CartridgeClasspathData;
import com.flca.mda.codegen.helpers.CartridgeHelper;
import com.flca.mda.codegen.helpers.LogHelper;
import com.flca.mda.codegen.helpers.ProjectInstanceHelper;

import flca.mda.codegen.CodegenConstants;
import flca.mda.codegen.data.DataStore;
import flca.mda.codegen.data.SubsValue;
import flca.mda.codegen.data.SubsValueType;
import flca.mda.codegen.helpers.FileHelper;

public class CodegenWizzard extends Wizard {
	public CodegenWizzard() {
		setWindowTitle("Generate code wizzard");
		// setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		addPage(new SelectTemplateFoldersPage("p1"));
		addPage(new SelectTemplatesPage("p2"));
		addPage(new ShowSetupDataPage("p3"));
	}

	@Override
	public boolean performFinish() {
		ShowSetupDataPage p3 = getShowSetupDataPage();
		if (p3.saveNeeded()) {
			p3.saveSettings();
			savePreferencesToFile();
		}
		return true;
	}

//	@Override
//	public boolean canFinish() {
//		boolean b1 = CartridgeHelper.getInstance().getSelectedTemplates().size() > 0;
//		return b1;
////		ShowSetupDataPage p3 = getShowSetupDataPage();
////		boolean b2 = p3.canFinish();
////		return b1 && b2;
//	}

	private ShowSetupDataPage getShowSetupDataPage() {
		return (ShowSetupDataPage) getPage("p3");
	}
	
	private void savePreferencesToFile() {
		StringBuffer sb = new StringBuffer();

		sb.append("[" + CodegenConstants.PREFERENCES_TEMPLATES_SECTION + "] \n");
		for (CartridgeClasspathData data : CartridgeHelper.getInstance().getAllCartridgeClassPaths()) {
			String value = (data.isEnabled()) ? "true" : "false";
			sb.append(data.getKey() + " = " + value + "\n");
		}

		sb.append("[" + CodegenConstants.PREFERENCES_SUBSVALS_SECTION + "] \n");
		for (SubsValue subsval : DataStore.getInstance().getSubsvalues()) {
			if (shouldSave(subsval) &&  sb.toString().indexOf(subsval.getName()) < 0) {
				sb.append(subsval.getName() + " = " + subsval.getValue() + "\n");
			}
		}

		File outfile = new File(modelDir() + "/" + CodegenConstants.PREFERENCES_VALUES_FNAME);
		try {
			FileHelper.saveFile(outfile, sb.toString());
		} catch (Exception e) {
			LogHelper.error("error saving preferences to " + outfile, e);
		}
	}
	
	private boolean shouldSave(SubsValue subsval) {
		return !SubsValueType.NONE.equals(subsval.getType());
	}
	
	private File modelDir() {
		return new File(ProjectInstanceHelper.getInstance().getCurrentProjectLocation());
	}

}
