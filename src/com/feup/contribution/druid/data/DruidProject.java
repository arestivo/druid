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

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import com.feup.contribution.druid.DruidPlugin;
import com.feup.contribution.druid.listeners.ProjectListener;
import com.feup.contribution.druid.tester.DruidTester;

public class DruidProject{
	private ArrayList<DruidUnit> units;
	private ArrayList<ProjectListener> listeners = new ArrayList<ProjectListener>();
	private IJavaProject project;
	
	public static final String FEATURE_BROKEN = "com.feup.contribution.druid.featureBroken";
	public static final String UNDEFINED_FEATURE = "com.feup.contribution.druid.undefinedFeature";
	
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

	public void removeClass(String unitName, IJavaElement javaElement) {
		ArrayList<DruidUnit> toRemove = new ArrayList<DruidUnit>();
		for (DruidUnit unit : units) {
			if (unit.getName().equals(unitName)) {
				unit.removeClass(javaElement);
				if (unit.isEmpty()) toRemove.add(unit);
			}
		}
		for (DruidUnit unit : toRemove) units.remove(unit); 
	}
	
	private DruidUnit getUnit(String unitName) {
		for (DruidUnit unit : units) {
			if (unit.getName().equals(unitName)) return unit;
		}
		return null;
	}

	public void builderDone(){
		for (ProjectListener listener : listeners) {
			listener.projectChanged(this);
		}
		drawDiagram();
	}
	
	public void addProjectListener(ProjectListener listener) {
		listeners.add(listener);
	}

	public void addFeature(String unitName, IMethod iMethod, String methodName, String featureName) {
		DruidUnit unit = getUnit(unitName);
		if (unit == null) {
			unit = new DruidUnit(unitName, this);
			units.add(unit);
		}
		unit.addFeature(iMethod, methodName, featureName);
	}

	public void removeAllFeatures() {
		units.clear();
	}

	public boolean addDepends(String unitName, String featureName, String dUnitName, String dFeatureName) {
		DruidUnit unit = getUnit(unitName);
		if (unit == null) {
			unit = new DruidUnit(unitName, this);
			units.add(unit);
		}
		DruidFeature dFeature = getFeature(dUnitName, dFeatureName);
		if (dFeature != null) unit.addDepends(featureName, dFeature);
		else return false;
		return true;
	}

	private DruidFeature getFeature(String unitName, String featureName) {
		DruidUnit unit = getUnit(unitName);
		if (unit == null) return null;
		return unit.getFeature(featureName);
	}

	public boolean addTest(String unitName, IMethod method, String tUnitName, String tFeatureName) {
		DruidFeature tFeature = getFeature(tUnitName, tFeatureName);
		if (tFeature != null) tFeature.addTests(method);
		else return false;
		return true;
	}

	public void detectInteractions() {
		ArrayList<DruidComponent> components = DruidComponent.getOrderedComponents(units);
		ArrayList<DruidComponent> toCompile = new ArrayList<DruidComponent>();

		DruidTester tester = new DruidTester();

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
		
		for (DruidComponent component : components) {			
			toCompile.add(component);
			for (DruidComponent druidComponent : toCompile) {
				tester.setUpTest(toCompile);
				tester.compile(cp);
				for (DruidUnit druidUnit : druidComponent.getUnits()) {
					for (DruidFeature druidFeature : druidUnit.getFeatures()) {
						for (DruidTest druidTest : druidFeature.getTests()) {
							boolean result = tester.test(druidTest.getMethod(), cp);
							if (!result) {
								String message = "";
								if (component.getUnits().size() == 1) message = "Unit " + component.getUnits().get(0).getName() + " breaks feature " + druidFeature.getName();
								else message = "Units " + component.getUnits() + " break feature " + druidFeature.getName();
								try {
									druidTest.getMethod().getResource().deleteMarkers(FEATURE_BROKEN, true, IResource.DEPTH_INFINITE);
									IMarker marker = druidTest.getMethod().getResource().createMarker(FEATURE_BROKEN);
									marker.setAttribute(IMarker.MESSAGE, message);
									marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
									marker.setAttribute(IMarker.CHAR_START, druidTest.getMethod().getNameRange().getOffset());
									marker.setAttribute(IMarker.CHAR_END, druidTest.getMethod().getNameRange().getOffset() + druidTest.getMethod().getNameRange().getLength());
									DruidPlugin.getPlugin().log(marker.exists()?"Exists":"Doesn't Exist");
								} catch (CoreException e) {
									DruidPlugin.getPlugin().logException(e);
								}
								return;
							}
						}
					}
				}
				tester.tearDown();
			}
		}
	}

	public String getName() {
		return project.getElementName();
	}

	public IJavaProject getIProject() {
		return project;
	}

	public void drawDiagram() {
		String workspacepath = Platform.getLocation().toOSString();
		String unitpath = getIProject().getPath().toOSString();
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(workspacepath + unitpath + "/druid.dot")));
			bw.write("graph \"druid\" {\n");
			bw.write("  node [ fontname = \"Trebuchet\", label = \"\\N\"]\n");
			bw.write("  node [ shape = \"component\", color = \"blue\"]\n");

			for (DruidUnit unit : units) {
				bw.write("    \"" + unit.getName() + "\"\n");
			}

			bw.write("  node [ shape = \"egg\", color=\"green\"]\n");

			for (DruidUnit unit : units) {
				for (DruidFeature feature : unit.getFeatures()) {
					bw.write("    \"" + feature.getName() + "\"\n");
				}
			}
			
			bw.write("  edge [ color = \"black\", arrowhead=\"dot\" ]\n");
			for (DruidUnit unit : units) {
				for (DruidFeature feature : unit.getFeatures()) {
					bw.write("    \"" + unit.getName() + "\" -- \"" + feature.getName() + "\"\n");
				}
			}

			bw.write("  edge [ color = \"green\", arrowhead=\"box\" ]\n");
			for (DruidUnit unit : units) {
				for (DruidFeature feature : unit.getFeatures()) {
					for (DruidDependency dependency : feature.getDependecies()) {
						bw.write("    \"" + feature.getName() + "\" -- \"" + dependency.getDependee().getName() + "\"\n");
					}
				}
			}

			
			bw.write("}\n");
			bw.close();
		} catch (FileNotFoundException e) {
			DruidPlugin.getPlugin().logException(e);
		} catch (IOException e) {
			DruidPlugin.getPlugin().logException(e);
		}
	}
}