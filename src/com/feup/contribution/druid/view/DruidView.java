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
		
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.feup.contribution.druid.DruidPlugin;
import com.feup.contribution.druid.data.DruidDependency;
import com.feup.contribution.druid.data.DruidFeature;
import com.feup.contribution.druid.data.DruidMethod;
import com.feup.contribution.druid.data.DruidProject;
import com.feup.contribution.druid.data.DruidTest;
import com.feup.contribution.druid.data.DruidUnit;
import com.feup.contribution.druid.diagram.ImageCanvas;
import com.feup.contribution.druid.listeners.ProjectListener;

public class DruidView extends ViewPart implements ProjectListener{
	private CheckboxTreeViewer treeViewer;
	private Composite dialogComposite;
	private GridLayout dialogLayout;
		
	private ImageCanvas image;
	private Button zoomInButton;
	private Button fitButton;

	private Menu popupMenu;
	private MenuItem detectItem;
	
	private DruidProject lastProject;
	
	@Override
	public void setFocus() {
	}

	public void projectChanged(final DruidProject project) {
		image.getDisplay().asyncExec(new Runnable(){
			@Override
			public void run() {
				imageRefresh(lastProject);
			}
		});
		treeViewer.getTree().getDisplay().asyncExec(new Runnable(){
			@Override
			public void run() {
				treeViewer.refresh();
				treeViewer.setCheckedElements (DruidPlugin.getPlugin().getSelectedUnits());
			}
		});
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(3, true);
		parent.setLayout(layout);
		
		treeViewer = new CheckboxTreeViewer(parent, SWT.CHECK);
		treeViewer.setContentProvider(new DruidContentProvider());
		treeViewer.setLabelProvider(new DruidLabelProvider());
		treeViewer.setInput(DruidPlugin.getPlugin());
		
		popupMenu = new Menu(treeViewer.getTree());
		treeViewer.getTree().setMenu(popupMenu);
		detectItem = new MenuItem(popupMenu, SWT.PUSH);
		detectItem.setText("Detect Interactions");
		
		GridData treeLayout = new GridData(GridData.FILL_BOTH);
		treeViewer.getTree().setLayoutData(treeLayout);
		
		dialogLayout = new GridLayout();
		dialogLayout.numColumns = 2;
		
		dialogComposite = new Composite(parent, SWT.NONE);
		dialogComposite.setLayout(dialogLayout);

		GridData compositeLayout = new GridData(GridData.FILL_BOTH);
		compositeLayout.horizontalSpan = 2;
		compositeLayout.grabExcessHorizontalSpace = true;
		dialogComposite.setLayoutData(compositeLayout);
		
		zoomInButton = new Button(dialogComposite, SWT.PUSH);
		zoomInButton.setText("Zoom");

		fitButton = new Button(dialogComposite, SWT.PUSH);
		fitButton.setText("Fit");
		
		image = new ImageCanvas(dialogComposite);
		GridData imageLayout = new GridData(GridData.FILL_BOTH);
		imageLayout.horizontalSpan = 2;
		image.setLayoutData(imageLayout);
		
		DruidPlugin.getPlugin().addProjectListener(this);
		
		detectItem.addListener(SWT.Selection, new Listener(){
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
		
		zoomInButton.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event arg0) {
				image.zoomIn();
			}
			
		});

		fitButton.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event arg0) {
				image.fitCanvas();
			}
		});
		
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener(){
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
				Object element = selection.getFirstElement();
				if (element instanceof DruidProject) {
					detectItem.setEnabled(true);
					lastProject = (DruidProject) element;
					imageRefresh(lastProject);
					image.fitCanvas();
				}
				else detectItem.setEnabled(false);
			}
		});
		
		treeViewer.addCheckStateListener(new ICheckStateListener(){
			@Override
			public void checkStateChanged(CheckStateChangedEvent arg0) {
				Object element = arg0.getElement();
				if (element instanceof DruidUnit) {
					DruidUnit unit = (DruidUnit) element;
					unit.setSelected(treeViewer.getChecked(element));
					unit.getProject().updateDiagram();
					imageRefresh(lastProject);
				} else treeViewer.setChecked(element, false);
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

	private void imageRefresh(DruidProject project) {
		if (project==null) return;
		String workspacepath = Platform.getLocation().toOSString();
		String unitpath = project.getIProject().getPath().toOSString();

		image.setImageData(new ImageData(workspacepath+unitpath+"/druid.png"));
	}
}
