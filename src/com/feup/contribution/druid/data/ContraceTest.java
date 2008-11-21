package com.feup.contribution.druid.data;

import org.eclipse.jdt.core.IMethod;

public class ContraceTest {
	private IMethod method;
	private ContraceFeature feature;
	
	public ContraceTest(IMethod method, ContraceFeature feature) {
		this.method = method;
		this.feature = feature;
	}
	
	public IMethod getMethod(){
		return method;
	}
	
	public ContraceFeature getFeature() {
		return feature;
	}
}
