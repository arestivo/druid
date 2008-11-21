package com.feup.contribution.druid.data;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;

public class ContraceFeature {
	private String name;
	private ArrayList<ContraceMethod> methods;
	private ArrayList<ContraceDependency> dependencies;
	private ArrayList<ContraceTest> tests;
	private ContraceUnit unit;
	
	public ContraceFeature(String name, ContraceUnit unit){
		setName(name);
		setUnit(unit);
		methods = new ArrayList<ContraceMethod>();
		dependencies = new ArrayList<ContraceDependency>();
		tests = new ArrayList<ContraceTest>();
	}

	private void setUnit(ContraceUnit unit) {
		this.unit = unit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void removeClass(IJavaElement javaElement) {
		ArrayList<ContraceMethod> toRemove = new ArrayList<ContraceMethod>(); 
		for (ContraceMethod method : methods)
			if (method.getMethod().getResource().equals(javaElement.getResource())) toRemove.add(method);
		for (ContraceMethod method : toRemove)
			methods.remove(method);
	}

	public boolean isEmpty() {
		return methods.isEmpty();
	}

	public void addMethod(IMethod iMethod, String methodName) {
		methods.add(new ContraceMethod(iMethod, methodName, this));		
	}

	public Collection<ContraceMethod> getMethods() {
		return methods;
	}

	public ContraceUnit getUnit() {
		return unit;
	}

	public void addDepends(ContraceFeature feature) {
		dependencies.add(new ContraceDependency(feature, this));
		
	}

	public Collection<ContraceDependency> getDependecies() {
		return dependencies;		
	}

	public void addTests(IMethod method) {
		ContraceTest exists = getTest(method);
		if (exists != null) return;
		ContraceTest test = new ContraceTest(method, this);
		tests.add(test);
	}

	private ContraceTest getTest(IMethod method) {
		for (ContraceTest test : tests) {
			if (test.getMethod().equals(method)) return test;
		}
		return null;
	}

	public Collection<ContraceTest> getTests() {
		return tests;
	}
	
}
