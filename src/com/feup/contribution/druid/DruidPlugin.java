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

package com.feup.contribution.druid;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.internal.resources.ProjectInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.NewAnnotationWizardPage;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.feup.contribution.druid.container.DruidClasspathContainer;
import com.feup.contribution.druid.data.DruidProject;
import com.feup.contribution.druid.data.DruidUnit;
import com.feup.contribution.druid.listeners.ProjectListener;
import com.feup.contribution.druid.view.DruidView;

@SuppressWarnings("deprecation")
public class DruidPlugin extends Plugin{
	public static final String DRUID_BUILDER = "com.feup.contribution.druid.druidbuilder";
	public static final String DRUID_NATURE = "com.feup.contribution.druid.druidnature";

	private static DruidPlugin instance;
		
	private Hashtable<String, DruidProject> projects = new Hashtable<String, DruidProject>();

	private MessageConsoleStream stream;
	private ArrayList<ProjectListener> listeners = new ArrayList<ProjectListener>();
	
	public DruidPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		instance = this;

		MessageConsole myConsole = new MessageConsole("Druid Log", null);
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { myConsole });
		stream = myConsole.newMessageStream();
	}

	public static DruidPlugin getPlugin() {
		return instance;
	}

	public void addDruidNature(IProject project) throws CoreException {
	  if (project.hasNature(DRUID_NATURE))
	    return;

	  IProjectDescription description = project.getDescription();
	  String[] ids= description.getNatureIds();
	  String[] newIds= new String[ids.length + 1];
	  System.arraycopy(ids, 0, newIds, 0, ids.length);
	  newIds[ids.length]= DRUID_NATURE;
	  description.setNatureIds(newIds);
	  project.setDescription(description, null);
	  
	  IJavaProject javaProject = (IJavaProject) JavaCore.create((IProject) project);
	  
      IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();

      List<IClasspathEntry> newEntries = new ArrayList<IClasspathEntry>(rawClasspath.length+1);            
      for(IClasspathEntry e: rawClasspath) {
    	  newEntries.add(e);
      }            
      newEntries.add(JavaCore.newContainerEntry(DruidClasspathContainer.CONTAINER_ID));

      IClasspathEntry[] newEntriesArray = new IClasspathEntry[newEntries.size()];
      newEntriesArray = (IClasspathEntry[])newEntries.toArray(newEntriesArray);
      javaProject.setRawClasspath(newEntriesArray, null);
      
      javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
	}
	
	public void removeDruidNature(IProject project) throws CoreException {
	  IProjectDescription description = project.getDescription();
	  String[] ids = description.getNatureIds();
	  for (int i = 0; i < ids.length; ++i) {
	    if (ids[i].equals(DRUID_NATURE)) {
	      String[] newIds = new String[ids.length - 1];
	      System.arraycopy(ids, 0, newIds, 0, i);
	      System.arraycopy(ids, i + 1, newIds, i, ids.length - i - 1);
	      description.setNatureIds(newIds);
	      project.setDescription(description, null);
	      return;
	    }
	  }
	}
	
	public DruidProject getProject(IJavaProject iProject) {
		if (projects.containsKey(iProject.getElementName())) return projects.get(iProject.getElementName());
		DruidProject project = new DruidProject(iProject);
		projects.put(iProject.getElementName(), project);

		for (ProjectListener listener : listeners) {
			project.addProjectListener(listener);
		}
		
		return project;
	}
	
	public void log(String message) {
		stream.println(message);
	}

	public void logException(Exception e) {
		e.printStackTrace(new PrintStream(stream));
	}

	public Collection<DruidProject> getProjects() {
		ArrayList<DruidProject> apjs = new ArrayList<DruidProject>();
		Enumeration<DruidProject> enumeration = projects.elements();
		while (enumeration.hasMoreElements()) {
			apjs.add(enumeration.nextElement());
		}
		return apjs;
	}

	public void addProjectListener(ProjectListener listener) {
		listeners.add(listener);
		Enumeration<DruidProject> enumeration = projects.elements();
		while (enumeration.hasMoreElements()) {
			DruidProject project = enumeration.nextElement();
			project.addProjectListener(listener);
		}
	}
}
