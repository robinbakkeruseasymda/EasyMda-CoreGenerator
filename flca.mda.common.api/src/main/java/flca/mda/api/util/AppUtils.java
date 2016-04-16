package flca.mda.api.util;

import flca.mda.codegen.data.DataStore;


public class AppUtils
{

	public String getApplicationName() {
		return DataStore.getInstance().getAppName();
	}
	
	public String getApplicationPackage()
	{
		return DataStore.getInstance().getAppPackage();
	}
	
}

