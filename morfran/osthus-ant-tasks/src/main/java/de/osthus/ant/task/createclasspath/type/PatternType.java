package de.osthus.ant.task.createclasspath.type;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;

public class PatternType extends DataType {
	private String include;
	private String exclude;
	
	public PatternType() {
		super();
	}
	
	public PatternType(String include, String exclude, Project project) {
		super();
		this.setInclude(include);
		this.setExclude(exclude);
		this.setProject(project);
	}

	public void setInclude(String include) {
		this.include = include;
	}
	
	public void setExclude(String exclude) {
		this.exclude = exclude;
	}
	
	public Boolean shallBeIncluded(String text) {
		if ( exclude != null && text.matches(exclude) ) {
			return false;
		} else if ( include != null && text.matches(include) ) {
			return true;
		} else {
			return null;
		}
	}
}
