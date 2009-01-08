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

package com.feup.contribution.druid.builder;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.feup.contribution.druid.DruidPlugin;
import com.feup.contribution.druid.data.DruidProject;
import com.feup.contribution.druid.util.MethodSignatureCreator;

public class DruidBuilder extends IncrementalProjectBuilder {
	private ArrayList<BuilderAnnotation> postBuildAnnotations = new ArrayList<BuilderAnnotation>();
	
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
	throws CoreException {
		try{
			if (kind == FULL_BUILD) {
				fullBuild(monitor);
			} else {
				IResourceDelta delta = getDelta(getProject());
				if (delta == null) {
					fullBuild(monitor);
				} else {
					fullBuild(monitor); // This shouldn't be a full build
				}
			}
		} catch (Exception e) {DruidPlugin.getPlugin().logException(e);}
		return null;
	}

	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		delta.accept(new DeltaVisitor());
		DruidPlugin.getPlugin().getProject(getJavaProject()).builderDone();
	}


	private void fullBuild(IProgressMonitor monitor) throws CoreException {
		DruidPlugin.getPlugin().getProject(getJavaProject()).removeAllFeatures();
		getProject().accept(new ResourceVisitor());
		processPostBuildAnnotations();
		DruidPlugin.getPlugin().getProject(getJavaProject()).builderDone();
	}

	public IJavaProject getJavaProject() {
		IProject project = getProject();
		IJavaProject javaProject = JavaCore.create(project);
		return javaProject;
	}
	
	private void checkResourceAnnotations(IResource resource) throws CoreException{
		if (resource.getFileExtension().equals("java")) checkClassAnnotations(resource);
		if (resource.getFileExtension().equals("aj")) checkAspectAnnotations(resource);
	}

	@SuppressWarnings("restriction")
	private void checkAspectAnnotations(IResource resource) throws CoreException{
		IFile file = (IFile) resource;
		ICompilationUnit cu = AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(file);
		ICompilationUnit mappedUnit = AJCompilationUnitManager.mapToAJCompilationUnit(cu);
		
		if (mappedUnit instanceof AJCompilationUnit) {
			AJCompilationUnit ajUnit = (AJCompilationUnit) mappedUnit;
			for (IType type : ajUnit.getAllTypes()){
				if (type instanceof AspectElement) {
					AspectElement aspect = (AspectElement) type;
					IMethod[] methods = aspect.getMethods();
					for (IMethod method : methods) {
						IAnnotation[] annotations = MethodAnnotationExtractor.extractAnnotations(method);
						compileAnnotations(method, annotations);
					}
				}
			}
		}
	}
	
	private void checkClassAnnotations(IResource resource) throws CoreException{
		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(resource);
		IJavaElement je = (IJavaElement) JavaCore.create(resource);
		
		DruidPlugin.getPlugin().getProject(getJavaProject()).removeClass(cu.getPackageDeclarations()[0].getElementName(), je);
		
		try {
			IType[] types = cu.getTypes();
			for (IType type : types) {
				IMethod[] methods = type.getMethods();
				for (IMethod method : methods) {
					IAnnotation[] annotations = MethodAnnotationExtractor.extractAnnotations(method);
					compileAnnotations(method, annotations);
				}
			}
		} catch (JavaModelException e) {DruidPlugin.getPlugin().logException(e);}
	}

	private void compileAnnotations(IMethod method, IAnnotation[] annotations) throws JavaModelException {
		DruidProject project = DruidPlugin.getPlugin().getProject(getJavaProject());
	    String unitName = method.getCompilationUnit().getPackageDeclarations()[0].getElementName();		
	    
	    for (IAnnotation annotation : annotations) {
	    	if (annotation.getMemberValuePairs().length != 1) continue;
	    	String annotationType = annotation.getElementName();
		    String value = annotation.getMemberValuePairs()[0].getValue().toString();
			String feature = extractFeature(value);
		    
			if (annotationType.equals("Feature")) project.addFeature(unitName, method, feature);
			if (annotationType.equals("Depends") || annotationType.equals("Tests") || annotationType.equals("Deprecates")) 
				postBuildAnnotations.add(new BuilderAnnotation(annotation, method));
		}
	}

	private void processPostBuildAnnotations() throws JavaModelException {
		DruidProject project = DruidPlugin.getPlugin().getProject(getJavaProject());
		for (BuilderAnnotation bAnnotation : postBuildAnnotations) {
			IAnnotation annotation = bAnnotation.getAnnotation();
			IMethod method = bAnnotation.getMethod();

			String annotationType = annotation.getElementName();
		    String unitName = method.getCompilationUnit().getPackageDeclarations()[0].getElementName();		
			
		    String value = annotation.getMemberValuePairs()[0].getValue().toString();
			String feature = extractFeature(value);
			String unit = extractUnit(value, unitName);
			
			int offset = annotation.getSourceRange().getOffset();
			int length = annotation.getSourceRange().getLength();

			ArrayList<String> featureNames = project.getFeatureNames(unitName, method);
			
			if (annotationType.equals("Depends")) project.addDepends(unitName, featureNames, unit, feature, method.getResource(), offset, length);
			if (annotationType.equals("Tests")) project.addTest(method, unit, feature);
			if (annotationType.equals("Deprecates")) project.addDeprecate(unitName, featureNames, unit, feature, method.getResource(), offset, length);
		}

		postBuildAnnotations.clear();
	}
	
	private String extractFeature(String value) {
		if (!value.contains(".")) return value;
		return value.substring(value.lastIndexOf('.') + 1);
	}

	private String extractUnit(String value, String unitName) {
		if (!value.contains(".")) return unitName;
		return value.substring(0,value.lastIndexOf('.'));
	}

	class DeltaVisitor implements IResourceDeltaVisitor {
		@Override
		public boolean visit(IResourceDelta arg0) throws CoreException {
			return false;
		}
	}

	class ResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			try {
				if (resource.getFileExtension() != null && (resource.getFileExtension().equals("java") || resource.getFileExtension().equals("aj"))) 
					checkResourceAnnotations(resource);
			} catch (JavaModelException e) {
				DruidPlugin.getPlugin().logException(e);
			} catch (CoreException e) {
				DruidPlugin.getPlugin().logException(e);
			}
			return true;
		}
	}
}
