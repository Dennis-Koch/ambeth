package de.osthus.ant.task.createclasspath.type;

import org.apache.tools.ant.types.DataType;

public class Classifier extends DataType {

	private String name;
	private int level;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	
	
}
