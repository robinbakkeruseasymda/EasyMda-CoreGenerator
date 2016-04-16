package flca.test.base;

import java.io.File;
import java.util.List;

import flca.mda.codegen.data.SubsValue;

/**
 * @author robin this class is used by TestTemplatesBase to fill the generator
 *         will data that otherwise comes from the interactive Gui. Each client
 *         should populate the instance of this class accordingly
 */
public abstract class AbstractTestTemplatesData {

	/**
	 * this should return the rootdir of the eclipse flca.mda.generator
	 * 
	 * @return
	 */
	public abstract File getPluginDir();

	/**
	 * This should return the rootdir of the eclipse project that contains the
	 * model(s)
	 * 
	 * @return
	 */
	public abstract File getModelDir();

	/**
	 * This should return the root of the eclipse project that contains the jet
	 * templates
	 * 
	 * @return
	 */
	public abstract File getTemplateDir();

	/**
	 * this should return a list of filled-id SubsValue's, as if filled in via
	 * the gui
	 * 
	 * @return
	 */
	public abstract List<SubsValue> getSubsvalues();

	protected static String getProjectDir() {
		String currentDir = System.getProperty("user.dir");
		return new File(currentDir).getParent();
	}

}
