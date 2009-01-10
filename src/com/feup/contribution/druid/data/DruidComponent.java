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

package com.feup.contribution.druid.data;

import java.util.ArrayList;
import java.util.Stack;

public class DruidComponent {
	private ArrayList<DruidUnit> componentUnits = new ArrayList<DruidUnit>();
	private ArrayList<DruidComponent> dependants = new ArrayList<DruidComponent>();
	private int indegree = 0;
	
	public ArrayList<DruidUnit> getUnits() {
		return componentUnits;
	}
	
	public static ArrayList<DruidComponent> getOrderedComponents(ArrayList<DruidUnit> units) {
		ArrayList<DruidComponent> components = getComponents(units);
		ArrayList<DruidComponent> orderedComponents = new ArrayList<DruidComponent>();
		
		Stack<DruidComponent> s = new Stack<DruidComponent>();
		
		for (DruidComponent component : components) 
			for (DruidUnit unit : component.getUnits())
				for (DruidUnit dependent : unit.getDependsOnUnit())
					for (DruidComponent dComponent : components)
						if (!component.equals(dComponent) && dComponent.contains(dependent)) dComponent.addDependency(component);		

		for (DruidComponent component : components) 
			for (DruidComponent dComponent : component.getDependents())
				dComponent.increaseIndegree();

		for (DruidComponent component : components) 
			if (component.getIndegree() == 0)
				s.add(component);
	
		while (!s.isEmpty()) {
			DruidComponent c = s.pop();
			orderedComponents.add(c);
			for (DruidComponent d : c.getDependents())
				if (d.decreaseIndegree() == 0 && !orderedComponents.contains(d)) s.add(d);
		}
		
		return orderedComponents;
	}
	
	private int decreaseIndegree() {
		return --indegree;
	}

	private int getIndegree() {
		return indegree;
	}

	private void increaseIndegree() {
		indegree++;
	}

	private ArrayList<DruidComponent> getDependents() {
		return dependants;
	}

	private void addDependency(DruidComponent component) {
		dependants.add(component);
	}

	private boolean contains(DruidUnit unit) {
		return componentUnits.contains(unit);
	}

	public static ArrayList<DruidComponent> getComponents(ArrayList<DruidUnit> units) {
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
		}	
		
		return components;
	}
	
	private static int dfs(ArrayList<DruidUnit> units, int v, int i, int[] index, int[] lowlink, Stack<DruidUnit> s, boolean[] instack, ArrayList<DruidComponent> components) {
		index[v] = i;
		lowlink[v] = i++;
		s.push(units.get(v)); instack[v] = true;
		ArrayList<DruidUnit> depends = units.get(v).getDependsOnUnit();
		for (int e = 0; e < depends.size(); e++){
			int w = units.indexOf(depends.get(e));
			if (index[w]==-1) {
				dfs(units, w, i, index, lowlink, s, instack, components);
				lowlink[v] = Math.min(lowlink[v], lowlink[w]);
			} else if (instack[w]) lowlink[v] = Math.min(lowlink[v], index[w]);
		}
		if (lowlink[v] == index[v]) {
			DruidComponent newComponent = new DruidComponent();
			while (!s.empty()){
				DruidUnit wUnit = s.pop(); instack[units.indexOf(wUnit)] = false;
				newComponent.addUnit(wUnit);
				if (wUnit==units.get(v)) break;
			}
			components.add(newComponent);
		}
		return i;
	}

	private void addUnit(DruidUnit unit) {
		componentUnits.add(unit);
	}
	
	@Override
	public String toString() {
		String s = "";
		for (DruidUnit unit : componentUnits) {
			if (s.equals("")) s = unit.getName();
			else s += ", " + unit.getName();
		}
		return s;
	}
}
