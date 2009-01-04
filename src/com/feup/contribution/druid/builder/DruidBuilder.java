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
import org.eclipse.jdt.core.IJavaProject;
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
	public static final String FEATURE_BROKEN = "com.feup.contribution.druid.featureBroken";
	
	@SuppressWarnings("unchecked")
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
				//incrementalBuild(delta, monitor);
			}
		}}
		catch (Exception e) {DruidPlugin.getPlugin().logException(e);}
		return null;
	}

	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		Vector<Dependency> dependencies = new Vector<Dependency>();
		Vector<Test> tests = new Vector<Test>();
		Vector<Deprecate> deprecates = new Vector<Deprecate>();
		delta.accept(new DeltaVisitor(dependencies, tests, deprecates));
		addDependecies(dependencies);
		addTests(tests);
		addDeprecates(deprecates);
		DruidPlugin.getPlugin().getProject(getJavaProject()).builderDone();
	}


	private void fullBuild(IProgressMonitor monitor) throws CoreException {
		Vector<Dependency> dependencies = new Vector<Dependency>();
		Vector<Test> tests = new Vector<Test>();
		Vector<Deprecate> deprecates = new Vector<Deprecate>();
		DruidPlugin.getPlugin().getProject(getJavaProject()).removeAllFeatures();
		getProject().accept(new ResourceVisitor(dependencies, tests, deprecates));
		addDependecies(dependencies);
		addTests(tests);
		addDeprecates(deprecates);
		DruidPlugin.getPlugin().getProject(getJavaProject()).builderDone();
	}

	private void addDeprecates(Vector<Deprecate> deprecates) throws CoreException {
		for (Deprecate deprecate : deprecates) {
			if (!DruidPlugin.getPlugin().getProject(getJavaProject()).addDeprecate(deprecate.getUnitName(), deprecate.getFeatureName(), deprecate.getUnit(), deprecate.getFeature(), deprecate.resource, deprecate.offset, deprecate.length)) {
				IMarker marker = deprecate.getResource().createMarker(UNDEFINED_FEATURE);
				marker.setAttribute(IMarker.MESSAGE, "Feature " + deprecate.getFeature() + " is undefined in unit " + deprecate.getUnit());
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				marker.setAttribute(IMarker.CHAR_START, deprecate.getOffset());
				marker.setAttribute(IMarker.CHAR_END, deprecate.getOffset() + deprecate.getLength());
			}
		}
	}

	public IJavaProject getJavaProject() {
		IProject project = getProject();
		IJavaProject javaProject = JavaCore.create(project);
		return javaProject;
	}
	
	private void addTests(Vector<Test> tests) throws CoreException {
		for (Test test : tests) {
			if (!DruidPlugin.getPlugin().getProject(getJavaProject()).addTest(test.getUnitName(), test.getMethod(), test.getUnit(), test.getFeature())) {
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
			if (!DruidPlugin.getPlugin().getProject(getJavaProject()).addDepends(dependency.getUnitName(), dependency.getFeatureName(), dependency.getUnit(), dependency.getFeature(), dependency.getResource(), dependency.getOffset(), dependency.getLength())) {
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

	private void checkResourceAnnotations(IResource resource, Vector<Dependency> dependencies, Vector<Test> tests, Vector<Deprecate> deprecates) throws CoreException{
		if (resource.getFileExtension().equals("java")) checkClassAnnotations(resource, dependencies, tests, deprecates);
		if (resource.getFileExtension().equals("aj")) checkAspectAnnotations(resource, dependencies, deprecates);
	}

	private void checkAspectAnnotations(IResource resource, Vector<Dependency> dependencies, Vector<Deprecate> deprecates) throws CoreException{
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
						checkAdviceAnnotations(advices[i], dependencies, deprecates);
					}
				}
			}
		}
	}
	
	private void checkAdviceAnnotations(AdviceElement advice, Vector<Dependency> dependencies, Vector<Deprecate> deprecates) throws CoreException {
		try {
			String unitName = advice.getCompilationUnit().getPackageDeclarations()[0].getElementName();
			String methodName = MethodSignatureCreator.createSignature(advice);
			IAnnotation[] annotations = AdviceAnnotationExtractor.extractAnnotations(advice);
			
			int annotationCount = 0;
			for (IAnnotation annotation : annotations) {
				if (annotation.getElementName().equals("Feature")) {
					String featureName = annotation.getMemberValuePairs()[0].getValue().toString();
					DruidPlugin.getPlugin().getProject(getJavaProject()).addFeature(unitName, advice, methodName, featureName);
					annotationCount++;
					for (IAnnotation annotation2 : annotations) {
						if (annotation2.getElementName().equals("Depends")) {
							String value = annotation2.getMemberValuePairs()[0].getValue().toString();
							String unit = extractUnit(value, unitName);
							String feature = extractFeature(value);
							dependencies.add(new Dependency(unitName, featureName, unit, feature, advice.getResource(), annotation2.getSourceRange().getOffset(), annotation2.getSourceRange().getLength()));
						}
						if (annotation2.getElementName().equals("Deprecates")) {
							String value = annotation2.getMemberValuePairs()[0].getValue().toString();
							String unit = extractUnit(value, unitName);
							String feature = extractFeature(value);
							deprecates.add(new Deprecate(unitName, featureName, unit, feature, advice.getResource(), annotation2.getSourceRange().getOffset(), annotation2.getSourceRange().getLength()));
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
				} catch (Exception e) {DruidPlugin.getPlugin().logException(e);}
			}
			
		} catch (JavaModelException e) {DruidPlugin.getPlugin().logException(e);}
	}

	private void checkClassAnnotations(IResource resource, Vector<Dependency> dependencies, Vector<Test> tests, Vector<Deprecate> deprecates) throws CoreException{
		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(resource);
		IJavaElement je = (IJavaElement) JavaCore.create(resource);
		
		resource.deleteMarkers(NO_ANNOTATION_MARKER, true, IResource.DEPTH_INFINITE);
		resource.deleteMarkers(UNDEFINED_FEATURE, true, IResource.DEPTH_INFINITE);
						
		DruidPlugin.getPlugin().getProject(getJavaProject()).removeClass(cu.getPackageDeclarations()[0].getElementName(), je);
		
		try {
			IType[] types = cu.getTypes();
			for (IType type : types) {
				IMethod[] methods = type.getMethods();
				for (IMethod method : methods) {
					checkMethodAnnotations(method, dependencies, tests, deprecates);
				}
			}
		} catch (JavaModelException e) {DruidPlugin.getPlugin().logException(e);}		
	}

	private void checkMethodAnnotations(IMethod method, Vector<Dependency> dependencies, Vector<Test> tests, Vector<Deprecate> deprecates) throws CoreException {
		//if (!Flags.isPublic(method.getFlags())) return;
		try {
			String unitName = method.getCompilationUnit().getPackageDeclarations()[0].getElementName();
			String methodName = MethodSignatureCreator.createSignature(method);
			IAnnotation[] annotations = method.getAnnotations();
						
			int annotationCount = 0;
			for (IAnnotation annotation : annotations) {
				if (annotation.getElementName().equals("Feature")) {
					String featureName = annotation.getMemberValuePairs()[0].getValue().toString();
					DruidPlugin.getPlugin().getProject(getJavaProject()).addFeature(unitName, method, methodName, featureName);
					annotationCount++;
					for (IAnnotation annotation2 : annotations) {
						if (annotation2.getElementName().equals("Depends")) {
							String value = annotation2.getMemberValuePairs()[0].getValue().toString();
							String unit = extractUnit(value, unitName);
							String feature = extractFeature(value);
							dependencies.add(new Dependency(unitName, featureName, unit, feature,method.getResource(), annotation2.getSourceRange().getOffset(), annotation2.getSourceRange().getLength()));
						}
						if (annotation2.getElementName().equals("Deprecates")) {
							String value = annotation2.getMemberValuePairs()[0].getValue().toString();
							String unit = extractUnit(value, unitName);
							String feature = extractFeature(value);
							deprecates.add(new Deprecate(unitName, featureName, unit, feature,method.getResource(), annotation2.getSourceRange().getOffset(), annotation2.getSourceRange().getLength()));
						}
					}
				}
				if (annotation.getElementName().equals("Tests")) {
					String value = annotation.getMemberValuePairs()[0].getValue().toString();
					
					String unit = extractUnit(value, unitName);
					String feature = extractFeature(value);
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
				} catch (Exception e) {DruidPlugin.getPlugin().logException(e);}
			}
			
		} catch (JavaModelException e) {DruidPlugin.getPlugin().logException(e);}
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
		Vector<Dependency> dependencies;
		Vector<Test> tests;
		Vector<Deprecate> deprecates;
		
		public DeltaVisitor(Vector<Dependency> dependencies, Vector<Test> tests, Vector<Deprecate> deprecates) {
			this.dependencies = dependencies;
			this.tests = tests;
			this.deprecates = deprecates;
		}

		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (resource.getFileExtension() == null) return true;
			if (!resource.getFileExtension().equals("java") && !resource.getFileExtension().equals("aj")) return true;
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				checkResourceAnnotations(resource, dependencies, tests, deprecates);
				break;
			case IResourceDelta.REMOVED:
				removeResource(resource);
				break;
			case IResourceDelta.CHANGED:
				removeResource(resource);
				checkResourceAnnotations(resource, dependencies, tests, deprecates);
				break;
			}
			return true;
		}

		private void removeResource(IResource resource) {
			try {
				String unitName = ((ICompilationUnit)JavaCore.create(resource)).getPackageDeclarations()[0].getElementName();
				IJavaElement je = (IJavaElement) JavaCore.create(resource);

				DruidPlugin.getPlugin().getProject(getJavaProject()).removeClass(unitName,je);
			} catch (JavaModelException e) {
				DruidPlugin.getPlugin().logException(e);
			}
		}
	}

	class ResourceVisitor implements IResourceVisitor {
		Vector<Dependency> dependencies;
		Vector<Test> tests;
		Vector<Deprecate> deprecates;
		
		public ResourceVisitor(Vector<Dependency> dependencies, Vector<Test> tests, Vector<Deprecate> deprecates) {
			this.dependencies = dependencies;
			this.tests = tests;
			this.deprecates = deprecates;
		}

		public boolean visit(IResource resource) {
			try {
				if (resource.getFileExtension() != null && (resource.getFileExtension().equals("java") || resource.getFileExtension().equals("aj"))) 
					checkResourceAnnotations(resource, dependencies, tests, deprecates);
			} catch (JavaModelException e) {
				DruidPlugin.getPlugin().logException(e);
			} catch (CoreException e) {
				DruidPlugin.getPlugin().logException(e);
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

	class Deprecate {
		private String unitName;
		private String featureName; 
		private String unit; 
		private String feature;
		private IResource resource;
		private int offset;
		private int length;
	
		public Deprecate(String unitName, String featureName, String unit, String feature, IResource resource, int offset, int length){
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
