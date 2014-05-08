package de.osthus.ant.task.folderexists;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;

import de.osthus.ant.task.AbstractOsthusAntTask;

public class FolderExistsTask extends AbstractOsthusAntTask implements TaskContainer {
	
	private List<Task> tasks = new ArrayList<Task>();
	
	private String folder;
	private String property;

	@Override
	public void init() throws BuildException {
		super.init();
	}
	

	@Override
	public void execute() throws BuildException {
		validatePropertiesNotNull("folder", "property");
		Project project = getProject();
		
		File f = new File(folder);
	
		if ( f.exists() ) {
			project.setProperty(property, "true");
			for ( Task task: tasks ) {
				task.perform();
			}
		}
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	@Override
	public void addTask(Task task) {
		this.tasks.add(task);
	}
}
