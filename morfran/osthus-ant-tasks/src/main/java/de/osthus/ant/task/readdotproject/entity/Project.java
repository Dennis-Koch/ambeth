package de.osthus.ant.task.readdotproject.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("projectDescription")
public class Project {
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	

}
