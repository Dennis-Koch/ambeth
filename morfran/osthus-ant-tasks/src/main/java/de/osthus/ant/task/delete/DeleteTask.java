package de.osthus.ant.task.delete;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.tools.ant.BuildException;

import de.osthus.ant.task.AbstractOsthusAntTask;
import de.osthus.ant.task.util.TaskUtils;

public class DeleteTask extends AbstractOsthusAntTask {

	private boolean verbose = false;
	private Integer retries = 10;
	private String file;
	
	@Override
	public void init() throws BuildException {
		super.init();
	}
	
	@Override
	public void execute() throws BuildException {
		validatePropertiesNotNull("file");
		File f = new File(file);		
//		validateTrue(f.isDirectory(), "Not a directory: " + dir);
		
		if ( f.exists() ) {
			try {
				log("Deleting: " + file);
				int filesDeleted = delete(f, retries);
				log(filesDeleted + " files deleted!");
			} catch (IOException e) {
				throw new BuildException("Unable to delete file/directory: " + file, e);
			}
		} else {
			log(file + " already deleted!");
		}
	}
	
	public int delete(File f, int retries) throws IOException {
		if ( !f.exists() ) { 
			return 0;
		}
		
		int r = retries;
		int filesDeleted = 0;
		while (r > 0) {			
			if (f.isDirectory()) {
				for (File c : f.listFiles())
					filesDeleted += delete(c, retries);
			}
			
			if (!f.delete()) {
				r--;
				if ( r > 0 ) {
					log("File was not deleted, will retry!");
					TaskUtils.sleep(100);
				} else {
					throw new IOException("File could not be deleted! No retries left");
				}
			} else {			
				if ( verbose )
					log("Deleted: " + f);
				return ++filesDeleted;
			}
		}
		throw new IOException("Failed to delete file: " + f);
	}

	public void setFile(String file) {
		this.file = file;
	}


	public void setRetries(Integer retries) {
		this.retries = retries;
	}


	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
}
