package com.feup.contribution.druid.data;

public class DruidDependency {
	private DruidFeature dependent; 
	private DruidFeature dependee;
	
	public DruidDependency(DruidFeature dependee, DruidFeature dependent) {
		this.dependee = dependee;
		this.dependent = dependent;
	}

	public DruidFeature getDependent() {
		return dependent;
	}

	public DruidFeature getDependee() {
		return dependee;
	}
}
