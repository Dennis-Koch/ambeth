package de.osthus.ant.task.util;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

public class TestEclipseTools {

	@Test @Ignore
	public void getProjectLocationTest_WithLocation_1() {
		File workspaceLocation = new File("C:/dev/allotrope/workspace");		
		String projectName = "Allotrope.AnIML.Chromatography.API";		
		String projectLocation = EclipseTools.getProjectLocation(workspaceLocation, projectName);		
		System.out.println("1) " + projectName + ": " + projectLocation);
		System.out.println();
	}
	
	@Test @Ignore
	public void getProjectLocationTest_WithLocation_2() {
		File workspaceLocation = new File("C:/dev/allotrope/workspace");		
		String projectName = "Allotrope.AnIML.Core.Impl";		
		String projectLocation = EclipseTools.getProjectLocation(workspaceLocation, projectName);		
		System.out.println("1) " + projectName + ": " + projectLocation);
		System.out.println();
	}	
	
	@Test @Ignore
	public void getProjectLocationTest_InWorkspaceLocation() {
		File workspaceLocation = new File("C:/dev/osthus");		
		String projectName = "osthus-ant-tasks-test";		
		String projectLocation = EclipseTools.getProjectLocation(workspaceLocation, projectName);		
		System.out.println("2) " + projectName + ": " + projectLocation);
		System.out.println();
	}
	
	@Test @Ignore
	public void getProjectLocationTest_NoLocation() {
		File workspaceLocation = new File("C:/dev/osthus");		
		String projectName = "osthus-ant-tasks-test2";		
		String projectLocation = EclipseTools.getProjectLocation(workspaceLocation, projectName);		
		System.out.println("3) " + projectName + ": " + projectLocation);
		System.out.println();
	}
	
	@Test @Ignore
	public void listWorkspaceProjectsTest() {
		EclipseTools.listWorkspaceProjects(new File("C:/dev/osthus"));
		EclipseTools.listWorkspaceProjects(new File("C:/dev/allotrope/workspace"));
	}
}
