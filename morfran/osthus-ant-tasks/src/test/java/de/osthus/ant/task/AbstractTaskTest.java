package de.osthus.ant.task;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;

public abstract class AbstractTaskTest {

	protected Project createProject() {
		Project project = new Project();
		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		project.addBuildListener(consoleLogger);
		return project;
	}
	
}
