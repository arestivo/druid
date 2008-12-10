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

package com.feup.contribution.druid.container;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;

public class DruidClasspathContainer implements IClasspathContainer {
    public static final Path CONTAINER_ID = new Path("DRUID_CONTAINER");
    private IClasspathEntry[] _entries;

	@SuppressWarnings("deprecation")
	public DruidClasspathContainer() throws IOException{
        List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
        Bundle bundle = Platform.getBundle("com.feup.contribution.druid");
        URL entry = FileLocator.getBundleFile(bundle).toURL();
        String rootPath = FileLocator.toFileURL(entry).getPath(); 
        //String rootPath = "/Applications/eclipse/plugins/com.feup.contribution.druid_1.0.0.jar";
        IClasspathEntry cpe = JavaCore.newLibraryEntry(new Path(rootPath), null, null);
        entries.add(cpe);
        IClasspathEntry[] cpes = new IClasspathEntry[entries.size()];
        _entries = (IClasspathEntry[])entries.toArray(cpes);
	}
	
	public IClasspathEntry[] getClasspathEntries() {
		return _entries;
	}

	public String getDescription() {
		return "Druid Library";
	}

	public int getKind() {
		return K_APPLICATION;
	}

	public IPath getPath() {
		return CONTAINER_ID;
	}

}
