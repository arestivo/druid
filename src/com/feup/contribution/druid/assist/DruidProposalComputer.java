package com.feup.contribution.druid.assist;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

public class DruidProposalComputer implements IJavaCompletionProposalComputer{
	private final DruidProposalProcessor fProcessor = new DruidProposalProcessor();
	
	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
        return Arrays.asList(fProcessor.computeCompletionProposals(context.getViewer(), context.getInvocationOffset()));
	}

	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		  return Arrays.asList(fProcessor.computeContextInformation(context.getViewer(), context.getInvocationOffset()));
	}

	@Override
	public String getErrorMessage() {
		return fProcessor.getErrorMessage();
	}

	@Override
	public void sessionEnded() {
	}

	@Override
	public void sessionStarted() {
	}
}
