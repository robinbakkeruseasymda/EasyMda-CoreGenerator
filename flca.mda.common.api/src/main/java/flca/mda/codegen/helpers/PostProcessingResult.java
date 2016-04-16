package flca.mda.codegen.helpers;

/** 
 * result class that must be returne by an instance implement IPostProcess
 * @author nly36776
 *
 */
public class PostProcessingResult
{
	private String code;
	private String outputFilename;
	
	public PostProcessingResult(String code)
	{
		this(code, null, null);
	}


	public PostProcessingResult(String code, String outputFilename, String message)
	{
		super();
		this.code = code;
		this.outputFilename = outputFilename;
	}

	/**
	 * return the code that may haven been modified by the postprocessor
	 * @return
	 */
	public String getCode() {
		return code;
	}


	/**
	 * returns the new output filename. If null, the output filename is the same.
	 * TODO NOTE THIS FEATURE IS NOT IMPLEMENTED YET IN TEMPLATEPROCESSOR
	 * @return
	 */
	public String getOutputFilename()	{
		return outputFilename;
	}


	public void setOutputFilename(String outputFilename) {
		this.outputFilename = outputFilename;
	}
	
	
}

