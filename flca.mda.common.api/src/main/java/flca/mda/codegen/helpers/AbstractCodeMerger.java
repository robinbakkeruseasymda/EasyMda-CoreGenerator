package flca.mda.codegen.helpers;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import flca.mda.api.util.SimpleMerge;
import flca.mda.codegen.data.ITemplate;
import flca.mda.codegen.data.TemplateMergeStrategy;

public abstract class AbstractCodeMerger {

	protected static Logger logger = LoggerFactory.getLogger(SimpleMerge.class);
	
	public String doMerge(String aNewCode, File aOldFile, 
			Class<?> aCurrentClass, ITemplate aTemplate) {

		TemplateMergeStrategy mergestrat = aTemplate.getMergeStrategy(); 
		if (mergestrat == null) {
			return aNewCode;
		} else if (mergestrat.equals(TemplateMergeStrategy.OVERWRITE)) {
			return aNewCode;
		} else if (mergestrat.equals(TemplateMergeStrategy.SKIP)) {
			return getOldCode(aOldFile);
		} else if (mergestrat.equals(TemplateMergeStrategy.SIMPLE_MERGE)) {
			return SimpleMerge.merge(aNewCode, aOldFile);
		} else {
			return specialMerge(aNewCode, aOldFile, aCurrentClass, aTemplate);
		}
	}
	
	public abstract String specialMerge(String aNewCode, File aOldFile, Class<?> aCurrentClass, ITemplate aTemplate);
	
	protected String getOldCode(File aOldFile) {
		try {
			return FileHelper.readFile(aOldFile);
		} catch(IOException ioEx) {
			logger.error("error reading " + aOldFile);
			return null;
		}
	}
}
