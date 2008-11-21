package com.feup.contribution.druid.data;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;

public class ContraceUnit {
	private String name;
	private ArrayList<ContraceFeature> features;
	private ContraceProject project;
	
	public ContraceUnit(String unitName, ContraceProject project){
		setName(unitName);
		setProject(project);
		features = new ArrayList<ContraceFeature>();
	}

	private void setProject(ContraceProject project) {
		this.project = project;
	}

	private void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void removeClass(IJavaElement javaElement) {
		ArrayList<ContraceFeature> toRemove = new ArrayList<ContraceFeature>();
		for (ContraceFeature feature : features) {
			feature.removeClass(javaElement);
			if (feature.isEmpty()) toRemove.add(feature);
		}
		for (ContraceFeature feature : toRemove) features.remove(feature);
	}

	public boolean isEmpty() {
		return features.isEmpty();
	}

	public void addFeature(IMethod iMethod, String methodName, String featureName) {
		for (ContraceFeature feature : features) {
			if (feature.getName().equals(featureName)) {
				feature.addMethod(iMethod, methodName);
				return;
			}
		}
		ContraceFeature feature = new ContraceFeature(featureName, this);
		feature.addMethod(iMethod, methodName);
		features.add(feature);
	}

	public Collection<ContraceFeature> getFeatures() {
		return features;
	}

	public ContraceProject getProject() {
		return project;
	}

	public void addDepends(String featureName, ContraceFeature dFeature) {
		for (ContraceFeature feature : features) {
			if (feature.getName().equals(featureName)) {
				feature.addDepends(dFeature);
				return;
			}
		}
		ContraceFeature feature = new ContraceFeature(featureName, this);
		feature.addDepends(dFeature);
		features.add(feature);
	}

	public ContraceFeature getFeature(String featureName) {
		for (ContraceFeature feature : features) {
			if (feature.getName().equals(featureName)) return feature;
		}
		return null;
	}
}
