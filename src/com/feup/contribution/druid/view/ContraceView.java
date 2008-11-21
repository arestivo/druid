package com.feup.contribution.druid.view;
		
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.feup.contribution.druid.ContracePlugin;
import com.feup.contribution.druid.data.ContraceDependency;
import com.feup.contribution.druid.data.ContraceFeature;
import com.feup.contribution.druid.data.ContraceMethod;
import com.feup.contribution.druid.data.ContraceProject;
import com.feup.contribution.druid.data.ContraceTest;
import com.feup.contribution.druid.data.ContraceUnit;
import com.feup.contribution.druid.listeners.ProjectListener;

public class ContraceView extends ViewPart implements ProjectListener{
	private TreeViewer treeViewer;

	@Override
	public void setFocus() {
	}

	public void projectChanged(final ContraceProject project) {
		treeViewer.getTree().getDisplay().asyncExec(new Runnable(){
			public void run() {
				//treeViewer.refresh(project, false);
				treeViewer.refresh();
			}
		});
	}

	@Override
	public void createPartControl(Composite parent) {
		treeViewer = new TreeViewer(parent);
		treeViewer.setContentProvider(new ContraceContentProvider());
		treeViewer.setLabelProvider(new ContraceLabelProvider());
		treeViewer.setInput(ContracePlugin.getPlugin().getProject());
		
		ContracePlugin.getPlugin().getProject().addProjectListener(this);
		
		treeViewer.addDoubleClickListener(new IDoubleClickListener(){
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
				Object element = selection.getFirstElement();
				if (element instanceof ContraceMethod) {
					ContraceMethod method = (ContraceMethod) element;
					try {
						JavaUI.openInEditor((IJavaElement) method.getMethod(),true,true);
					} catch (PartInitException e) {
					} catch (JavaModelException e) {
					}
				}
				if (element instanceof ContraceTest) {
					ContraceTest test = (ContraceTest) element;
					try {
						JavaUI.openInEditor((IJavaElement) test.getMethod(),true,true);
					} catch (PartInitException e) {
					} catch (JavaModelException e) {
					}
				}
				if (element instanceof ContraceDependency) {
					ContraceDependency dependency = (ContraceDependency) element;
					treeViewer.setSelection(new TreeSelection(new TreePath(new Object[] {dependency.getDependee()})));
				}
				if (element instanceof ContraceFeature) {
					ContraceFeature feature = (ContraceFeature) element;
					treeViewer.setExpandedState(feature, !treeViewer.getExpandedState(feature));
				}
				if (element instanceof ContraceUnit) {
					ContraceUnit unit = (ContraceUnit) element;
					treeViewer.setExpandedState(unit, !treeViewer.getExpandedState(unit));
				}
			}
			
		});
	}

}
