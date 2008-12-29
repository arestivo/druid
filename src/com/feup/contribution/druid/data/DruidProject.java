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
import java.util.Collection;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;

import com.feup.contribution.druid.DruidPlugin;
import com.feup.contribution.druid.listeners.ProjectListener;

public class DruidProject{
	private ArrayList<DruidUnit> units;
	private ArrayList<ProjectListener> listeners;
	
	public DruidProject(){
		units = new ArrayList<DruidUnit>();
		listeners = new ArrayList<ProjectListener>();
	}
		
	public Collection<DruidUnit> getUnits(){
		return units;
	}

	public void removeClass(String unitName, IJavaElement javaElement) {
		ArrayList<DruidUnit> toRemove = new ArrayList<DruidUnit>();
		for (DruidUnit unit : units) {
			if (unit.getName().equals(unitName)) {
				unit.removeClass(javaElement);
				if (unit.isEmpty()) toRemove.add(unit);
			}
		}
		for (DruidUnit unit : toRemove) units.remove(unit); 
	}
	
	private DruidUnit getUnit(String unitName) {
		for (DruidUnit unit : units) {
			if (unit.getName().equals(unitName)) return unit;
		}
		return null;
	}

	public void builderDone(){
		for (ProjectListener listener : listeners) {
			listener.projectChanged(this);
		}
	}
	
	public void addProjectListener(ProjectListener listener) {
		listeners.add(listener);
	}

	public void addFeature(String unitName, IMethod iMethod, String methodName, String featureName) {
		DruidUnit unit = getUnit(unitName);
		if (unit == null) {
			unit = new DruidUnit(unitName, this);
			units.add(unit);
		}
		unit.addFeature(iMethod, methodName, featureName);
	}

	public void removeAllFeatures() {
		units.clear();
	}

	public boolean addDepends(String unitName, String featureName, String dUnitName, String dFeatureName) {
		DruidUnit unit = getUnit(unitName);
		if (unit == null) {
			unit = new DruidUnit(unitName, this);
			units.add(unit);
		}
		DruidFeature dFeature = getFeature(dUnitName, dFeatureName);
		if (dFeature != null) unit.addDepends(featureName, dFeature);
		else return false;
		return true;
	}

	private DruidFeature getFeature(String unitName, String featureName) {
		DruidUnit unit = getUnit(unitName);
		if (unit == null) return null;
		return unit.getFeature(featureName);
	}

	public boolean addTest(String unitName, IMethod method, String tUnitName, String tFeatureName) {
		DruidFeature tFeature = getFeature(tUnitName, tFeatureName);
		if (tFeature != null) tFeature.addTests(method);
		else return false;
		return true;
	}

	public void detectInteractions() {
		ArrayList<DruidComponent> components = DruidComponent.getOrderedComponents(units);
		for (DruidComponent druidComponent : components) {
			DruidPlugin.getPlugin().log(druidComponent.toString());
		}
	}

}
