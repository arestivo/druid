package com.feup.contribution.druid.util;

import org.eclipse.jdt.core.IMemberValuePair;

public class StringValuePair implements IMemberValuePair{
	private String name;
	private String value;
	
	public StringValuePair(String name, String value){
		this.name = name;
		this.value = value;
	}
	
	public String getMemberName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public int getValueKind() {
		return K_STRING;
	}
}
