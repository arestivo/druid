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
