package de.osthus.ant.task.createclasspath.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ProjectArtifacts {

	private static final Pattern PATTERN_EMMA    = Pattern.compile("(?i).*-emma\\.jar");
	private static final Pattern PATTERN_TEST    = Pattern.compile("(?i).*-test\\.jar");
	private static final Pattern PATTERN_SOURCE  = Pattern.compile("(?i).*-sources\\.jar");
	private static final Pattern PATTERN_JAVADOC = Pattern.compile("(?i).*-javadoc\\.jar");
	
	enum Classifier {
		MAIN,
		TEST,
		EMMA,
		SOURCE,
		JAVADOC;
		
		public static Classifier parseClassifier(File f) {
			String jarName = f.getName();			
			if ( PATTERN_EMMA.matcher(jarName).matches() ) {
				return EMMA;
			} else if ( PATTERN_TEST.matcher(jarName).matches() ) {
				return TEST;				
			} else if ( PATTERN_SOURCE.matcher(jarName).matches() ) {
				return SOURCE;
			} else if ( PATTERN_JAVADOC.matcher(jarName).matches() ) {
				return JAVADOC;
			} else {
				return MAIN;
			}
		}
	}
	
	private Map<Classifier, File> classifiedArtifacts;
	
	public ProjectArtifacts() {
		init();
	}
	
	private void init() {
		classifiedArtifacts = new HashMap<Classifier, File>();		
	}

	public void addArtifact(File f) {
		Classifier classifier = Classifier.parseClassifier(f);
		classifiedArtifacts.put(classifier, f);
	}
	
	
	public void addArtifacts(Collection<File> files) {
		for ( File f: files ) {
			addArtifact(f);
		}
	}
	
	public List<File> sortByClassifiers() {
		
		List<File> sorted = new ArrayList<File>() {
			private static final long serialVersionUID = 1L;
			public boolean add(File e) {
				if ( e != null ) {
					return super.add(e);
				} else {
					return false;
				}
			}
		};
		
		sorted.add(classifiedArtifacts.get(Classifier.TEST));
		sorted.add(classifiedArtifacts.get(Classifier.EMMA));
		sorted.add(classifiedArtifacts.get(Classifier.MAIN));
		//ignore javadoc and source artifacts
		
		return sorted;
	}

	public static List<File> classifierSort(Collection<File> jarFiles) {		
		ProjectArtifacts pa = new ProjectArtifacts();
		pa.addArtifacts(jarFiles);
		return pa.sortByClassifiers();
	}
}
