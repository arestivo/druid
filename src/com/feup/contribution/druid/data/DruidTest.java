package com.feup.contribution.druid.data;

import org.eclipse.jdt.core.IMethod;

public class DruidTest {
	private IMethod method;
	private DruidFeature feature;
	
	public DruidTest(IMethod method, DruidFeature feature) {
		this.method = method;
		this.feature = feature;
	}
	
	public IMethod getMethod(){
		return method;
	}
	
	public DruidFeature getFeature() {
		return feature;
	}
}
