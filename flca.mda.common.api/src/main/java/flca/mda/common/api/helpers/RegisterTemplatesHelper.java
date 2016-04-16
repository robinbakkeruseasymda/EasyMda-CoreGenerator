package flca.mda.common.api.helpers;

import java.util.Collection;
import java.util.HashSet;


import flca.mda.codegen.CodegenConstants;
import flca.mda.codegen.data.DataStore;
import flca.mda.codegen.data.SubsValue;
import flca.mda.codegen.data.SubsValueType;

public class RegisterTemplatesHelper implements CodegenConstants {

	private RegisterTemplatesHelper() {}
	
	/** 
	 * this will return some SubsValues that will normally be used by all cartridges
	 * @return
	 */
	public static Collection<SubsValue> getCommomSubsValues(String aForCartridges[]) {
		Collection<SubsValue> result = new HashSet<SubsValue>();
		
		result.add(makeSubsValue(APP_NAME, SubsValueType.TEXTINPUT, "App name", 
				"the application name that will be generated (ex: Demo)", 
				1, null, aForCartridges));

		String basepck = DataStore.getInstance().getBasePackage();
		result.add(makeSubsValue(APP_PACKAGE, SubsValueType.TEXTINPUT, "App package", 
				"the main application package that will be generated \nthe base-package: " + basepck + ". will be substited with this value", 
				2, null, aForCartridges));

		result.add(makeSubsValue(SUBSVAL_USER_SOURCE_DIRS, 
				SubsValueType.FOLDERS, "My source directories", 
				"You may want to move file from the generate fodler to you own directories", 
				100, null, aForCartridges));

		result.add(makeSubsValue(SUBSVAL_GENERATE_ALL_TMPDIR, 
				SubsValueType.CHECKBOX, "generate to temp dir", 
				"do you want to generate all code to the temp folder?", 
				101, null, aForCartridges));

		result.add(makeSubsValue(SUBSVAL_SKIP_ALL_EXISTING, 
				SubsValueType.CHECKBOX, "skip all existing files", 
				"Only files that you deleted manually will be re-generated", 
				102, null, aForCartridges));

		
		return result;
	}
	
	/**
	 * return a SubsValue to be used for a Text box
	 * @param name
	 * @param type
	 * @param label
	 * @param help
	 * @param rank
	 * @param aDefaultValue
	 * @param aForCartridges
	 * @return
	 */
	public static  SubsValue makeSubsValue(
			String name, 
			SubsValueType type, 
			String label, 
			String help, 
			int rank, 
			String aDefaultValue, 
			String aForCartridges[]) {
		
		SubsValue result = new SubsValue(name);
		result.setType(type);
		result.setLabel(label);
		result.setRank(rank);
		result.setUseForCartridges(aForCartridges); //null means for any cartridge
		result.setHelp(help);
		result.setValue(aDefaultValue);
		
		return result;
	}

	/**
	 * return a SubsValue to be used for a combobox
	 * @param name
	 * @param label
	 * @param items
	 * @param help
	 * @param rank
	 * @param aDefaultValue
	 * @param aForCartridges
	 * @return
	 */
	public static  SubsValue makeComboSubsValue(
			String name, 
			String label, 
			String[] items, 
			String help, 
			int rank, 
			String aDefaultValue, 
			String aForCartridges[]) {
		
		SubsValue result = new SubsValue(name);
		result.setType(SubsValueType.COMBOBOX);
		result.setLabel(label);
		result.setComboList(items);
		result.setRank(rank);
		result.setHelp(help);
		result.setUseForCartridges(aForCartridges); //null means for any cartridge
		result.setValue(aDefaultValue);
		
		return result;
	}
}
