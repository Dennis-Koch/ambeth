package com.koch.ambeth.persistence.xml.model;

import java.util.List;

public interface IProjectService
{
	List<Project> getAllProjects();

	Project getProjectByName(String name);

	void saveProject(Project project);

	void deleteProject(Project project);
}