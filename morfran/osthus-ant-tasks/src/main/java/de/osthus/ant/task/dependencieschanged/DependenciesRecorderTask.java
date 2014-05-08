package de.osthus.ant.task.dependencieschanged;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;

import de.osthus.ant.task.AbstractOsthusAntTask;
import de.osthus.ant.task.util.TaskUtils;

public class DependenciesRecorderTask extends AbstractOsthusAntTask {
	
	private String dependencyFingerprintsFile;
	private Set<Path> paths = new HashSet<Path>();
	
	
	@Override
	public void init() throws BuildException {
		
	}
	
	@Override
	public void execute() throws BuildException {
		try {
			validatePropertiesNotNull("dependencyFingerprintsFile");
			File previousFingerprintFile = new File(dependencyFingerprintsFile);			
			Properties previousFingerprints = null;
			
			if ( previousFingerprintFile.exists() ) {
				previousFingerprints = new Properties();
				FileInputStream fis = new FileInputStream(previousFingerprintFile);
				previousFingerprints.load(fis);
				fis.close();
			}
			
			Properties fingerprints = new Properties();
			String checksum, dependencyName;
			for ( Path p: paths ) {
				for ( String s: p.list() ) {
					File f = new File(s);
					if ( f.isFile() ) {	
						dependencyName = f.getName();
						checksum = TaskUtils.generateVeryVeryComplexHash(f);					
						fingerprints.setProperty(dependencyName, checksum);
						
						if ( previousFingerprints != null ) {
							String previousDependencyName = TaskUtils.getPropertyNameByValue(checksum, previousFingerprints);
							if ( previousDependencyName != null && !previousDependencyName.equals(dependencyName) ) {
								String sinceProperty = previousFingerprints.getProperty(previousDependencyName + ".since");
								if ( sinceProperty != null ) {
									fingerprints.setProperty(dependencyName + ".since", sinceProperty);			
								} else {
									fingerprints.setProperty(dependencyName + ".since", previousDependencyName);
								}
							}
						}						
					}
				}
			}
			FileOutputStream out = new FileOutputStream(new File(dependencyFingerprintsFile));
			fingerprints.store(out, "Dependency Checksums");
			out.close();
		} catch (IOException e) {
			throw new BuildException(e);
		}
	}
	
	public void add(Path path) {
		paths.add(path);
	}

	public String getDependencyFingerprintsFile() {
		return dependencyFingerprintsFile;
	}

	public void setDependencyFingerprintsFile(String dependencyFingerprintsFile) {
		this.dependencyFingerprintsFile = dependencyFingerprintsFile;
	}
}
