package com.feup.contribution.druid.assist;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;

import com.feup.contribution.druid.builder.DruidBuilder;

public class FeatureQuickAssistProcessor implements IQuickAssistProcessor{

	public IJavaCompletionProposal[] getAssists(IInvocationContext context,
			IProblemLocation[] locations) throws CoreException {
		IJavaCompletionProposal[] proposals = new IJavaCompletionProposal[1];
		IMarker[] markers = context.getCompilationUnit().getResource().findMarkers(DruidBuilder.NO_ANNOTATION_MARKER, true, IResource.DEPTH_INFINITE);
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			int ims = (int) marker.getAttribute(IMarker.CHAR_START, -1);
			int ime = (int) marker.getAttribute(IMarker.CHAR_END, -1);
			int nsp = context.getCoveringNode().getStartPosition();
			if (nsp>=ims && nsp <= ime) {
				proposals[0] = new AddFeatureAnnotationProposal(context);
				return proposals;
			}
		}
		return null;
	}

	public boolean hasAssists(IInvocationContext context) throws CoreException {
		IMarker[] markers = context.getCompilationUnit().getResource().findMarkers(DruidBuilder.NO_ANNOTATION_MARKER, true, IResource.DEPTH_INFINITE);
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			int ims = (int) marker.getAttribute(IMarker.CHAR_START, -1);
			int ime = (int) marker.getAttribute(IMarker.CHAR_END, -1);
			int nsp = context.getCoveringNode().getStartPosition();
			if (nsp>=ims && nsp <= ime) return true;
		}
		return false;
	}

}
