package com.feup.contribution.druid.data;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;

public class DruidFeature {
	private String name;
	private ArrayList<DruidMethod> methods;
	private ArrayList<DruidDependency> dependencies;
	private ArrayList<DruidTest> tests;
	private DruidUnit unit;
	
	public DruidFeature(String name, DruidUnit unit){
		setName(name);
		setUnit(unit);
		methods = new ArrayList<DruidMethod>();
		dependencies = new ArrayList<DruidDependency>();
		tests = new ArrayList<DruidTest>();
	}

	private void setUnit(DruidUnit unit) {
		this.unit = unit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void removeClass(IJavaElement javaElement) {
		ArrayList<DruidMethod> toRemove = new ArrayList<DruidMethod>(); 
		for (DruidMethod method : methods)
			if (method.getMethod().getResource().equals(javaElement.getResource())) toRemove.add(method);
		for (DruidMethod method : toRemove)
			methods.remove(method);
	}

	public boolean isEmpty() {
		return methods.isEmpty();
	}

	public void addMethod(IMethod iMethod, String methodName) {
		methods.add(new DruidMethod(iMethod, methodName, this));		
	}

	public Collection<DruidMethod> getMethods() {
		return methods;
	}

	public DruidUnit getUnit() {
		return unit;
	}

	public void addDepends(DruidFeature feature) {
		dependencies.add(new DruidDependency(feature, this));
		
	}

	public Collection<DruidDependency> getDependecies() {
		return dependencies;		
	}

	public void addTests(IMethod method) {
		DruidTest exists = getTest(method);
		if (exists != null) return;
		DruidTest test = new DruidTest(method, this);
		tests.add(test);
	}

	private DruidTest getTest(IMethod method) {
		for (DruidTest test : tests) {
			if (test.getMethod().equals(method)) return test;
		}
		return null;
	}

	public Collection<DruidTest> getTests() {
		return tests;
	}
	
}