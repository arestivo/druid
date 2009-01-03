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

package com.feup.contribution.druid.view;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.feup.contribution.druid.data.DruidDependency;
import com.feup.contribution.druid.data.DruidDeprecate;
import com.feup.contribution.druid.data.DruidFeature;
import com.feup.contribution.druid.data.DruidMethod;
import com.feup.contribution.druid.data.DruidProject;
import com.feup.contribution.druid.data.DruidTest;
import com.feup.contribution.druid.data.DruidUnit;
import com.feup.contribution.druid.util.MethodSignatureCreator;

public class DruidLabelProvider extends LabelProvider {	
	@Override
	public Image getImage(Object element) {
		if (element instanceof DruidProject) return SharedImages.getImage("project.gif"); 
		if (element instanceof DruidTest) return SharedImages.getImage("test.gif"); 
		if (element instanceof DruidUnit) return SharedImages.getImage("package_obj.gif"); 
		if (element instanceof DruidFeature) return SharedImages.getImage("feature_obj.gif");
		if (element instanceof DruidDependency) return SharedImages.getImage("depends.gif");
		if (element instanceof DruidDeprecate) return SharedImages.getImage("deprecates.gif");
		if (element instanceof DruidMethod) {
			DruidMethod method = (DruidMethod) element;
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
		if (element instanceof DruidProject) {
			DruidProject project = (DruidProject) element;
			return project.getName();
		}
		if (element instanceof DruidUnit) {
			DruidUnit unit = (DruidUnit) element;
			return unit.getName(); 
		}
		if (element instanceof DruidFeature) {
			DruidFeature feature = (DruidFeature) element;
			return feature.getName(); 
		}
		if (element instanceof DruidMethod) {
			DruidMethod method = (DruidMethod) element;
			return method.getMethodName(); 
		}
		if (element instanceof DruidDependency) {
			DruidDependency dependency = (DruidDependency) element;
			return dependency.getDependee().getName();
		}
		if (element instanceof DruidDeprecate) {
			DruidDeprecate deprecate = (DruidDeprecate) element;
			return deprecate.getDeprecated().getName();
		}
		if (element instanceof DruidTest) {
			DruidTest test = (DruidTest) element;
			try {
				return MethodSignatureCreator.createSignature(test.getMethod());
			} catch (JavaModelException e) {}
		}
		return null;
	}
}
