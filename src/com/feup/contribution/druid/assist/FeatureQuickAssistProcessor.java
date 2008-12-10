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
