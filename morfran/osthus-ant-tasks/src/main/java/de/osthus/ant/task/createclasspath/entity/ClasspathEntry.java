package de.osthus.ant.task.createclasspath.entity;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/*
	<classpathentry kind="src" path="src/main/java"/>
	<classpathentry kind="src" path="src/main/resources"/>
	<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
	<classpathentry combineaccessrules="false" exported="true" kind="src" path="/Allotrope.AnIML.Technique.API"/>
	<classpathentry combineaccessrules="false" kind="src" path="/Allotrope.AnIML.Technique.Impl"/>
	<classpathentry kind="output" path="target/classes"/>
 */

//@Data
//@XStreamAlias("classpathentry")
public class ClasspathEntry {
	
	public enum Kind{src,output,lib,con}
	
	@XStreamAsAttribute	private Kind kind;
	@XStreamAsAttribute private String path;
	@XStreamAsAttribute private String sourcepath;	
	@XStreamAsAttribute private Boolean combineaccessrules;
	@XStreamAsAttribute	private boolean exported;	
	
	public Kind getKind() {
		return kind;
	}

	public void setKind(Kind kind) {
		this.kind = kind;
	}

	public String getPath() {		
		return path;
	}

	public void setPath(String path) {		
		this.path = path;
	}

	public String getSourcepath() {
		return sourcepath;
	}

	public void setSourcepath(String sourcepath) {
		this.sourcepath = sourcepath;
	}

	public Boolean isCombineaccessrules() {		
		return combineaccessrules;
	}

	public void setCombineaccessrules(boolean combineaccessrules) {
		this.combineaccessrules = combineaccessrules;
	}

	public boolean isExported() {
		return exported;
	}

	public void setExported(boolean exported) {
		this.exported = exported;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	private List<Attribute> attributes;
	
	public List<Attribute> getAttributes() {
		if ( attributes == null ) {
			attributes = new ArrayList<Attribute>();
		}
		return this.attributes;
	}

	public void standardize() {
		this.path = path != null ? path.replace("\\\\", "/") : null;
		this.sourcepath = sourcepath != null ? sourcepath.replace("\\\\", "/") : null;
	}
}
