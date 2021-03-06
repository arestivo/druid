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

package com.feup.contribution.druid.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.feup.contribution.druid.DruidPlugin;
import com.feup.contribution.druid.diagram.DotDiagramCreator;
import com.feup.contribution.druid.listeners.ProjectListener;
import com.feup.contribution.druid.tester.DruidTester;
import com.feup.contribution.druid.util.MethodSignatureCreator;

public class DruidProject{
	private ArrayList<DruidUnit> units;
	private ArrayList<ProjectListener> listeners = new ArrayList<ProjectListener>();
	private IJavaProject project;
	
	public static final String FEATURE_BROKEN = "com.feup.contribution.druid.featureBroken";
	public static final String FEATURE_NOT_BROKEN = "com.feup.contribution.druid.featureNotBroken";
	public static final String FAILED_TEST = "com.feup.contribution.druid.failedTest";
	public static final String DEPENDENCY_DEPRECATED = "com.feup.contribution.druid.dependencyDeprecated";
	
	public DruidProject(IJavaProject project){
		units = new ArrayList<DruidUnit>();
		listeners = new ArrayList<ProjectListener>();
		this.setProject(project);
	}
		
	private void setProject(IJavaProject project) {
		this.project = project;
	}

	public Collection<DruidUnit> getUnits(){
		return units;
	}
	
	private DruidUnit getUnit(String unitName) {
		for (DruidUnit unit : units) {
			if (unit.getName().equals(unitName)) return unit;
		}
		return null;
	}

	public void updateDiagram() {
		new DotDiagramCreator(this).drawDiagram();		
	}
	
	public void builderDone(){
		updateDiagram();
		for (ProjectListener listener : listeners) {
			listener.projectChanged(this);
		}
	}
	
	public void addProjectListener(ProjectListener listener) {
		listeners.add(listener);
	}

	public void addFeature(String unitName, IMethod method, String featureName) throws JavaModelException {
		String methodSignature = MethodSignatureCreator.createSignature(method);
		DruidUnit unit = getUnit(unitName);
		if (unit == null) {
			unit = new DruidUnit(unitName, this);
			units.add(unit);
		}
		unit.addFeature(method, methodSignature, featureName);
	}

	public void removeAllFeatures() {
		units.clear();
	}

	public boolean addDepends(String unitName, ArrayList<String> featureNames, String dUnitName, String dFeatureName, IResource resource, int offset, int length) {
		DruidUnit unit = getUnit(unitName);
		if (unit == null) return false;
		DruidFeature dFeature = getFeature(dUnitName, dFeatureName);
		if (dFeature != null) unit.addDepends(featureNames, dFeature, resource, offset, length);
		else return false;
		return true;
	}

	private DruidFeature getFeature(String unitName, String featureName) {
		DruidUnit unit = getUnit(unitName);
		if (unit == null) return null;
		return unit.getFeature(featureName);
	}

	public boolean addTest(IMethod method, String tUnitName, String tFeatureName) {
		DruidFeature tFeature = getFeature(tUnitName, tFeatureName);
		if (tFeature != null) tFeature.addTests(method);
		else return false;
		return true;
	}

	private Shell getShell()  {
		  IWorkbench workbench= PlatformUI.getWorkbench();
		  IWorkbenchWindow window= workbench.getActiveWorkbenchWindow();
		  if (window == null)
		    return null;
		  return window.getShell();
		}
	
	public boolean detectInteractions() {
		ProgressMonitorDialog dialog= new ProgressMonitorDialog(getShell());
		try {
			dialog.run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor)
				throws InvocationTargetException {
					detectInteractionsBuild(monitor);
				}
			});
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			return false;
		}
		return true;
	}
	
	public void detectInteractionsBuild(IProgressMonitor monitor) {		
		ArrayList<DruidComponent> components = DruidComponent.getOrderedComponents(units);
		ArrayList<DruidComponent> toCompile = new ArrayList<DruidComponent>();

		DruidTester tester = new DruidTester();

		String cp = getClasspath();

		removeDeprecatedBy();
		removeOldMarkers();

		monitor.beginTask("Detect Interactions", components.size()+1);
		monitor.worked(1);
		int currentComponent = 1;
		for (DruidComponent component : components) {
			if (monitor.isCanceled()) return;
			monitor.subTask(component.toString());
			toCompile.add(component);
		
			if (testDeprecatedFeatures(component)) return;
		
			for (DruidUnit druidUnit : component.getUnits()) druidUnit.addDeprecatedBy();
			
			tester.setUpTest(toCompile);
			tester.compile(cp);
			
			for (DruidComponent druidComponent : toCompile) {
				for (DruidUnit druidUnit : druidComponent.getUnits()) {
					for (DruidFeature druidFeature : druidUnit.getFeatures()) {						
						for (DruidTest druidTest : druidFeature.getTests()) {
							boolean result = tester.test(druidTest.getMethod(), cp);
							
							try {
								if (!result && component.getUnits().contains(druidUnit)) {
									String message = "Unit " + component.getUnits().get(0).getName() + ": test failed for feature " + druidFeature;
									IMethod method = druidTest.getMethod();
									addMarker(FAILED_TEST, message, method.getResource(), method.getNameRange().getOffset(), method.getNameRange().getLength());
									monitor.done();
									showErrorDialog("Failed Test", message, tester.getDetails());
									return;
								} else if (!druidFeature.isDeprecated() && !result) {
									String message = "Unit " + component.getUnits().get(0).getName() + " breaks feature " + druidFeature;
									IMethod method = druidTest.getMethod();
									addMarker(FEATURE_BROKEN, message, method.getResource(), method.getNameRange().getOffset(), method.getNameRange().getLength());
									monitor.done();
									showErrorDialog("Feature Broken", message, "");
									return;
								} else if (druidFeature.isDeprecated() && result) {
									String message = "Unit " + component.getUnits().get(0).getName() + " doesn't break feature " + druidFeature;
									DruidDeprecate deprecate = druidFeature.getDeprecatedBy().get(0);
									addMarker(FEATURE_NOT_BROKEN, message, deprecate.getResource(), deprecate.getOffset(), deprecate.getLength());
									monitor.done();
									showErrorDialog("Feature Not Broken", message, "");
									return;
								}
							} catch (JavaModelException e) {
								DruidPlugin.getPlugin().logException(e);
							}
						}
					}
				}
				tester.tearDown();
			}
			monitor.worked(++currentComponent);
		}
		monitor.done();
	}

	private void showErrorDialog(final String title, final String message, final String details) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openError(getShell(), title, message + "\n\n" + details);
			}
		});
	}

	private void addMarker(String type, String message, IResource resource, int offset, int length) {
		try {
			IMarker marker = resource.createMarker(type);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			marker.setAttribute(IMarker.CHAR_START, offset);
			marker.setAttribute(IMarker.CHAR_END, offset + length);
		} catch (CoreException e) {
			DruidPlugin.getPlugin().logException(e);
		}		
	}
	
	private void removeDeprecatedBy() {
		for (DruidUnit unit : units) unit.removeDeprecatedBy();
	}

	private void removeOldMarkers() {
		try {
			for (DruidUnit unit : units)
				for (DruidFeature feature : unit.getFeatures()) {
					for (DruidMethod method : feature.getMethods()) {
						method.getMethod().getResource().deleteMarkers(DEPENDENCY_DEPRECATED, true, IResource.DEPTH_INFINITE);
						method.getMethod().getResource().deleteMarkers(FEATURE_NOT_BROKEN, true, IResource.DEPTH_INFINITE);
					}
					
					for(DruidTest test : feature.getTests()) {
							test.getMethod().getResource().deleteMarkers(FEATURE_BROKEN, true, IResource.DEPTH_INFINITE);
							test.getMethod().getResource().deleteMarkers(FAILED_TEST, true, IResource.DEPTH_INFINITE);
					}
				}
		} catch (CoreException e) {
			DruidPlugin.getPlugin().logException(e);
		}
	}

	private String getClasspath() {
		String cp = "";
		try {
			IClasspathEntry[] classpath = project.getJavaProject().getResolvedClasspath(false);
			for (IClasspathEntry classpathEntry : classpath) {
				if (cp.equals("")) cp = classpathEntry.getPath().toOSString();
				else cp += ":" + classpathEntry.getPath().toOSString();
			}
		} catch (JavaModelException e) {
			DruidPlugin.getPlugin().logException(e);
		}
		return cp;
	}

	private boolean testDeprecatedFeatures(DruidComponent component) {
		for (DruidUnit druidUnit : component.getUnits())
			for (DruidFeature feature : druidUnit.getFeatures()) {
				for (DruidDependency dependency : feature.getDependecies()) {
					if (dependency.getDependee().isDeprecated()) {
						String message = "Feature " + dependency.getDependee() + " used by " + dependency.getDependent() + " deprecated by " + dependency.getDependee().getDeprecatedBy();

						try {
							IMarker marker = dependency.getResource().createMarker(DEPENDENCY_DEPRECATED);
							marker.setAttribute(IMarker.MESSAGE, message);
							marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
							marker.setAttribute(IMarker.CHAR_START, dependency.getOffset());
							marker.setAttribute(IMarker.CHAR_END, dependency.getOffset() + dependency.getLength());
							showErrorDialog("Feature Deprecated", message, "");
						} catch (CoreException e) {
							DruidPlugin.getPlugin().logException(e);
						}

						return true;
					}
				}
			}
		return false;
	}

	public String getName() {
		return project.getElementName();
	}

	public IJavaProject getIProject() {
		return project;
	}

	public boolean addDeprecate(String unitName, ArrayList<String> featureNames, String dUnitName, String dFeatureName, IResource resource, int offset, int length) {
		DruidUnit unit = getUnit(unitName);
		if (unit == null) return false;
		DruidFeature dFeature = getFeature(dUnitName, dFeatureName);
		if (dFeature != null) unit.addDeprecates(featureNames, dFeature, resource, offset, length);
		else return false;
		return true;
	}

	public ArrayList<String> getFeatureNames(String unitName, String prefix) {
		ArrayList<String> names = new ArrayList<String>();
		Collection<DruidUnit> units = getUnits();
		for (DruidUnit unit : units) {
			Collection<DruidFeature> features = unit.getFeatures();
			for (DruidFeature feature : features) {
				String featureName = "";
				if (unit.getName().equals(unitName))
					featureName = feature.getName();
				else
					featureName = feature.getUnit().getName()+"."+feature.getName();
				if (featureName.startsWith(prefix)) names.add(featureName);
			}
		}
		return names;
	}

	public ArrayList<String> getFeatureNames(String unitName, IMethod method) throws JavaModelException {
		for (DruidUnit unit : units)
			if (unit.getName().equals(unitName)) return unit.getFeaturesWithMethod(method);
		return new ArrayList<String>();
	}
}