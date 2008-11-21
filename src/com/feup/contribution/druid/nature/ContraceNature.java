package com.feup.contribution.druid.nature;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import com.feup.contribution.druid.ContracePlugin;

public class ContraceNature implements IProjectNature {
	private IProject project;

	public void configure() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i)
			if (commands[i].getBuilderName().equals(
					ContracePlugin.CONTRACE_BUILDER))
				return;

		ICommand command = description.newCommand();
		command.setBuilderName(ContracePlugin.CONTRACE_BUILDER);
		
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
			if (commands[i].getBuilderName().equals(ContracePlugin.CONTRACE_BUILDER)) 
				newsize--;

		if (commands.length == newsize) return;
		ICommand[] newCommands = new ICommand[newsize];

		int pos = 0;
		for (int i = 0; i < commands.length; ++i)
			if (!commands[i].getBuilderName().equals(ContracePlugin.CONTRACE_BUILDER))
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
