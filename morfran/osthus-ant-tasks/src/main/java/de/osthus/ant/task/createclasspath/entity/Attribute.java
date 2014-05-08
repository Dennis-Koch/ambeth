package de.osthus.ant.task.createclasspath.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;

//@Data
@XStreamAlias("attribute")
public class Attribute {	
	//@XStreamAsAttribute 
	private String name;
	
	//@XStreamAsAttribute 
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}
