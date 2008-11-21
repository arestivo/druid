package com.feup.contribution.druid.property;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;

import com.feup.contribution.druid.ContracePlugin;

public class ContracePropertyPage extends PropertyPage{
	private Button contraceButton;

	private IProject getProject() {
		  return (IProject) getElement();
	}
	
	private Control addControl(Composite parent) {
		  Composite composite= new Composite(parent, SWT.NULL);
		  GridLayout layout= new GridLayout();
		  layout.numColumns= 1;
		  composite.setLayout(layout);
		  GridData data= new GridData();
		  data.verticalAlignment= GridData.FILL;
		  data.horizontalAlignment= GridData.FILL;
		  composite.setLayoutData(data);

		  Font font= parent.getFont();
		  Label label= new Label(composite, SWT.NONE);
		  label.setText("Activate Contrace. Someday an inteligent explanation will appear here.");
		  label.setFont(font);
		  contraceButton = new Button(composite, SWT.CHECK);
		  contraceButton.setText("Contrace");
		  contraceButton.setFont(font);
		  return composite;
		}
	
	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		Control composite = addControl(parent);
		try {
			contraceButton.setSelection(getProject().hasNature(ContracePlugin.CONTRACE_NATURE));
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return composite;
	}
	
	@Override
	public boolean performOk() {
		  try {
		    ContracePlugin plugin= ContracePlugin.getPlugin();
		    if (contraceButton.getSelection())
		      plugin.addContraceNature(getProject());
		    else
		      plugin.removeContraceNature(getProject());
		  } catch (CoreException e) {
		    e.printStackTrace();
		  }
		  return true;
		}

}
