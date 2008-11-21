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
