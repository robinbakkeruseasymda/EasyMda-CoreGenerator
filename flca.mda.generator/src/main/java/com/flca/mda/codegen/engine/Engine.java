package com.flca.mda.codegen.engine;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import mda.type.IRegisterTemplates;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.flca.mda.codegen.Activator;
import com.flca.mda.codegen.helpers.CartridgeHelper;
import com.flca.mda.codegen.helpers.ClassloaderHelper;
import com.flca.mda.codegen.helpers.LogHelper;
import com.flca.mda.codegen.helpers.ProjectInstanceHelper;
import com.flca.mda.codegen.helpers.SaveGeneratedCodeHelper;
import com.flca.mda.codegen.helpers.SimpleClibboardHelper;

import flca.mda.codegen.data.ITemplate;
import flca.mda.codegen.data.ITemplateHooks;
import flca.mda.codegen.data.TemplatesStore;
import flca.mda.codegen.helpers.AdditionalGeneratesHelper;

/**
 * @author robin bakkerus
 * @version $Id: Engine.java,v 1.9 2010-08-01 21:40:48 rbakkerus Exp $
 * 
 *          Note: this file is based on the similar file from JGilbers's Taylor
 *          MDA
 */

public class Engine {
	private ClassLoader loader;
	private CartridgeHelper crh;
	private ProjectInstanceHelper ph;

	private static final boolean FORCE_GENERATE = true;
	private static final boolean DONT_FORCE_GENERATE = false;

	public Engine() {
	}

	/**
	 * this method will loop over all selected model classes and then over all
	 * selected templates and with the class/template pair execute the
	 * generator. But before that first the classes are sorted based on the
	 * lowest rank that is applicable for this class and then the templates also
	 * on the rank.
	 */
	public void generate() {
		LogHelper.info("generate() started ...");
		loader = ClassloaderHelper.getInstance().getClassLoader();
		crh = CartridgeHelper.getInstance();
		ph = ProjectInstanceHelper.getInstance();

		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
					InterruptedException {
				IJavaProject currproj = ProjectInstanceHelper.getInstance().getCurrentProject();
				TemplateProcessor processor = new TemplateProcessor(currproj, monitor);
				AdditionalGeneratesHelper.reset();

				List<Class<?>> sortedModelClasses = sortSelectedClasses(ph.getSelectedClassnames(),
						crh.getSelectedTemplates());
				ITemplate sortedTemplates[] = sortTemplates(crh.getSelectedTemplates());

				if (sortedTemplates.length > 0) {
					doBeforeGenerate(sortedTemplates[0]);
					doGenerate(processor, sortedModelClasses, sortedTemplates);
					generateAdditionClasses(processor);
					doAfterGenerate(sortedTemplates[0]);
				}
				logSummary();
			}
		};

		run(op);
	}

	private void doGenerate(TemplateProcessor processor, List<Class<?>> sortedModelClasses,
			ITemplate sortedTemplates[]) {

		for (Class<?> modelFqn : sortedModelClasses) {
			for (ITemplate template : sortedTemplates) {
				processor.generate(modelFqn, template, null, DONT_FORCE_GENERATE);
			}
		}
	}

	private void doBeforeGenerate(ITemplate aTemplate) {
		IRegisterTemplates reg = getRegisterTemplates(aTemplate);
		reg.doBefore();
	}

	private void doAfterGenerate(ITemplate aTemplate) {
		IRegisterTemplates reg = getRegisterTemplates(aTemplate);
		reg.doAfter();
	}
	
	private IRegisterTemplates getRegisterTemplates(ITemplate aTemplate) {
		return TemplatesStore.getInstance().getRegisterTemplates(aTemplate);
	}

	/**
	 * the jet templates may add additional templates to be processed (via
	 * tu.generate) these are generated here.
	 * 
	 * @param processor
	 */
	private void generateAdditionClasses(TemplateProcessor processor) {
		for (int i = 0; i < AdditionalGeneratesHelper.size(); i++) {
			processor.generate(AdditionalGeneratesHelper.getClass(i), AdditionalGeneratesHelper.getTemplate(i),
					AdditionalGeneratesHelper.getArguments(i), FORCE_GENERATE);
		}
	}

	private void logSummary() {
		LogHelper.console(LogHelper.getResults());
		LogHelper.console("backup files are written to " + SaveGeneratedCodeHelper.getTempdir());
		LogHelper.console("detailed logging can be found in /tmp/log/easymda.log ");
	}

	protected void run(WorkspaceModifyOperation op) {
		Shell shell = Activator.getActiveWorkbenchShell();
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		try {
			dialog.run(false, true, op);
		} catch (Throwable t) {
			LogHelper.error("error encountered ", t);
			throw new RuntimeException(t);
		}
	}

	protected List<Class<?>> sortSelectedClasses(Collection<String> aFqnames, Collection<ITemplate> aTemplates) {
		SortFqn sortarr[] = new SortFqn[aFqnames.size()];

		int i = 0;
		for (String fqn : aFqnames) {
			sortarr[i++] = new SortFqn(fqn, aTemplates);
		}

		Arrays.sort(sortarr);

		List<Class<?>> result = new ArrayList<Class<?>>();

		for (SortFqn sortfqn : sortarr) {
			result.add(sortfqn.clazz);
		}

		return result;
	}

	protected ITemplate[] sortTemplates(Set<ITemplate> aTemplates) {
		ITemplate result[] = new ITemplate[aTemplates.size()];

		int i = 0;
		for (ITemplate t : aTemplates) {
			result[i++] = t;
		}

		Arrays.sort(result);
		return result;
	}

	// -----------------------

	class SortFqn implements Comparable<SortFqn> {
		String fqn;
		Class<?> clazz;
		Integer rank = 99;

		public SortFqn(String aFqn, Collection<ITemplate> aTemplates) {
			fqn = aFqn;

			try {
				clazz = loader.loadClass(aFqn);

				for (ITemplate t : aTemplates) {
					if (t.appliesTo(clazz)) {
						if (t.getRank() < rank) {
							rank = t.getRank();
						}
					}
				}
			} catch (Throwable e) {
				handleSortFqnError(e);
			}

		}

		void handleSortFqnError(Throwable e) {
			String fout = e.getMessage();
			if (e instanceof InvocationTargetException) {
				fout = ((InvocationTargetException) e).getMessage();
			} else if (e instanceof RuntimeException) {
				RuntimeException rte = (RuntimeException) e;
				if (rte.getCause() instanceof InvocationTargetException) {
					InvocationTargetException invtex = (InvocationTargetException) rte.getCause();
					fout = invtex.getTargetException().getMessage();
				}
			}
			LogHelper.error("error loading class " + fqn + " : " + fout);
		}

		@Override
		public int compareTo(SortFqn other) {
			return rank.compareTo(other.rank);
		}
	}
}
