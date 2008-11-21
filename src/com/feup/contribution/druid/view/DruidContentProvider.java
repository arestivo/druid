package com.feup.contribution.druid.view;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.feup.contribution.druid.data.DruidDependency;
import com.feup.contribution.druid.data.DruidFeature;
import com.feup.contribution.druid.data.DruidMethod;
import com.feup.contribution.druid.data.DruidProject;
import com.feup.contribution.druid.data.DruidTest;
import com.feup.contribution.druid.data.DruidUnit;

public class DruidContentProvider implements IContentProvider, ITreeContentProvider {

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof DruidProject) {
			DruidProject project = (DruidProject)parentElement;
			return project.getUnits().toArray();
		}
		if(parentElement instanceof DruidUnit) {
			DruidUnit unit = (DruidUnit)parentElement;
			return unit.getFeatures().toArray();
		}
		if(parentElement instanceof DruidFeature) {
			DruidFeature feature = (DruidFeature)parentElement;
			Collection<Object> children = new ArrayList<Object>();
			children.addAll(feature.getDependecies());
			children.addAll(feature.getMethods());
			children.addAll(feature.getTests());
			return children.toArray();
		}
		return new Object[0];
	}

	public Object getParent(Object element) {
		if(element instanceof DruidProject) {
			return null;
		}
		if(element instanceof DruidUnit) {
			DruidUnit unit = (DruidUnit) element;
			return unit.getProject();
		}
		if(element instanceof DruidFeature) {
			DruidFeature feature = (DruidFeature) element;
			return feature.getUnit();
		}
		if(element instanceof DruidMethod) {
			DruidMethod method = (DruidMethod) element;
			return method.getFeature();
		}
		if (element instanceof DruidDependency) {
			DruidDependency dependency = (DruidDependency) element;
			return dependency.getDependent();
		}
		if (element instanceof DruidTest) {
			DruidTest test = (DruidTest) element;
			return test.getFeature();
		}
		return new Object[0];
	}

	public boolean hasChildren(Object element) {
		if(element instanceof DruidProject) {
			DruidProject project = (DruidProject)element;
			return project.getUnits().size() > 0;
		}
		if(element instanceof DruidUnit) {
			DruidUnit unit = (DruidUnit) element;
			return unit.getFeatures().size() > 0;
		}
		if(element instanceof DruidFeature) {
			DruidFeature feature = (DruidFeature) element;
			return feature.getMethods().size() > 0 || feature.getDependecies().size() > 0 || feature.getTests().size() > 0;
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

}
