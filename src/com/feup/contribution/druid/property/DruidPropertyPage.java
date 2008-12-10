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
