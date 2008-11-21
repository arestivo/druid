package com.feup.contribution.druid.data;

public class ContraceDependency {
	private ContraceFeature dependent; 
	private ContraceFeature dependee;
	
	public ContraceDependency(ContraceFeature dependee, ContraceFeature dependent) {
		this.dependee = dependee;
		this.dependent = dependent;
	}

	public ContraceFeature getDependent() {
		return dependent;
	}

	public ContraceFeature getDependee() {
		return dependee;
	}
}
