package de.osthus.ant.task.makedir;

import java.io.File;

import org.apache.tools.ant.BuildException;

import de.osthus.ant.task.AbstractOsthusAntTask;
import de.osthus.ant.task.util.TaskUtils;

public class MakeDirTask extends AbstractOsthusAntTask {
	private String dir;
	private int retries = 5;

	public void setDir(String dir) {
		this.dir = dir;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}
	
	@Override
	public void init() throws BuildException {

	}

	@Override
	public void execute() throws BuildException {
		validatePropertiesNotNull("dir");

		try {
			File f = new File(dir);
			boolean created = false;
			if (!f.exists()) {
				for ( int n = 1; n <= retries; n++ ) {
					created = f.mkdirs();
					if (created) {
						log("Directory created: " + dir);
						return;
					} else {						
						if ( n < retries ) {
							log("Failed, retrying in 500ms! [" + n + "/" + retries + "]");
							TaskUtils.sleep(500);
						}
					}
				}
				log("Directory creation failed for an unknown reason! Directory: " + dir);
			} else {
				log("Directory already exists!");
			}
		} catch (Exception e) {
			throw new BuildException(e);
		}
	}
}
