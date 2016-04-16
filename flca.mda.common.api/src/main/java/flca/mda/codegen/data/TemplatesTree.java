package flca.mda.codegen.data;

import java.util.ArrayList;
import java.util.List;

public class TemplatesTree {
	private String name;
	private String description;
	private List<TemplatesBranch> branches = new ArrayList<TemplatesBranch>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<TemplatesBranch> getBranches() {
		return branches;
	}

	public void setBranches(List<TemplatesBranch> branches) {
		this.branches = branches;
	}

	public void addBranch(TemplatesBranch aTemplate) {
		if (branches == null) {
			branches = new ArrayList<TemplatesBranch>();
		}
		branches.add(aTemplate);
	}
	
	//------------- help method to clone myself
	
	public TemplatesTree cloneMe() {
		TemplatesTree result = new TemplatesTree();
		
		result.setName(this.getName());
		result.setDescription(this.getDescription());
		for (TemplatesBranch tb : this.getBranches()) {
			result.addBranch(tb.cloneMe());
		}
		
		return result;
	}
}
