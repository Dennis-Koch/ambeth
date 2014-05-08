package de.osthus.ant.task.createclasspath.entity;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("classpath")
public class Classpath {

	@XStreamImplicit(itemFieldName="classpathentry")
	private List<ClasspathEntry> classpathEntries;
	
	public List<ClasspathEntry> getClasspathEntries() {
		if ( classpathEntries == null ) {
			classpathEntries = new ArrayList<ClasspathEntry>();
		}
		return this.classpathEntries;
	}

	public void setClasspathEntries(List<ClasspathEntry> classpathEntries) {
		this.classpathEntries = classpathEntries;
	}	
}
