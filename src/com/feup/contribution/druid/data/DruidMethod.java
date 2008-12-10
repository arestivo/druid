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

import org.eclipse.jdt.core.IMethod;

public class DruidMethod {
	private IMethod iMethod;
	private String methodName;
	private DruidFeature feature;

	public DruidMethod(IMethod iMethod, String methodName, DruidFeature feature){
		setMethod(iMethod);
		setMethodName(methodName);
		setFeature(feature);
	}
	
	private void setFeature(DruidFeature feature) {
		this.feature = feature;
	}

	private void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public void setMethod(IMethod iMethod) {
		this.iMethod = iMethod;
	}
	
	public String getMethodName(){
		return methodName;
	}

	public IMethod getMethod() {
		return iMethod;
	}

	public DruidFeature getFeature() {		
		return feature;
	}
}
