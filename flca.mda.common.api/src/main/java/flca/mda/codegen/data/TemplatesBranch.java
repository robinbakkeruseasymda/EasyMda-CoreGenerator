package flca.mda.codegen.data;

import java.util.ArrayList;
import java.util.List;

public class TemplatesBranch {

	private String name;
	private String description;
	private List<ITemplate> templates = new ArrayList<ITemplate>();

	public TemplatesBranch() {
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<ITemplate> getTemplates() {
		return templates;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setTemplates(List<ITemplate> templates) {
		this.templates = templates;
	}

	public void addTemplate(ITemplate template) {
		if (this.templates == null) {
			this.templates = new ArrayList<ITemplate>();
		}
		this.templates.add(template);
	}

	// ------------- help method to clone myself

	public TemplatesBranch cloneMe() {
		TemplatesBranch result = new TemplatesBranch();
		
		result.setName(this.getName());
		result.setDescription(this.getDescription());
		for (ITemplate t : this.getTemplates()) {
			result.addTemplate(t.cloneMe());
		}
		
		return result;
	}
}
