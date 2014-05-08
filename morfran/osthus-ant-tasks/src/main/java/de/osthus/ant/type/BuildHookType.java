package de.osthus.ant.type;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.types.DataType;

public class BuildHookType extends DataType implements TaskContainer {
	private List<Task> tasks = new ArrayList<Task>();
	
	public void execute(Target owningTarget) {		
		for ( Task t: tasks ) {
			t.setProject(getProject());
			t.setOwningTarget(owningTarget);
			t.perform();
		}
	}

	@Override
	public void addTask(Task task) {
		tasks.add(task);
	}

}
