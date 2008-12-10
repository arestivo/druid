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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;

public class DruidUnit {
	private String name;
	private ArrayList<DruidFeature> features;
	private DruidProject project;
	
	public DruidUnit(String unitName, DruidProject project){
		setName(unitName);
		setProject(project);
		features = new ArrayList<DruidFeature>();
	}

	private void setProject(DruidProject project) {
		this.project = project;
	}

	private void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void removeClass(IJavaElement javaElement) {
		ArrayList<DruidFeature> toRemove = new ArrayList<DruidFeature>();
		for (DruidFeature feature : features) {
			feature.removeClass(javaElement);
			if (feature.isEmpty()) toRemove.add(feature);
		}
		for (DruidFeature feature : toRemove) features.remove(feature);
	}

	public boolean isEmpty() {
		return features.isEmpty();
	}

	public void addFeature(IMethod iMethod, String methodName, String featureName) {
		for (DruidFeature feature : features) {
			if (feature.getName().equals(featureName)) {
				feature.addMethod(iMethod, methodName);
				return;
			}
		}
		DruidFeature feature = new DruidFeature(featureName, this);
		feature.addMethod(iMethod, methodName);
		features.add(feature);
	}

	public Collection<DruidFeature> getFeatures() {
		return features;
	}

	public DruidProject getProject() {
		return project;
	}

	public void addDepends(String featureName, DruidFeature dFeature) {
		for (DruidFeature feature : features) {
			if (feature.getName().equals(featureName)) {
				feature.addDepends(dFeature);
				return;
			}
		}
		DruidFeature feature = new DruidFeature(featureName, this);
		feature.addDepends(dFeature);
		features.add(feature);
	}

	public DruidFeature getFeature(String featureName) {
		for (DruidFeature feature : features) {
			if (feature.getName().equals(featureName)) return feature;
		}
		return null;
	}
}
