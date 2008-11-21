package com.feup.contribution.druid.view;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.feup.contribution.druid.data.ContraceDependency;
import com.feup.contribution.druid.data.ContraceFeature;
import com.feup.contribution.druid.data.ContraceMethod;
import com.feup.contribution.druid.data.ContraceProject;
import com.feup.contribution.druid.data.ContraceTest;
import com.feup.contribution.druid.data.ContraceUnit;

public class ContraceContentProvider implements IContentProvider, ITreeContentProvider {

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof ContraceProject) {
			ContraceProject project = (ContraceProject)parentElement;
			return project.getUnits().toArray();
		}
		if(parentElement instanceof ContraceUnit) {
			ContraceUnit unit = (ContraceUnit)parentElement;
			return unit.getFeatures().toArray();
		}
		if(parentElement instanceof ContraceFeature) {
			ContraceFeature feature = (ContraceFeature)parentElement;
			Collection<Object> children = new ArrayList<Object>();
			children.addAll(feature.getDependecies());
			children.addAll(feature.getMethods());
			children.addAll(feature.getTests());
			return children.toArray();
		}
		return new Object[0];
	}

	public Object getParent(Object element) {
		if(element instanceof ContraceProject) {
			return null;
		}
		if(element instanceof ContraceUnit) {
			ContraceUnit unit = (ContraceUnit) element;
			return unit.getProject();
		}
		if(element instanceof ContraceFeature) {
			ContraceFeature feature = (ContraceFeature) element;
			return feature.getUnit();
		}
		if(element instanceof ContraceMethod) {
			ContraceMethod method = (ContraceMethod) element;
			return method.getFeature();
		}
		if (element instanceof ContraceDependency) {
			ContraceDependency dependency = (ContraceDependency) element;
			return dependency.getDependent();
		}
		if (element instanceof ContraceTest) {
			ContraceTest test = (ContraceTest) element;
			return test.getFeature();
		}
		return new Object[0];
	}

	public boolean hasChildren(Object element) {
		if(element instanceof ContraceProject) {
			ContraceProject project = (ContraceProject)element;
			return project.getUnits().size() > 0;
		}
		if(element instanceof ContraceUnit) {
			ContraceUnit unit = (ContraceUnit) element;
			return unit.getFeatures().size() > 0;
		}
		if(element instanceof ContraceFeature) {
			ContraceFeature feature = (ContraceFeature) element;
			return feature.getMethods().size() > 0 || feature.getDependecies().size() > 0 || feature.getTests().size() > 0;
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

}
