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

package com.feup.contribution.druid.nature;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import com.feup.contribution.druid.DruidPlugin;

public class DruidNature implements IProjectNature {
	private IProject project;

	public void configure() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i)
			if (commands[i].getBuilderName().equals(
					DruidPlugin.DRUID_BUILDER))
				return;

		ICommand command = description.newCommand();
		command.setBuilderName(DruidPlugin.DRUID_BUILDER);
		
		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
		newCommands[newCommands.length - 1] = command;
		
		description.setBuildSpec(newCommands);
		getProject().setDescription(description, null);
	}

	public void deconfigure() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		int newsize = commands.length;
		for (int i = 0; i < commands.length; ++i)
			if (commands[i].getBuilderName().equals(DruidPlugin.DRUID_BUILDER)) 
				newsize--;

		if (commands.length == newsize) return;
		ICommand[] newCommands = new ICommand[newsize];

		int pos = 0;
		for (int i = 0; i < commands.length; ++i)
			if (!commands[i].getBuilderName().equals(DruidPlugin.DRUID_BUILDER))
				newCommands[pos++] = commands[i];

		description.setBuildSpec(newCommands);
		getProject().setDescription(description, null);
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

}
