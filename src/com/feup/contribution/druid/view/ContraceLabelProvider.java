package com.feup.contribution.druid.view;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.feup.contribution.druid.data.ContraceDependency;
import com.feup.contribution.druid.data.ContraceFeature;
import com.feup.contribution.druid.data.ContraceMethod;
import com.feup.contribution.druid.data.ContraceProject;
import com.feup.contribution.druid.data.ContraceTest;
import com.feup.contribution.druid.data.ContraceUnit;
import com.feup.contribution.druid.util.MethodSignatureCreator;

public class ContraceLabelProvider extends LabelProvider {	
	@Override
	public Image getImage(Object element) {
		if (element instanceof ContraceTest) return SharedImages.getImage("test.gif"); 
		if (element instanceof ContraceUnit) return SharedImages.getImage("package_obj.gif"); 
		if (element instanceof ContraceFeature) return SharedImages.getImage("feature_obj.gif");
		if (element instanceof ContraceDependency) return SharedImages.getImage("depends.gif");
		if (element instanceof ContraceMethod) {
			ContraceMethod method = (ContraceMethod) element;
			try {
				if (Flags.isPublic(method.getMethod().getFlags())) return SharedImages.getImage("methpub_obj.gif");
				else if (Flags.isPrivate(method.getMethod().getFlags())) return SharedImages.getImage("methpri_obj.gif");
				else return SharedImages.getImage("methpro_obj.gif");
			} catch (JavaModelException e) { e.printStackTrace(); }
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ContraceProject) {
			return "Project"; 
		}
		if (element instanceof ContraceUnit) {
			ContraceUnit unit = (ContraceUnit) element;
			return unit.getName(); 
		}
		if (element instanceof ContraceFeature) {
			ContraceFeature feature = (ContraceFeature) element;
			return feature.getName(); 
		}
		if (element instanceof ContraceMethod) {
			ContraceMethod method = (ContraceMethod) element;
			return method.getMethodName(); 
		}
		if (element instanceof ContraceDependency) {
			ContraceDependency dependency = (ContraceDependency) element;
			return dependency.getDependee().getName();
		}
		if (element instanceof ContraceTest) {
			ContraceTest test = (ContraceTest) element;
			try {
				return MethodSignatureCreator.createSignature(test.getMethod());
			} catch (JavaModelException e) {}
		}
		return null;
	}
}
