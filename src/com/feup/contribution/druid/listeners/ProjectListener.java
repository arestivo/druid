package com.feup.contribution.druid.listeners;

import com.feup.contribution.druid.data.DruidProject;

public interface ProjectListener {

	public void projectChanged(DruidProject project);
	
}
