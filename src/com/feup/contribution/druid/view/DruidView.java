/*
 * This file is part of drUID.
 * 
 * drUID is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * drUID is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with drUID.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.feup.contribution.druid.view;
		
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.feup.contribution.druid.DruidPlugin;
import com.feup.contribution.druid.data.DruidDependency;
import com.feup.contribution.druid.data.DruidFeature;
import com.feup.contribution.druid.data.DruidMethod;
import com.feup.contribution.druid.data.DruidProject;
import com.feup.contribution.druid.data.DruidTest;
import com.feup.contribution.druid.data.DruidUnit;
import com.feup.contribution.druid.listeners.ProjectListener;

public class DruidView extends ViewPart implements ProjectListener{
	private TreeViewer treeViewer;
	private Composite dialogComposite;
	private GridLayout dialogLayout;
	
	private Button detectButton;
	private Label detectLabel;
		
	@Override
	public void setFocus() {
	}

	public void projectChanged(final DruidProject project) {
		treeViewer.getTree().getDisplay().asyncExec(new Runnable(){
			public void run() {
				treeViewer.refresh();
			}
		});
	}

	@Override
	public void createPartControl(Composite parent) {
		treeViewer = new TreeViewer(parent);
		treeViewer.setContentProvider(new DruidContentProvider());
		treeViewer.setLabelProvider(new DruidLabelProvider());
		treeViewer.setInput(DruidPlugin.getPlugin());

		dialogLayout = new GridLayout();
		dialogLayout.numColumns = 2;
		
		dialogComposite = new Composite(parent, SWT.NONE);
		dialogComposite.setLayout(dialogLayout);

		detectLabel = new Label(dialogComposite, SWT.NONE);
		detectLabel.setText("Detect Interactions");

		detectButton = new Button(dialogComposite, SWT.PUSH);
		detectButton.setText("Execute");
		detectButton.setEnabled(false);
		
		DruidPlugin.getPlugin().addProjectListener(this);
		
		detectButton.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event evt) {
				IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
				Object element = selection.getFirstElement();
				if (element instanceof DruidProject) {
					DruidProject project = (DruidProject) element;
					project.detectInteractions();
				}
			}
		});
		
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener(){
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
				Object element = selection.getFirstElement();
				if (element instanceof DruidProject) detectButton.setEnabled(true);
				else detectButton.setEnabled(false);
			}
		});
		
		treeViewer.addDoubleClickListener(new IDoubleClickListener(){
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
				Object element = selection.getFirstElement();
				if (element instanceof DruidMethod) {
					DruidMethod method = (DruidMethod) element;
					try {
						JavaUI.openInEditor((IJavaElement) method.getMethod(),true,true);
					} catch (PartInitException e) {
					} catch (JavaModelException e) {
					}
				}
				if (element instanceof DruidTest) {
					DruidTest test = (DruidTest) element;
					try {
						JavaUI.openInEditor((IJavaElement) test.getMethod(),true,true);
					} catch (PartInitException e) {
					} catch (JavaModelException e) {
					}
				}
				if (element instanceof DruidDependency) {
					DruidDependency dependency = (DruidDependency) element;
					treeViewer.setSelection(new TreeSelection(new TreePath(new Object[] {dependency.getDependee()})));
				}
				if (element instanceof DruidFeature) {
					DruidFeature feature = (DruidFeature) element;
					treeViewer.setExpandedState(feature, !treeViewer.getExpandedState(feature));
				}
				if (element instanceof DruidUnit) {
					DruidUnit unit = (DruidUnit) element;
					treeViewer.setExpandedState(unit, !treeViewer.getExpandedState(unit));
				}
			}
			
		});
	}

}
