package de.osthus.ant.task.readdotclasspath;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.junit.Ignore;
import org.junit.Test;

import de.osthus.ant.task.createclasspath.CreateClasspathTask;

public class TestReadDotClasspathTask {

	@Test @Ignore
	public void taskTest() throws FileNotFoundException, IOException {

		Project project = new Project();
		project.setProperty("basedir", "C:/dev/allotrope/ambeth/jAmbeth.Cache.File");
		

		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		project.addBuildListener(consoleLogger);

		Properties properties = new Properties();
		properties.load(new FileInputStream("C:/dev/allotrope/framework/Allotrope.Lib/ant/build.properties"));

		for (Object propertyName : properties.keySet()) {
			project.setProperty((String) propertyName, properties.getProperty((String) propertyName));
		}
		project.setProperty("root.dir", "C:/dev/allotrope");

		PropertyHelper propertyHelper = PropertyHelper
				.getPropertyHelper(project);
		String expandedValue;
		for (String property : project.getProperties().keySet()) {
			expandedValue = propertyHelper.replaceProperties(project.getProperty(property));
			project.setProperty(property, expandedValue);
		}

		project.init();

		CreateClasspathTask task = new CreateClasspathTask();
		task.setVerbose(true);
		//task.isUnderTest = true;
		task.setProject(project);
		task.setProjectDir("C:/dev/allotrope/ambeth/jAmbeth.Cache.File");
		task.setClassesDir("foo");
		task.init();
		task.execute();
	}
	
	public void taskTest2() throws FileNotFoundException, IOException {

		Project project = new Project();
		project.setProperty("basedir", "C:/dev/allotrope/ambeth/jAmbeth.Cache.File");
		

		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		project.addBuildListener(consoleLogger);

		Properties properties = new Properties();
		properties.load(new FileInputStream("C:/dev/allotrope/framework/Allotrope.Lib/ant/build.properties"));

		for (Object propertyName : properties.keySet()) {
			project.setProperty((String) propertyName, properties.getProperty((String) propertyName));
		}
		project.setProperty("root.dir", "C:/dev/allotrope");

		PropertyHelper propertyHelper = PropertyHelper
				.getPropertyHelper(project);
		String expandedValue;
		for (String property : project.getProperties().keySet()) {
			expandedValue = propertyHelper.replaceProperties(project.getProperty(property));
			project.setProperty(property, expandedValue);
		}

		project.init();

		CreateClasspathTask task = new CreateClasspathTask();
		task.setVerbose(true);
		//task.isUnderTest = true;
		task.setProject(project);
		task.setProjectDir("C:/dev/allotrope/ambeth/jAmbeth.Cache.File");
		task.setClassesDir("foo");
		task.init();
		task.execute();
	}	
	
	
	@Test @Ignore
	public void taskTest3() throws FileNotFoundException, IOException {
		System.out.println("==================== Running Test 3 ====================");
		
		Project project = new Project();
		project.setProperty("basedir", "C:/dev/allotrope/ambeth/jAmbeth.Cache.File");
		

		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		project.addBuildListener(consoleLogger);

		Properties properties = new Properties();
		properties.load(new FileInputStream("C:/dev/allotrope/framework/Allotrope.Lib/ant/build.properties"));

		for (Object propertyName : properties.keySet()) {
			project.setProperty((String) propertyName, properties.getProperty((String) propertyName));
		}
		project.setProperty("root.dir", "C:/dev/allotrope");

		PropertyHelper propertyHelper = PropertyHelper
				.getPropertyHelper(project);
		String expandedValue;
		for (String property : project.getProperties().keySet()) {
			expandedValue = propertyHelper.replaceProperties(project.getProperty(property));
			project.setProperty(property, expandedValue);
		}

		project.init();

		CreateClasspathTask task = new CreateClasspathTask();
		task.setVerbose(true);
		//task.isUnderTest = true;
		task.setProject(project);
		//task.setProjectDir("C:/dev/allotrope/animl/Allotrope.AnIML.Chromatography.API");
		task.setProjectDir("C:/dev/allotrope/ambeth/jAmbeth.Cache.File");
		task.setClassesDir("foo");
		task.init();
		task.execute();
	}		
}
