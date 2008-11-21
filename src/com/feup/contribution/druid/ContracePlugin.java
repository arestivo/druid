package com.feup.contribution.druid;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.feup.contribution.druid.container.ContraceClasspathContainer;
import com.feup.contribution.druid.data.ContraceProject;

@SuppressWarnings("deprecation")
public class ContracePlugin extends Plugin{
	public static final String CONTRACE_BUILDER = "com.feup.contribution.contrace.contracebuilder";
	public static final String CONTRACE_NATURE = "com.feup.contribution.contrace.contracenature";

	private static ContracePlugin instance;
		
	private ContraceProject project;

	private MessageConsoleStream stream;
	
	public ContracePlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		project = new ContraceProject();
		instance = this;

		MessageConsole myConsole = new MessageConsole("Contrace Log", null);
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { myConsole });
		stream = myConsole.newMessageStream();
	}

	public static ContracePlugin getPlugin() {
		return instance;
	}

	public void addContraceNature(IProject project) throws CoreException {
	  if (project.hasNature(CONTRACE_NATURE))
	    return;

	  IProjectDescription description = project.getDescription();
	  String[] ids= description.getNatureIds();
	  String[] newIds= new String[ids.length + 1];
	  System.arraycopy(ids, 0, newIds, 0, ids.length);
	  newIds[ids.length]= CONTRACE_NATURE;
	  description.setNatureIds(newIds);
	  project.setDescription(description, null);
	  
	  IJavaProject javaProject = (IJavaProject) JavaCore.create((IProject) project);
	  
      IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();

      List<IClasspathEntry> newEntries = new ArrayList<IClasspathEntry>(rawClasspath.length+1);            
      for(IClasspathEntry e: rawClasspath) {
    	  newEntries.add(e);
      }            
      newEntries.add(JavaCore.newContainerEntry(ContraceClasspathContainer.CONTAINER_ID));

      IClasspathEntry[] newEntriesArray = new IClasspathEntry[newEntries.size()];
      newEntriesArray = (IClasspathEntry[])newEntries.toArray(newEntriesArray);
      javaProject.setRawClasspath(newEntriesArray, null);
      
      javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
	}
	
	public void removeContraceNature(IProject project) throws CoreException {
	  IProjectDescription description = project.getDescription();
	  String[] ids = description.getNatureIds();
	  for (int i = 0; i < ids.length; ++i) {
	    if (ids[i].equals(CONTRACE_NATURE)) {
	      String[] newIds = new String[ids.length - 1];
	      System.arraycopy(ids, 0, newIds, 0, i);
	      System.arraycopy(ids, i + 1, newIds, i, ids.length - i - 1);
	      description.setNatureIds(newIds);
	      project.setDescription(description, null);
	      return;
	    }
	  }
	}
	
	public ContraceProject getProject() {
		return project;
	}
	
	public void log(String message) {
		stream.println(message);
	}
}
