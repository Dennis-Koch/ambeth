package de.osthus.ant.task.targetexists;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;

import de.osthus.ant.task.AbstractOsthusAntTask;

public class TargetExistsTask extends AbstractOsthusAntTask implements TaskContainer {

	private List<Task> tasks = new ArrayList<Task>();
	private String target;
	private String property;
	

	
	@Override
	public void execute() throws BuildException {
		validatePropertiesNotNull("target", "property");
		Project project = getProject();
		
		Target t = project.getTargets().get(target);
		
		if ( t != null ) {
			project.setProperty(property, "true");
			for ( Task task: tasks ) {
				task.perform();
			}
		}
	}
	
	@Override
	public void addTask(Task task) {
		tasks.add(task);
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void setProperty(String property) {
		this.property = property;
	}


}
