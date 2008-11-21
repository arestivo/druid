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
