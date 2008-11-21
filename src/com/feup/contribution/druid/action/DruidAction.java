package com.feup.contribution.druid.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class DruidAction implements IWorkbenchWindowActionDelegate{

	public void dispose() {
	}

	public void init(IWorkbenchWindow workbench) {
	}

	public void run(IAction action) {
		MessageDialog.openInformation(null, null,
	    "Hello, Eclipse world");
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
