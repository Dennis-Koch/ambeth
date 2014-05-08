package de.osthus.ant.task.hooks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;

import de.osthus.ant.task.AbstractOsthusAntTask;

public class BuildHookTask extends AbstractOsthusAntTask {
	
	private String target;


	@Override
	public void execute() throws BuildException {
		validatePropertiesNotNull("target");
		Project project = getProject();
		
		if ( getOwningTarget() == null ) {
			throw new BuildException("execute-hook must not be used at the top level");
		}
		
		if ( project.getProperty(target + ".executed") == null ) {
		
			log("Executing target: " + target);
			
//			BuildHookType hook;
			//Object hookRef = project.getReference(target);
			Target hookTarget = project.getTargets().get(target);
			
			
			if ( hookTarget != null ) {
				try {
					project.executeTarget(target);
					
//					hook = (BuildHookType) TaskUtils.copy(hookRef);
//					hook.setProject(project);
//					hook.execute(this.getOwningTarget());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}  else {
				log("No target registered!");
			}	
			
			project.setProperty(target + ".executed", "true");
		} else {
//			log("hook already executed: " + type);
		}
	}

	public void setTarget(String target) {
		this.target = target;
	}
}
