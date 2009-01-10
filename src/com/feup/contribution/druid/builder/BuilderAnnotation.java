package com.feup.contribution.druid.builder;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMethod;

public class BuilderAnnotation {
	private IAnnotation annotation;
	private IMethod method;
	private String unitName;
	
	public BuilderAnnotation(IAnnotation annotation, IMethod method, String unitName) {
		this.annotation = annotation;
		this.method = method;
		this.setUnitName(unitName);
	}

	public void setMethod(IMethod method) {
		this.method = method;
	}
	
	public IMethod getMethod() {
		return method;
	}
	
	public void setAnnotation(IAnnotation annotation) {
		this.annotation = annotation;
	}
	
	public IAnnotation getAnnotation() {
		return annotation;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getUnitName() {
		return unitName;
	}	
}
