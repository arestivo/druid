package com.feup.contribution.druid.assist;

import java.util.ArrayList;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.internal.Workbench;

import com.feup.contribution.druid.DruidPlugin;
import com.feup.contribution.druid.data.DruidProject;

public class DruidProposalProcessor implements IContentAssistProcessor{
    private static final ICompletionProposal[] NO_PROPOSALS = new ICompletionProposal[0];
    private static final IContextInformation[] NO_CONTEXTS = new IContextInformation[0];

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		try {
			IRegion region = viewer.getDocument().getLineInformationOfOffset(offset);
			
			String line = viewer.getDocument().get(region.getOffset(), region.getLength());
			String annotations[] = {"@Depends(\"","@Feature(\"","@Tests(\"", "@Deprecates(\""}; 
			for (String annotation : annotations) {
				if (line.contains(annotation)) {
					int anStart = line.indexOf('\"') + 1;
					int cursor = viewer.getSelectedRange().x - region.getOffset();
					if (cursor < anStart) return NO_PROPOSALS;
					
					while (line.substring(anStart,cursor).contains(",")) anStart = line.indexOf(',', anStart) + 1;
					
					DruidPlugin.getPlugin().log("AN: " + anStart);
					
					String prefix = line.substring(anStart, cursor);
					prefix = prefix.trim();
					
					IEditorInput editorInput = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
					String name = editorInput.toString();
					String unitName = editorInput.toString();
							
					name = name.substring(name.indexOf('/') + 1);
					name = name.substring(0,name.indexOf('/'));

					DruidProject project = DruidPlugin.getPlugin().getProject(name);
					if (project == null) return NO_PROPOSALS;
					
					unitName = unitName.substring(unitName.indexOf('/') + 1);
					unitName = unitName.substring(unitName.indexOf('/') + 1);
					unitName = unitName.substring(unitName.indexOf('/') + 1);
					unitName = unitName.substring(0,unitName.lastIndexOf('/'));
					unitName = unitName.replaceAll("/", ".");
					
					ArrayList<String> names = project.getFeatureNames(unitName, prefix);
					ICompletionProposal proposals[] = new ICompletionProposal[names.size()];

					int i = 0;
					for (String uName : names) {
						proposals[i++] = new CompletionProposal(uName, offset-prefix.length(), prefix.length() + viewer.getSelectedRange().y, uName.length());
					}
					
					return proposals;
				}
			}
		} catch (BadLocationException e) {
			DruidPlugin.getPlugin().logException(e);
		}
		return NO_PROPOSALS;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer arg0, int arg1) {
		return NO_CONTEXTS;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

}
