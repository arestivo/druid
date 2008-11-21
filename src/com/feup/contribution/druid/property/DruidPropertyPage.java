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

import com.feup.contribution.druid.DruidPlugin;

public class DruidPropertyPage extends PropertyPage{
	private Button druidButton;

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
		  label.setText("Activate Druid. Someday an inteligent explanation will appear here.");
		  label.setFont(font);
		  druidButton = new Button(composite, SWT.CHECK);
		  druidButton.setText("Druid");
		  druidButton.setFont(font);
		  return composite;
		}
	
	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		Control composite = addControl(parent);
		try {
			druidButton.setSelection(getProject().hasNature(DruidPlugin.DRUID_NATURE));
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return composite;
	}
	
	@Override
	public boolean performOk() {
		  try {
		    DruidPlugin plugin= DruidPlugin.getPlugin();
		    if (druidButton.getSelection())
		      plugin.addDruidNature(getProject());
		    else
		      plugin.removeDruidNature(getProject());
		  } catch (CoreException e) {
		    e.printStackTrace();
		  }
		  return true;
		}

}
