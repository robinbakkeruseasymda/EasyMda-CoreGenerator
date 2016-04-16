package flca.mda.common.api.util.postprocess;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import flca.mda.api.util.NameUtils;
import flca.mda.codegen.data.ITemplate;
import flca.mda.codegen.helpers.StrUtil;

public class PostprocessPackages  {
	private String pckFrom;
	private String pckTo;
	private NameUtils nu = new NameUtils();
	private Set<ITemplate> applicableTemplates = new HashSet<ITemplate>();
	
	public PostprocessPackages() {
		super();
	}

	public PostprocessPackages(String pckFrom, String pckTo) {
		super();
		this.pckFrom = pckFrom;
		this.pckTo = pckTo;
	}

	

	public String parse(String aInput, ITemplate aTemplate,
			Class<?> aTargetClass) {
		String result = aInput;

		Map<String, String> subsfromTos = getSubFromTo();
		for (String strfrom : subsfromTos.keySet()) {
			String strto = subsfromTos.get(strfrom);
			result = result.replaceAll(strfrom, strto);
		}

		return result;
	}

	
	public void applicableTemplates(Set<ITemplate> aApplicableTemplates) {
		applicableTemplates = aApplicableTemplates;
	}

	public boolean isApplicable(ITemplate aTemplate) {
		if (applicableTemplates == null || applicableTemplates.isEmpty()) {
			return true;
		} else {
			return applicableTemplates.contains(aTemplate);
		}
	}

	//---- private methods ------
	private String getPackageFrom() {
		if (pckFrom != null) {
			return pckFrom;
		} else {
			return nu.getIniFileValue("global.settings", "replace.package.from");
		}
	}

	private String getPackageTo() {
		if (pckTo != null) {
			return pckTo;
		} else {
			return nu.getIniFileValue("global.settings", "replace.package.to");
		}
	}

	private Map<String, String> getSubFromTo() {
		Map<String, String> p = new HashMap<String, String>();

		String strFrom = getPackageFrom();
		String strTo = getPackageTo();

		if (!StrUtil.isEmpty(strFrom) && !StrUtil.isEmpty(strTo)) {
			p.put("package " + strFrom, "package " + strTo);
			p.put("import " + strFrom, "import " + strTo);
		}

		return p;
	}

	public Set<ITemplate> getApplicableTemplates() {
		return applicableTemplates;
	}
	
	
}
