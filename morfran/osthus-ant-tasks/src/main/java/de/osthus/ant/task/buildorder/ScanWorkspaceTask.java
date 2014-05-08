package de.osthus.ant.task.buildorder;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import de.osthus.ant.task.AbstractOsthusAntTask;
import de.osthus.ant.task.util.EclipseTools;

/**
 * Scans the workspace for projects
 * 
 * @author daniel.mueller
 *
 */
public class ScanWorkspaceTask extends AbstractOsthusAntTask {

private String propertySuffix = ".location";
	
	private String workspace;
	
	private String basedir;

	private String property;
	
	private boolean verbose = false;


	
	@Override
	public void execute() throws BuildException {
		validatePropertiesNotNull("propertySuffix", "workspace", "basedir", "property");
		Project project = getProject();
		
		String basedir = getBasedir();
		File workspaceDirectory = new File(workspace);
		Map<String, String> projects = EclipseTools.listWorkspaceProjects(workspaceDirectory);
//		String projectLocationRel;
		String projectLocation;		
		
		//Properties projectLocations = new Properties();		
		//projectLocations.setProperty("location.suffix", propertySuffix);
		
		StringBuilder allProjectsProperty = new StringBuilder("");
				
		for ( String projectName: projects.keySet() ) {
			
			allProjectsProperty.append(projectName + ",");
			
			projectLocation = EclipseTools.getProjectLocation(workspaceDirectory, projectName);
			
			// uniquify file seperators 
			projectLocation = projectLocation.replaceAll("\\\\+", "/");
			if ( basedir != null ) {
				basedir = basedir.replaceAll("\\\\+", "/");
				projectLocation = projectLocation.replaceAll(Pattern.quote("" + basedir), "");
			} else {
				
			}
			//projectLocations.setProperty(projectName + propertySuffix, projectLocationRel);
			project.setProperty(projectName + propertySuffix, projectLocation);
			
			if ( verbose ) {
				log(projectName + propertySuffix + "=" + projectLocation);
			}
		}
		project.setProperty(property, allProjectsProperty.toString());
		//projectLocations.setProperty(property, allProjectsProperty.toString());
	}
	

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}
	
	public String getPropertySuffix() {
		return propertySuffix;
	}

	public void setPropertySuffix(String propertySuffix) {
		this.propertySuffix = propertySuffix;
	}
	
	public String getWorkspace() {
		return workspace;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}

	public String getBasedir() {
		return basedir;
	}

	public void setBasedir(String basedir) {
		this.basedir = basedir;
	}


	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
}
