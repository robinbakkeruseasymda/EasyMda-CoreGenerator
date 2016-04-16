package flca.mda.api.util;


import flca.mda.codegen.data.ITemplate;
import flca.mda.codegen.helpers.SourceCodeUtils;

public class JetArgument {
	private Object element;
	private Class<?> elementClass;
	private ITemplate template;
	private String sourceCode;

	private static JetArgument sCurrentJarArgument;

	public JetArgument() {
	}

	public JetArgument(Object aSourceElem, ITemplate aTemplate, String aSourceCode) {
		this(aTemplate, aSourceCode);

		element = aSourceElem;
		elementClass = aSourceElem.getClass();
	}

	public JetArgument(Class<?> aSourceClass, ITemplate aTemplate, String aSourceCode) {
		this(aTemplate, aSourceCode);

		element = null;
		elementClass = aSourceClass;
	}

	private JetArgument(ITemplate aTemplate, String aSourceCode) {
		template = aTemplate;
		sourceCode = aSourceCode;
		sCurrentJarArgument = this;
	}

	public static JetArgument getCurrent() {
		return sCurrentJarArgument;
	}
	/**
	 * This can be used in JUnit tests
	 * @param jetarg
	 */
	public static void setCurrent(JetArgument jetarg) {
		sCurrentJarArgument = jetarg; 
	}

	public Object getElement() {
		return element;
	}

	public void setElement(Object element) {
		this.element = element;
	}

	public Class<?> getElementClass() {
		return elementClass;
	}

	public void setElementClass(Class<?> elementClass) {
		this.elementClass = elementClass;
	}

	public ITemplate getTemplate() {
		return template;
	}

	public void setTemplate(ITemplate template) {
		this.template = template;
	}

	public String getSourceCode() {
		if (sourceCode == null) {
			Class<?> currclz = JetArgument.getCurrent().getElementClass();
			sourceCode = new SourceCodeUtils().readSource(currclz.getName());
		}
		return sourceCode;
	}

	public void setSourceCode(String courceCode) {
		this.sourceCode = courceCode;
	}
	
}
