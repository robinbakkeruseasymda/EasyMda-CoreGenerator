package com.flca.mda.codegen.engine;

import flca.mda.codegen.CodegenConstants;
import flca.mda.codegen.data.DataStore;
import flca.mda.codegen.data.SubsValue;
import flca.mda.codegen.data.SubsValueType;


/**
 * Helper class where all common subs values are defined, these are:
 * APP_PACKAGE, APP_NAME, SUBSVAL_GENERATE_ALL_TMPDIR, SUBSVAL_SKIP_ALL_EXISTING, SUBSVAL_USER_DIRS
 * Note the cardridges may add additional subs-valiues !
 * @author rbakkerus
 *
 */
public class CommonSubsValues {

	public static void addCommonSubsValues() 
	{
		skipAllExisting();
		generateToTmpdir();
		addAppName();
		addAppPackage();
		addUserDirs();
	}

	/*
	 * initialy we give the app-package the same value as the base-package, this may overwritten by user in gui
	 */
	private static void addAppPackage() {
		String basepck = DataStore.getInstance().getBasePackage();
		SubsValue appPckSubsval = new SubsValue(CodegenConstants.APP_PACKAGE, basepck, SubsValueType.TEXTINPUT, null);
		appPckSubsval.setValue(basepck);
		appPckSubsval.setLabel("App package.");
		appPckSubsval.setRank(1);
		appPckSubsval.setHelp("the main application package that will be generated \nthe base-package: " + 
				basepck + ". will be substited with this value");
		DataStore.getInstance().mergeSubsValue(appPckSubsval, false);
	}

	private static String addAppName() {
		String appName = DataStore.getInstance().getAppName();
		SubsValue appnameSubsval = new SubsValue(CodegenConstants.APP_NAME, appName, SubsValueType.TEXTINPUT, null);
		appnameSubsval.setLabel("App name");
		appnameSubsval.setRank(1);
		appnameSubsval.setHelp("the application name that will be generated (ex: Demo)");
		DataStore.getInstance().mergeSubsValue(appnameSubsval, false);
		return appName;
	}

	private static void generateToTmpdir() {
		SubsValue generTmpdir = new SubsValue(CodegenConstants.SUBSVAL_GENERATE_ALL_TMPDIR, "false", SubsValueType.CHECKBOX, null);
		generTmpdir.setLabel("generate to temp dir");
		generTmpdir.setRank(9);
		generTmpdir.setHelp("do you want to generate all code to the temp folder?");
		DataStore.getInstance().addSubsValue(generTmpdir);
	}

	private static void skipAllExisting() {
		SubsValue skipAllExist = new SubsValue(CodegenConstants.SUBSVAL_SKIP_ALL_EXISTING, "false", SubsValueType.CHECKBOX, null);
		skipAllExist.setLabel("skip all existing files");
		skipAllExist.setRank(9);
		skipAllExist.setHelp("Only files that you deleted manually will be re-generated");
		DataStore.getInstance().addSubsValue(skipAllExist);
	}

	private static void addUserDirs() {
		SubsValue subsval = new SubsValue(CodegenConstants.SUBSVAL_USER_SOURCE_DIRS, "", SubsValueType.FOLDERS, null);
		subsval.setLabel("My source directories");
		subsval.setRank(8);
		subsval.setHelp("You may want to move file from the generate fodler to you own directories");
		DataStore.getInstance().addSubsValue(subsval);
	}
}
