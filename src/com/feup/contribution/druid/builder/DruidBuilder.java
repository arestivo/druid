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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.feup.contribution.druid.DruidPlugin;
import com.feup.contribution.druid.util.AdviceAnnotationExtractor;
import com.feup.contribution.druid.util.MethodSignatureCreator;

public class DruidBuilder extends IncrementalProjectBuilder {

	public static final String NO_ANNOTATION_MARKER = "com.feup.contribution.druid.noAnnotationWarning";
	public static final String UNDEFINED_FEATURE = "com.feup.contribution.druid.undefinedFeature";
	
	@SuppressWarnings("unchecked")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
	throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		Vector<Dependency> dependencies = new Vector<Dependency>();
		Vector<Test> tests = new Vector<Test>();
		delta.accept(new DeltaVisitor(dependencies, tests));
		addDependecies(dependencies);
		addTests(tests);
		DruidPlugin.getPlugin().getProject(getProject()).builderDone();
	}


	private void fullBuild(IProgressMonitor monitor) throws CoreException {
		Vector<Dependency> dependencies = new Vector<Dependency>();
		Vector<Test> tests = new Vector<Test>();
		DruidPlugin.getPlugin().getProject(getProject()).removeAllFeatures();
		getProject().accept(new ResourceVisitor(dependencies, tests));
		addDependecies(dependencies);
		addTests(tests);
		DruidPlugin.getPlugin().getProject(getProject()).builderDone();
	}
	
	private void addTests(Vector<Test> tests) throws CoreException {
		for (Test test : tests) {
			if (!DruidPlugin.getPlugin().getProject(getProject()).addTest(test.getUnitName(), test.getMethod(), test.getUnit(), test.getFeature())) {
				IMarker marker = test.getResource().createMarker(UNDEFINED_FEATURE);
				marker.setAttribute(IMarker.MESSAGE, "Feature " + test.getFeature() + " is undefined in unit " + test.getUnit());
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				marker.setAttribute(IMarker.CHAR_START, test.getOffset());
				marker.setAttribute(IMarker.CHAR_END, test.getOffset() + test.getLength());
			}
		}
	}

	private void addDependecies(Vector<Dependency> dependencies) throws CoreException {
		for (Dependency dependency : dependencies) {
			if (!DruidPlugin.getPlugin().getProject(getProject()).addDepends(dependency.getUnitName(), dependency.getFeatureName(), dependency.getUnit(), dependency.getFeature())) {
				IMarker marker = dependency.getResource().createMarker(UNDEFINED_FEATURE);
				marker.setAttribute(IMarker.MESSAGE, "Feature " + dependency.getFeature() + " is undefined in unit " + dependency.getUnit());
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				marker.setAttribute(IMarker.CHAR_START, dependency.getOffset());
				marker.setAttribute(IMarker.CHAR_END, dependency.getOffset() + dependency.getLength());
			}
		}
	}

	private void openView() throws CoreException {
		IWorkbenchWindow dw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = dw.getActivePage();
		page.showView("com.feup.contribution.druid.view");
	}

	private void checkResourceAnnotations(IResource resource, Vector<Dependency> dependencies, Vector<Test> tests) throws CoreException{
		if (resource.getFileExtension().equals("java")) checkClassAnnotations(resource, dependencies, tests);
		if (resource.getFileExtension().equals("aj")) checkAspectAnnotations(resource, dependencies);
	}

	private void checkAspectAnnotations(IResource resource, Vector<Dependency> dependencies) throws CoreException{
		IFile file = (IFile) resource;
		ICompilationUnit cu = AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(file);
		ICompilationUnit mappedUnit = AJCompilationUnitManager.mapToAJCompilationUnit(cu);

		resource.deleteMarkers(NO_ANNOTATION_MARKER, true, IResource.DEPTH_INFINITE);
		resource.deleteMarkers(UNDEFINED_FEATURE, true, IResource.DEPTH_INFINITE);
		
		if (mappedUnit instanceof AJCompilationUnit) {
			AJCompilationUnit ajUnit = (AJCompilationUnit) mappedUnit;
			for (IType type : ajUnit.getAllTypes()){
				if (type instanceof AspectElement) {
					AspectElement aspect = (AspectElement) type;
					AdviceElement[] advices = aspect.getAdvice();
					for (int i = 0; i < advices.length; i++) {
						checkAdviceAnnotations(advices[i], dependencies);
					}
				}
			}
		}
	}
	
	private void checkAdviceAnnotations(AdviceElement advice, Vector<Dependency> dependencies) throws CoreException {
		try {
			String unitName = advice.getCompilationUnit().getPackageDeclarations()[0].getElementName();
			String methodName = MethodSignatureCreator.createSignature(advice);
			IAnnotation[] annotations = AdviceAnnotationExtractor.extractAnnotations(advice);
			
			int annotationCount = 0;
			for (IAnnotation annotation : annotations) {
				if (annotation.getElementName().equals("Feature")) {
					String featureName = annotation.getMemberValuePairs()[0].getValue().toString();
					DruidPlugin.getPlugin().getProject(getProject()).addFeature(unitName, advice, methodName, featureName);
					annotationCount++;
					for (IAnnotation annotation2 : annotations) {
						if (annotation2.getElementName().equals("Depends")) {
							String unit = annotation2.getMemberValuePairs()[0].getValue().toString();
							String feature = annotation2.getMemberValuePairs()[1].getValue().toString();
							dependencies.add(new Dependency(unitName, featureName, unit, feature, advice.getResource(), annotation2.getSourceRange().getOffset(), annotation2.getSourceRange().getLength()));
						}
					}
				}
			}
			
			if (annotationCount == 0) {
				try {
					IMarker marker = advice.getResource().createMarker(NO_ANNOTATION_MARKER);
					marker.setAttribute(IMarker.MESSAGE, "Method doesn't provide features");
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
					marker.setAttribute(IMarker.CHAR_START, advice.getNameRange().getOffset());
					marker.setAttribute(IMarker.CHAR_END, advice.getNameRange().getOffset() + advice.getNameRange().getLength());
				} catch (Exception e) {e.printStackTrace();}
			}
			
		} catch (JavaModelException e) {e.printStackTrace();}
	}

	private void checkClassAnnotations(IResource resource, Vector<Dependency> dependencies, Vector<Test> tests) throws CoreException{
		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(resource);
		IJavaElement je = (IJavaElement) JavaCore.create(resource);
		
		resource.deleteMarkers(NO_ANNOTATION_MARKER, true, IResource.DEPTH_INFINITE);
		resource.deleteMarkers(UNDEFINED_FEATURE, true, IResource.DEPTH_INFINITE);
						
		DruidPlugin.getPlugin().getProject(getProject()).removeClass(cu.getPackageDeclarations()[0].getElementName(), je);
		
		try {
			IType[] types = cu.getTypes();
			for (IType type : types) {
				IMethod[] methods = type.getMethods();
				for (IMethod method : methods) {
					checkMethodAnnotations(method, dependencies, tests);
				}
			}
		} catch (JavaModelException e) {e.printStackTrace();}		
	}

	private void checkMethodAnnotations(IMethod method, Vector<Dependency> dependencies, Vector<Test> tests) throws CoreException {
		//if (!Flags.isPublic(method.getFlags())) return;
		try {
			String unitName = method.getCompilationUnit().getPackageDeclarations()[0].getElementName();
			String methodName = MethodSignatureCreator.createSignature(method);
			IAnnotation[] annotations = method.getAnnotations();
						
			int annotationCount = 0;
			for (IAnnotation annotation : annotations) {
				if (annotation.getElementName().equals("Feature")) {
					String featureName = annotation.getMemberValuePairs()[0].getValue().toString();
					DruidPlugin.getPlugin().getProject(getProject()).addFeature(unitName, method, methodName, featureName);
					annotationCount++;
					for (IAnnotation annotation2 : annotations) {
						if (annotation2.getElementName().equals("Depends")) {
							String unit = annotation2.getMemberValuePairs()[0].getValue().toString();
							String feature = annotation2.getMemberValuePairs()[1].getValue().toString();
							dependencies.add(new Dependency(unitName, featureName, unit, feature,method.getResource(), annotation2.getSourceRange().getOffset(), annotation2.getSourceRange().getLength()));
						}
					}
				}
				if (annotation.getElementName().equals("Tests")) {
					String unit = annotation.getMemberValuePairs()[0].getValue().toString();
					String feature = annotation.getMemberValuePairs()[1].getValue().toString();
					tests.add(new Test(unitName, method, unit, feature, method.getResource(), annotation.getSourceRange().getOffset(), annotation.getSourceRange().getLength()));
				}
			}
			if (annotationCount == 0 &&	!method.getParent().getElementName().contains("Test")) {
				try {
					IMarker marker = method.getResource().createMarker(NO_ANNOTATION_MARKER);
					marker.setAttribute(IMarker.MESSAGE, "Method doesn't provide features");
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
					marker.setAttribute(IMarker.CHAR_START, method.getNameRange().getOffset());
					marker.setAttribute(IMarker.CHAR_END, method.getNameRange().getOffset() + method.getNameRange().getLength());
				} catch (Exception e) {e.printStackTrace();}
			}
			
		} catch (JavaModelException e) {e.printStackTrace();}
	}

	class DeltaVisitor implements IResourceDeltaVisitor {
		Vector<Dependency> dependencies;
		Vector<Test> tests;
		
		public DeltaVisitor(Vector<Dependency> dependencies, Vector<Test> tests) {
			this.dependencies = dependencies;
			this.tests = tests;
		}

		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (resource.getFileExtension() == null) return true;
			if (!resource.getFileExtension().equals("java") && !resource.getFileExtension().equals("aj")) return true;
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				checkResourceAnnotations(resource, dependencies, tests);
				break;
			case IResourceDelta.REMOVED:
				removeResource(resource);
				break;
			case IResourceDelta.CHANGED:
				removeResource(resource);
				checkResourceAnnotations(resource, dependencies, tests);
				break;
			}
			return true;
		}

		private void removeResource(IResource resource) {
			try {
				String unitName = ((ICompilationUnit)JavaCore.create(resource)).getPackageDeclarations()[0].getElementName();
				IJavaElement je = (IJavaElement) JavaCore.create(resource);

				DruidPlugin.getPlugin().getProject(getProject()).removeClass(unitName,je);
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
	}

	class ResourceVisitor implements IResourceVisitor {
		Vector<Dependency> dependencies;
		Vector<Test> tests;
		
		public ResourceVisitor(Vector<Dependency> dependencies, Vector<Test> tests) {
			this.dependencies = dependencies;
			this.tests = tests;
		}

		public boolean visit(IResource resource) {
			try {
				if (resource.getFileExtension() != null && (resource.getFileExtension().equals("java") || resource.getFileExtension().equals("aj"))) 
					checkResourceAnnotations(resource, dependencies, tests);
			} catch (JavaModelException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return true;
		}
	}
	
	class Dependency {
		private String unitName;
		private String featureName; 
		private String unit; 
		private String feature;
		private IResource resource;
		private int offset;
		private int length;
	
		public Dependency(String unitName, String featureName, String unit, String feature, IResource resource, int offset, int length){
			this.unitName = unitName;
			this.featureName = featureName;
			this.unit = unit;
			this.feature = feature;
			this.resource = resource;
			this.offset = offset;
			this.length = length;
		}

		public String getUnitName() {
			return unitName;
		}

		public String getFeatureName() {
			return featureName;
		}

		public String getUnit() {
			return unit;
		}

		public String getFeature() {
			return feature;
		}

		public IResource getResource() {
			return resource;
		}

		public int getOffset() {
			return offset;
		}

		public int getLength() {
			return length;
		}
	}
	
	class Test {
		private String unitName;
		private IMethod method; 
		private String unit; 
		private String feature;
		private IResource resource;
		private int offset;
		private int length;
	
		public Test(String unitName, IMethod method, String unit, String feature, IResource resource, int offset, int length){
			this.unitName = unitName;
			this.method = method;
			this.unit = unit;
			this.feature = feature;
			this.resource = resource;
			this.offset = offset;
			this.length = length;
		}

		public String getUnitName() {
			return unitName;
		}

		public IMethod getMethod() {
			return method;
		}

		public String getUnit() {
			return unit;
		}

		public String getFeature() {
			return feature;
		}

		public IResource getResource() {
			return resource;
		}

		public int getOffset() {
			return offset;
		}

		public int getLength() {
			return length;
		}
	}
}
