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

package com.feup.contribution.druid.tester;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import com.feup.contribution.druid.DruidPlugin;
import com.feup.contribution.druid.data.DruidComponent;
import com.feup.contribution.druid.data.DruidUnit;

public class DruidTester {

	public void setUpTest(ArrayList<DruidComponent> components) {
		try {
			Runtime.getRuntime().exec("rm -Rf /tmp/druid").waitFor();
		} catch (IOException e) {
			DruidPlugin.getPlugin().logException(e);
		} catch (InterruptedException e) {
			DruidPlugin.getPlugin().logException(e);
		}
		new File("/tmp/druid/src/").mkdirs();
		for (DruidComponent component : components) {
			for (DruidUnit unit : component.getUnits()) {
				String unitname = unit.getName();
				unitname = unitname.replace('.', '/');
				String workspacepath = Platform.getLocation().toOSString();
				String unitpath = unit.getProject().getIProject().getPath().toOSString();
				
				new File("/tmp/druid/src/" + unitname + "/").mkdirs();
				File dir = new File(workspacepath + unitpath + "/src/" + unitname);
				String[] files = dir.list();
				for (String file : files) {
					File source = new File(workspacepath + unitpath + "/src/" + unitname, file); 
					File dest = new File("/tmp/druid/src/" + unitname, file); 
					try {
						copy(source, dest);
					} catch (IOException e) {
						DruidPlugin.getPlugin().logException(e);
					}
				}
			}
		}
	}
	
	public void compile(String classpath) {
		try {
			Runtime.getRuntime().exec("rm -Rf /tmp/druid/bin").waitFor();
			
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/tmp/druid/compile.sh")));
			bw.write("ajc -d /tmp/druid/bin -source 1.5  -sourceroots /tmp/druid/src/ -verbose -classpath \"" + classpath + "\"");
			bw.close();
			
			new File("/tmp/druid/compile.sh").setExecutable(true);
					
			Runtime.getRuntime().exec("/tmp/druid/compile.sh").waitFor();
		} catch (IOException e) {
			DruidPlugin.getPlugin().logException(e);
		} catch (InterruptedException e) {
			DruidPlugin.getPlugin().logException(e);
		}
	}

	private void copy(File source, File dest) throws IOException {
		if (!source.getName().endsWith(".java") && !source.getName().endsWith(".aj")) return;
		
		InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(dest);
        
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();	}

	public void tearDown() {
	}

	public boolean test(IMethod method, String classpath) {
		try {
			String unitname = method.getCompilationUnit().getPackageDeclarations()[0].getElementName();

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/tmp/druid/bin/test.sh")));
			bw.write("export CLASSPATH=" + classpath + "\ncd /tmp/druid/bin/\njunit -m " + unitname + "." + method.getParent().getElementName() + "." + method.getElementName());
			bw.close();

			new File("/tmp/druid/bin/test.sh").setExecutable(true);
			
			Process p = Runtime.getRuntime().exec("/tmp/druid/bin/test.sh");
			
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = br.readLine()) != null)
				if (line.contains(new String("OK (1 test)"))) return true;
			
			p.waitFor();
		} catch (JavaModelException e) {
			DruidPlugin.getPlugin().logException(e);
		} catch (IOException e) {
			DruidPlugin.getPlugin().logException(e);
		} catch (InterruptedException e) {
			DruidPlugin.getPlugin().logException(e);
		}
		return false;
	}

}
