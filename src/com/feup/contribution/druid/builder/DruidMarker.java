package com.feup.contribution.druid.builder;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;

public class DruidMarker {
	public static final String NO_ANNOTATION_MARKER = "com.feup.contribution.druid.noAnnotationWarning";
	public static final String UNDEFINED_FEATURE = "com.feup.contribution.druid.undefinedFeature";
	public static final String FEATURE_BROKEN = "com.feup.contribution.druid.featureBroken";

	public static void removeBuildMarkers(IResource resource) throws CoreException {
		resource.deleteMarkers(DruidMarker.NO_ANNOTATION_MARKER, true, IResource.DEPTH_INFINITE);
		resource.deleteMarkers(DruidMarker.UNDEFINED_FEATURE, true, IResource.DEPTH_INFINITE);
	}
	
	public static void addNoAnnotationMarker(IMethod method) throws CoreException {
		IMarker marker = method.getResource().createMarker(NO_ANNOTATION_MARKER);
        marker.setAttribute(IMarker.MESSAGE, "Method doesn't provide features");
        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
        marker.setAttribute(IMarker.CHAR_START, method.getNameRange().getOffset());
        marker.setAttribute(IMarker.CHAR_END, method.getNameRange().getOffset() + method.getNameRange().getLength());
	}
}
