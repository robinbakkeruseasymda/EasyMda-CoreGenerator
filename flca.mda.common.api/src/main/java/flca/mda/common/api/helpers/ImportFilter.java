package flca.mda.common.api.helpers;

public interface ImportFilter {

	/**
	 * via this filter the behavior can be altered.
	 * When this methods return null, no import is executed otherwise he return value will be imported
	 */
	String filter(String aImport);
}
