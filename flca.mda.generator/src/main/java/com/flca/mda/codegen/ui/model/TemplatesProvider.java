package com.flca.mda.codegen.ui.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.flca.mda.codegen.helpers.CartridgeHelper;
import com.flca.mda.codegen.helpers.ClassloaderHelper;
import com.flca.mda.codegen.helpers.LogHelper;
import com.flca.mda.codegen.helpers.ProjectInstanceHelper;

import flca.mda.codegen.data.ITemplate;
import flca.mda.codegen.data.TemplatesBranch;
import flca.mda.codegen.data.TemplatesStoreData;

public class TemplatesProvider {

	private static TemplatesProvider sInstance;

	// with this list the left table will be filled
	private List<ListNode> branches;

	// with this list the right table will be filled. It only contains
	// applicable templates
	private List<ListNode> templates;

	private TemplatesProvider() {
		reset();
	}

	public void reset() {
		branches = setupBranches();
		templates = new ArrayList<ListNode>();
	}

	private List<ListNode> setupBranches() {
		branches = new ArrayList<ListNode>();

		Map<File, TemplatesStoreData> treeMap = CartridgeHelper.getInstance().getTemplatesDescriptors();

		for (File templatesFolder : treeMap.keySet()) {
			if (CartridgeHelper.getInstance().getCartridgeEnableStatus(templatesFolder)) {
				TemplatesStoreData storeData = treeMap.get(templatesFolder);
				ListNode treenode = new TreeNode(templatesFolder.getPath());
				branches.add(treenode);

				if (storeData != null && storeData.getTemplatesTree() != null
						&& storeData.getTemplatesTree().getBranches() != null) {
					for (TemplatesBranch branch : storeData.getTemplatesTree().getBranches()) {
						BranchNode brnode = new BranchNode(branch.getName(), treenode.getId());
						branches.add(brnode);

						for (ITemplate template : branch.getTemplates()) {
							brnode.addTemplate(new TemplateNode(template.getName(), brnode.getId(), template));
						}
					}
				}
			}
		}

		return branches;
	}

	/**
	 * This methog iis triggered when a branchnode is clicked in the left grid.
	 * It will update a list that is used to populate the grid on the right side
	 * Note this list only contains 'applicable' items depending on the Java
	 * sources that were selected.
	 * 
	 * @param aBranchId
	 * @param aFlag
	 */
	public void updateTemplates(int aBranchId, boolean aFlag) {
		BranchNode bnode = getBranchNode(aBranchId);

		if (bnode != null && bnode.templates != null) {
			if (aFlag) {
				templates.add(bnode);
				for (TemplateNode tnode : bnode.templates) {
					if (isApplicable(tnode)) {
						templates.add(tnode);
					}
				}
			} else {
				templates.remove(bnode);
				for (TemplateNode tnode : bnode.templates) {
					templates.remove(tnode);
				}
			}
		}
	}

	private boolean isApplicable(TemplateNode aNode) {
		if (ProjectInstanceHelper.getInstance().getSelectedClassnames() != null) {
			for (String fqn : ProjectInstanceHelper.getInstance().getSelectedClassnames()) {
				Class<?> clz;
				try {
					clz = ClassloaderHelper.getInstance().getClassLoader().loadClass(fqn);
					if (clz != null && aNode.getTemplate().appliesTo(clz)) {
						return true;
					}
				} catch (Exception e) {
					LogHelper.error("error in TemplatesProvider.isApplicable() on : " + fqn + " : " + e);
				}
			}
		}

		return false;
	}

	/**
	 * This methog is triggered when a branchnode is clicked in the right grid.
	 * It will update a the selection state that is used to populate the grid on
	 * the right side
	 * 
	 * @param aBranchId
	 * @param aFlag
	 */
	public void updateTemplatesState(BranchNode bnode, boolean aFlag) {
		if (bnode != null && bnode.templates != null) {
			for (TemplateNode tnode : bnode.templates) {
				TemplateNode tnode2 = getTemplateNode(tnode.getId());
				tnode2.setSelected(aFlag);
			}
		}
	}

	private BranchNode getBranchNode(int aId) {
		for (ListNode node : branches) {
			if (node.getId() == aId) {
				if (node instanceof BranchNode) {
					return (BranchNode) node;
				}
			}
		}
		return null;
	}

	private TemplateNode getTemplateNode(int aId) {
		for (ListNode node : templates) {
			if (node.getId() == aId) {
				if (node instanceof TemplateNode) {
					return (TemplateNode) node;
				}
			}
		}
		return null;
	}

	public static synchronized TemplatesProvider getInstance() {
		if (sInstance != null) {
			return sInstance;
		}
		sInstance = new TemplatesProvider();
		return sInstance;
	}

	public List<ListNode> getTemplateBranches() {
		return branches;
	}

	public List<ListNode> getTemplates() {
		return templates;
	}

	public List<ListNode> getEmptyList(int n) {
		List<ListNode> result = new ArrayList<ListNode>();

		for (int i = 0; i < n; i++) {
			result.add(new ListNode(""));

		}
		return result;
	}

}
