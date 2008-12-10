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

import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class AddFeatureAnnotationProposal implements IJavaCompletionProposal{
	private IInvocationContext context;

	public AddFeatureAnnotationProposal(IInvocationContext context) {
		this.context = context;
	}

	public int getRelevance() {
		return 10;
	}

	public void apply(IDocument document) {
		try {
			int start = context.getCoveringNode().getParent().getStartPosition();
			document.replace(start, 0, "@Feature(\"\")\n\t");
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public String getAdditionalProposalInfo() {
		return "Adds a @Feature annotation that can be used to declare features implemented by the method.";
	}

	public IContextInformation getContextInformation() {
		return null;
	}

	public String getDisplayString() {
		return "Add Feature Annotation";
	}

	public Image getImage() {
		return null;
	}

	public Point getSelection(IDocument document) {
		return null;
	}

}
