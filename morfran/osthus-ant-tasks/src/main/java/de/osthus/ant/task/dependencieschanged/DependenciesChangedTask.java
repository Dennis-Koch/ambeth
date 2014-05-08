package de.osthus.ant.task.dependencieschanged;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;

import com.google.common.base.Joiner;
import com.google.common.io.Closer;

import de.osthus.ant.task.AbstractOsthusAntTask;
import de.osthus.ant.task.util.TaskUtils;

public class DependenciesChangedTask extends AbstractOsthusAntTask {

	private String property;
	private String dependencyFingerprintsFile;
	private Set<Path> paths = new HashSet<Path>();
	
	
	@Override
	public void init() throws BuildException {
		
	}
	
	public void add(Path path) {
		paths.add(path);
	}
	
	@Override
	public void execute() throws BuildException {
		validatePropertiesNotNull("property", "paths", "dependencyFingerprintsFile");
		Closer closer = Closer.create();
		
		try {
			Properties oldFingerprints = new Properties();
			
			File checksumPropertyFile = new File(dependencyFingerprintsFile);
			
			if ( !checksumPropertyFile.exists() ) {
				//getProject().setProperty(property, "no dependency fingerprints found!");
				return;
			}
				
			
			FileInputStream fis = closer.register(new FileInputStream(checksumPropertyFile));
			oldFingerprints.load(fis);
			
			SortedSet<String> changedDependencies = new TreeSet<String>();
			String checksum;
			String dependencyName;
			String oldDependencyName;
			for ( Path p: paths ) {
				for ( String s: p.list() ) {
					File f = new File(s);
					if ( f.isFile() ) {
						checksum = TaskUtils.generateVeryVeryComplexHash(f);//Files.hash(f, Hashing.sha256()).toString();
						oldDependencyName = TaskUtils.getPropertyNameByValue(checksum, oldFingerprints);						
						dependencyName = f.getName();
						
//						System.out.println("NOW: " + dependencyName + "=" + checksum);
//						System.out.println("WAS: " + oldDependencyName);
//						System.out.println();
						
						
						if ( oldDependencyName == null ) { //changed
							changedDependencies.add(dependencyName);
						}
						
//						//currentFingerprints.setProperty(dependency, currentChecksum);					
//						oldChecksum = oldFingerprints.getProperty(dependencyName);
//						
//						if ( oldChecksum == null || !oldChecksum.equals(checksum) ) {
//							changedDependencies.add(dependencyName);
//						}
					}
				}
			}
			
			if ( changedDependencies.size() > 0 ) {
				getProject().setProperty(property, Joiner.on(", ").join(changedDependencies));
				//getProject().setProperty(property, StringUtils.join(changedDependencies, ", "));
			}			
		} catch (IOException e) {
			throw new BuildException(e);
		} finally {
			try {
				closer.close();
			} catch (IOException e) {
				throw new BuildException(e);
			}
		}
	}
	
	


	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getDependencyFingerprintsFile() {
		return dependencyFingerprintsFile;
	}

	public void setDependencyFingerprintsFile(String dependencyFingerprintsFile) {
		this.dependencyFingerprintsFile = dependencyFingerprintsFile;
	}

}
