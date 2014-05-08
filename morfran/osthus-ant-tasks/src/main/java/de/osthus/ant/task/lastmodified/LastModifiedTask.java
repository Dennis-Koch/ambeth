package de.osthus.ant.task.lastmodified;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

import de.osthus.ant.task.AbstractOsthusAntTask;
import de.osthus.ant.task.util.TaskUtils;

/**
 * recursively walks through a  fileset and find the youngest file
 * 
 * @author daniel.mueller
 *
 */
public class LastModifiedTask extends AbstractOsthusAntTask {

	private String result;
	private Set<FileSet> fileSets = new HashSet<FileSet>(); 
	
	
	private static final String DATE_FORMAT = "dd/MM/yyyy";
	

	
	@Override
	public void execute() throws BuildException {
		validatePropertiesNotNull("result");
		Project project = getProject();		
		
		if ( fileSets.size() > 0 ) {		
			File youngestFile = TaskUtils.findYoungestFile(fileSets.toArray(new FileSet[0]));
			
			project.setProperty(result + ".name", youngestFile.getName());
			project.setProperty(result + ".path", youngestFile.getAbsolutePath());
			project.setProperty(result + ".date", new SimpleDateFormat(DATE_FORMAT).format(new Date(youngestFile.lastModified())));
		}
	}
	
	public String getProperty() {
		return result;
	}

	public void setProperty(String property) {
		this.result = property;
	}

	public void add(FileSet fileSet) {
		this.fileSets.add(fileSet);
	}
}
