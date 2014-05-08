package de.osthus.ant.task.createclasspath.type;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Path;

public class MatchedPathType extends DataType {
	protected String name;
	protected List<PatternType> patterns = new ArrayList<PatternType>();
	
	protected Set<String> pathContent = new LinkedHashSet<String>();
	protected List<Path> additionalPaths = new ArrayList<Path>();
	
	
	public Set<String> getPathContent() {
		return pathContent;
	}
		
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<PatternType> getPatterns() {
		return patterns;
	}
	public void setPatterns(List<PatternType> patterns) {
		this.patterns = patterns;
	}
	
	public void addPattern(PatternType pattern) {
		patterns.add(pattern);
	}

	public Boolean shallInclude(String path) {
		Boolean shallBeIncluded;
		for ( PatternType pattern: patterns ) {
			shallBeIncluded = pattern.shallBeIncluded(path); 
			if ( shallBeIncluded != null ) {
				return shallBeIncluded;
			}
		}
		return null;
	}

	public void addPathElement(String path) {
		this.pathContent.add(path);
	}
		
	public void add(Path path) {
		this.additionalPaths.add(path);
	}
	
	public List<Path> getAdditionalPaths() {
		return additionalPaths;
	}
}