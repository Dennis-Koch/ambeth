package de.osthus.ant.task.readdotproject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.google.common.io.Closer;

import de.osthus.ant.task.readdotproject.entity.Project;

/**
 * reads a .project file and stores the project name to an ant property
 * 
 * @author daniel.mueller
 *
 */
public class ReadDotProjectTask extends Task {

	private String projectFile;
	
	private String nameProperty;
	
	private DotProjectReader dotProjectReader;
	

	@Override
	public void init() throws BuildException {
		super.init();
		this.dotProjectReader = new DotProjectReader();
	}
	
	@Override
	public void execute() throws BuildException {
		
		if ( projectFile == null ) {
			throw new BuildException("mandatory parameter is missing: projectFile");
		}
		
		if ( nameProperty == null ) {
			throw new BuildException("mandatory parameter is missing: nameProperty");
		}
		
		Closer closer = Closer.create();
		try {			
			FileInputStream fis = closer.register(new FileInputStream(projectFile));
			Project p = dotProjectReader.readDotProject(fis);
			getProject().setProperty(nameProperty, p.getName());
		} catch (FileNotFoundException e) {
			throw new BuildException(".project file could not be found: " + projectFile, e);
		} finally {
			//IOUtils.closeQuietly(fis);
			try {
				closer.close();
			} catch (IOException e) {
				throw new BuildException(e);
			}
		}
	}
	
	
	public String getNameProperty() {
		return nameProperty;
	}
	
	public void setNameProperty(String nameProperty) {
		this.nameProperty = nameProperty;
	}

	public String getProjectFile() {
		return projectFile;
	}

	public void setProjectFile(String projectFile) {
		this.projectFile = projectFile;
	}
}
