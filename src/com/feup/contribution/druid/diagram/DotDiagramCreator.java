package com.feup.contribution.druid.diagram;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.eclipse.core.runtime.Platform;

import com.feup.contribution.druid.DruidPlugin;
import com.feup.contribution.druid.data.DruidDependency;
import com.feup.contribution.druid.data.DruidDeprecate;
import com.feup.contribution.druid.data.DruidFeature;
import com.feup.contribution.druid.data.DruidProject;
import com.feup.contribution.druid.data.DruidUnit;

public class DotDiagramCreator {
	DruidProject project;
	
	public DotDiagramCreator(DruidProject project) {
		this.project = project;
	}
	
	public void drawDiagram() {
		String workspacepath = Platform.getLocation().toOSString();
		String unitpath = project.getIProject().getPath().toOSString();
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(workspacepath + unitpath + "/druid.dot")));
			bw.write("graph \"druid\" {\n");
			bw.write("  node [ fontname = \"Trebuchet\", label = \"\\N\"]\n");
			bw.write("  node [ shape = \"component\", color = \"blue\"]\n");

			for (DruidUnit unit : project.getUnits()) {
				bw.write("    \"u:" + unit.getName() + "\" [label=\""+unit.getName()+"\"]\n");
			}

			bw.write("  node [ shape = \"egg\", color=\"green\"]\n");

			for (DruidUnit unit : project.getUnits()) {
				for (DruidFeature feature : unit.getFeatures()) {
					bw.write("    \"" + unit.getName()+":"+feature.getName() + "\" [label=\""+feature.getName()+"\"]\n");
				}
			}
			
			bw.write("  edge [ color = \"black\", arrowhead=\"dot\" ]\n");
			for (DruidUnit unit : project.getUnits()) {
				for (DruidFeature feature : unit.getFeatures()) {
					bw.write("    \"u:" + unit.getName() + "\" -- \"" + unit.getName()+":"+feature.getName() + "\"\n");
				}
			}

			bw.write("  edge [ color = \"green\", arrowhead=\"box\" ]\n");
			for (DruidUnit unit : project.getUnits()) {
				for (DruidFeature feature : unit.getFeatures()) {
					for (DruidDependency dependency : feature.getDependecies()) {
						bw.write("    \"" + unit.getName()+":"+feature.getName() + "\" -- \"" + dependency.getDependee().getUnit().getName()+":"+dependency.getDependee().getName() + "\"\n");
					}
				}
			}

			bw.write("  edge [ color = \"orange\", arrowhead=\"diamond\" ]\n");
			for (DruidUnit unit : project.getUnits()) {
				for (DruidFeature feature : unit.getFeatures()) {
					for (DruidDeprecate deprecate : feature.getDeprecates()) {
						bw.write("    \"" + unit.getName()+":"+feature.getName() + "\" -- \"" + deprecate.getDeprecated().getUnit().getName()+":"+deprecate.getDeprecated().getName() + "\"\n");
					}
				}
			}			
			
			bw.write("}\n");
			bw.close();
			createPngFile();
		} catch (FileNotFoundException e) {
			DruidPlugin.getPlugin().logException(e);
		} catch (IOException e) {
			DruidPlugin.getPlugin().logException(e);
		}
	}

	private void createPngFile() throws IOException{
		String workspacepath = Platform.getLocation().toOSString();
		String unitpath = project.getIProject().getPath().toOSString();
		
		Process p = Runtime.getRuntime().exec(new String[]{"dot", workspacepath+unitpath+"/druid.dot", "-Tpng", "-o"+workspacepath+unitpath+"/druid.png"});
		try {
			p.waitFor();
		} catch (InterruptedException e) {}
	}
	
}
