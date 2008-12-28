package com.feup.contribution.druid.data;

import java.util.ArrayList;
import java.util.Stack;

import com.feup.contribution.druid.DruidPlugin;

public class DruidComponent {
	ArrayList<DruidUnit> componentUnits = new ArrayList<DruidUnit>();

	public static ArrayList<DruidComponent> getComponents(ArrayList<DruidUnit> units) {
		DruidPlugin.getPlugin().log("Detecting SCC");
		DruidPlugin.getPlugin().log("units: " + units.size());
		int index[], lowlink[];
		int i = 0;
		index = new int[units.size()];
		lowlink = new int[units.size()];
		boolean instack[];
		instack = new boolean[units.size()];
		for (int v = 0; v < units.size(); v++) {
			index[v] = -1; instack[v] = false;
		}
		ArrayList<DruidComponent> components = new ArrayList<DruidComponent>();
		Stack<DruidUnit> s = new Stack<DruidUnit>();
		for (int v = 0; v < units.size(); v++) {
			if (index[v]==-1) i = dfs(units, v, i, index, lowlink, s, instack, components);
			DruidPlugin.getPlugin().log(v + " " + components.size());
		}	
		return components;
	}
	
	private static int dfs(ArrayList<DruidUnit> units, int v, int i, int[] index, int[] lowlink, Stack<DruidUnit> s, boolean[] instack, ArrayList<DruidComponent> components) {
		DruidPlugin.getPlugin().log("DFS " + v);
		index[v] = i;
		lowlink[v] = i++;
		s.push(units.get(v)); instack[v] = true;
		ArrayList<DruidUnit> depends = units.get(v).getDependsOnUnit();
		for (int e = 0; e < depends.size(); e++){
			int w = units.indexOf(depends.get(e));
			DruidPlugin.getPlugin().log(" e " + w);
			if (index[w]==-1) {
				dfs(units, w, i, index, lowlink, s, instack, components);
				lowlink[v] = Math.min(lowlink[v], lowlink[w]);
			} else if (instack[w]) lowlink[v] = Math.min(lowlink[v], index[w]);
		}
		if (lowlink[v] == index[v]) {
			DruidPlugin.getPlugin().log("LL == I");
			DruidComponent newComponent = new DruidComponent();
			while (!s.empty()){
				DruidUnit wUnit = s.pop(); instack[units.indexOf(wUnit)] = false;
				newComponent.addUnit(wUnit);
				if (wUnit==units.get(v)) break;
			}
			components.add(newComponent);
		}
		DruidPlugin.getPlugin().log("DFS END " + v);
		return i;
	}

	private void addUnit(DruidUnit unit) {
		componentUnits.add(unit);
	}
	
	@Override
	public String toString() {
		String s = "Component:";
		for (DruidUnit unit : componentUnits) {
			s += " " + unit.getName();
		}
		return s;
	}
}
