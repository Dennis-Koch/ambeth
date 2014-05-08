package de.osthus.ant.task.createclasspath.util;

import java.util.List;

import de.osthus.ant.task.createclasspath.type.MatchedPathType;

public class ClasspathContainer {
	
	private List<MatchedPathType> matchedPaths;
	
	public ClasspathContainer(List<MatchedPathType> matchedPaths) {
		this.matchedPaths = matchedPaths;
	}
		
	public MatchedPathType getPathById(String pathName) {
		for ( MatchedPathType mp: matchedPaths ) {
			if ( mp.getName().equals(pathName) ) {
				return mp;
			}
		}
		return new MatchedPathType();
	}
	
//	public void join(ClasspathContainer otherCpc) {		
//		for ( MatchedPathType mp: matchedPaths ) {
//			mp.getPathContent().addAll(otherCpc.getPathById(mp.getPathId()).getPathContent());
//		}
//	}
	
	public List<MatchedPathType> getMatchedPaths() {
		return matchedPaths;
	}
	
	public void setMatchedPaths(List<MatchedPathType> matchedPaths) {
		this.matchedPaths = matchedPaths;
	}

	public void addToMatchingPath(String path) {
		Boolean shallInclude;
		for ( MatchedPathType mp: matchedPaths ) {
			shallInclude = mp.shallInclude(path);			
			if ( shallInclude != null ) {
				if ( shallInclude ) {
					mp.addPathElement(path);
					//break;
				}
			}
		}
	}
}
