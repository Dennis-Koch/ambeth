package de.osthus.ant.task.buildorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import com.google.common.io.Closer;

import de.osthus.ant.task.AbstractOsthusAntTask;
import de.osthus.ant.task.createclasspath.entity.Classpath;
import de.osthus.ant.task.createclasspath.entity.ClasspathEntry;
import de.osthus.ant.task.createclasspath.entity.ClasspathEntry.Kind;
import de.osthus.ant.task.util.DotClasspathReader;

public class ComputeBuildOrderTask extends AbstractOsthusAntTask {
	
	public static final String LIST_DELIMITER = ","; 
	private String projects;
	private String projectLocationPropertySuffix = ".location";
	
	private String propertyFile;
	

	@Override
	public void execute() throws BuildException {
		validatePropertiesNotNull("projects", "projectLocationPropertySuffix");
		Project project = getProject();		
		List<String> sortedProjects = new ArrayList<String>();
		List<String> availableProjects = Arrays.asList(projects.split(LIST_DELIMITER));
		
		Collections.sort(availableProjects, new ProjectNameComparator());
		
		System.out.println(availableProjects);
		
		
//		Map<String, Classpath> classpathsByProject = new HashMap<String, Classpath>(); 
		String cpFile;
		int lastProgress = -1;
		Closer closer = Closer.create();
		
		
		HashMap<String, Classpath> classpathCache = new HashMap<String, Classpath>();
		StringBuilder projectNamesBuilder = new StringBuilder();
		StringBuilder projectLocationsBuilder = new StringBuilder();
		
		try {
			while ( availableProjects.size() != sortedProjects.size() ) {
				if ( lastProgress == sortedProjects.size() ) {
					throw new BuildException("unable to create build order. is there a build cycle?");
				}
					
				lastProgress = sortedProjects.size();
				middleLoop:
				for ( String projectName: availableProjects ) {
					if ( sortedProjects.contains(projectName) ) {
						//already contained in build order!
						continue middleLoop;
					}
					cpFile = project.getProperty(projectName + projectLocationPropertySuffix) + "/.classpath";
					
					Classpath classpath = classpathCache.get(cpFile);
					if ( classpath == null ) {
						classpath = DotClasspathReader.readDotClasspath(closer.register(new FileInputStream(new File(cpFile))));
						classpathCache.put(cpFile, classpath);
					}
					
					for ( ClasspathEntry cpe: classpath.getClasspathEntries() ) {		
						if ( Kind.src == cpe.getKind() && availableProjects.contains(cpe.getPath().replaceAll("/", "")) ) { // this is a referenced project
							String refProjectName = cpe.getPath().substring(1);
							if ( !sortedProjects.contains(refProjectName) ) {
								continue middleLoop;
							}
						}
					}
					//System.out.println("add to build order: " + projectName + " ["+project.getProperty(projectName+projectLocationPropertySuffix)+"]");
					log(projectName + ".location="+project.getProperty(projectName+projectLocationPropertySuffix));
					sortedProjects.add(projectName);
					projectNamesBuilder.append(projectName + ",\\\r\n");
					projectLocationsBuilder.append(projectName + ".location="+project.getProperty(projectName+projectLocationPropertySuffix)+"\r\n");
					//only add one at a time to preserve domain ordering
					break;
				}				
			}
			
			if ( propertyFile != null ) {
				PrintWriter pw = new PrintWriter(new File(propertyFile));				
				pw.println("projects=\\\r\n" + projectNamesBuilder.toString());
				pw.println();
				
				pw.println(projectLocationsBuilder.toString());
				
				pw.close();				
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(e);
		} finally {
			try {
				closer.close();
			} catch (IOException e) {
				throw new BuildException(e);
			}
		}
	}

	public String getProjects() {
		return projects;
	}

	public void setProjects(String projects) {
		this.projects = projects;
	}

	public String getProjectLocationPropertySuffix() {
		return projectLocationPropertySuffix;
	}

	public void setProjectLocationPropertySuffix(
			String projectLocationPropertySuffix) {
		this.projectLocationPropertySuffix = projectLocationPropertySuffix;
	}

	public void setPropertyFile(String propertyFile) {
		this.propertyFile = propertyFile;
	}
}
