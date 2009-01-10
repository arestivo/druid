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

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;

public class DruidFeature {
	private String name;
	private ArrayList<DruidMethod> methods;
	private ArrayList<DruidDependency> dependencies;
	private ArrayList<DruidDeprecate> deprecates;
	private ArrayList<DruidTest> tests;
	private ArrayList<DruidDeprecate> deprecatedBy;
	private DruidUnit unit;
	
	public DruidFeature(String name, DruidUnit unit){
		setName(name);
		setUnit(unit);
		methods = new ArrayList<DruidMethod>();
		dependencies = new ArrayList<DruidDependency>();
		tests = new ArrayList<DruidTest>();
		deprecates = new ArrayList<DruidDeprecate>();
		deprecatedBy = new ArrayList<DruidDeprecate>();
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

	public void addDepends(DruidFeature feature, IResource resource, int offset, int length) {
		for (DruidDependency dependency : dependencies) {
			if (dependency.getDependee() == feature) return;
		}
		dependencies.add(new DruidDependency(feature, this, resource, offset, length));
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

	public void addDeprecates(DruidFeature feature, IResource resource, int offset, int length) {
		for (DruidDeprecate deprecate : deprecates) {
			if (deprecate.getDeprecated() == feature) return;
		}
		deprecates.add(new DruidDeprecate(feature, this, resource, offset, length));
	}

	public Collection<DruidDeprecate> getDeprecates() {
		return deprecates;		
	}

	public void cleanDeprecatedBy() {
		deprecatedBy.clear();	
	}

	public void updateDeprecatedBy() {
		for (DruidDeprecate deprecate : deprecates) {
			deprecate.getDeprecated().addDeprecatedBy(deprecate);
		}
		
	}

	private void addDeprecatedBy(DruidDeprecate deprecate) {
		deprecatedBy.add(deprecate);
	}

	public boolean isDeprecated() {
		return deprecatedBy.size() > 0;
	}

	public ArrayList<DruidDeprecate> getDeprecatedBy() {
		return deprecatedBy;
	}

	@Override
	public String toString() {
		return getUnit().getName()+"."+getName();
	}
	
}
