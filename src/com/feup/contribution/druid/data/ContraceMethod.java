package com.feup.contribution.druid.data;

import org.eclipse.jdt.core.IMethod;

public class ContraceMethod {
	private IMethod iMethod;
	private String methodName;
	private ContraceFeature feature;

	public ContraceMethod(IMethod iMethod, String methodName, ContraceFeature feature){
		setMethod(iMethod);
		setMethodName(methodName);
		setFeature(feature);
	}
	
	private void setFeature(ContraceFeature feature) {
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

	public ContraceFeature getFeature() {		
		return feature;
	}
}
