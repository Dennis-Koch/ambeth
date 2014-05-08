package de.osthus.ant.task.sourceschanged;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

import de.osthus.ant.task.AbstractOsthusAntTask;
import de.osthus.ant.task.util.TaskUtils;

public class SourcesChangedTask extends AbstractOsthusAntTask {

//	private String buildFile;
	private String property;
//	private Set<FileSet> sourceDirs = new HashSet<FileSet>();
//	private Set<FileSet> classDirs = new HashSet<FileSet>();
	
	private String sourceDirs;
	private String classDirs;
	
	
	
	@Override
	public void init() throws BuildException {
		
	}
	
	@Override
	public void execute() throws BuildException {
		validatePropertiesNotNull("property", "sourceDirs", "classDirs");
		Project project = getProject();
		
		Collection<FileSet> fsSources = convertStringArrayToFileSetCollection(sourceDirs);
		Collection<FileSet> fsClasses = convertStringArrayToFileSetCollection(classDirs);
		
		Pattern p = Pattern.compile("^(.(?!\\$))*$");
		
		Map<String, Long> sourceFiles = TaskUtils.listFiles(fsSources, p);
		Map<String, Long> classFiles = TaskUtils.listFiles(fsClasses, p);
		
		
		
		if ( sourceFiles.size() != classFiles.size() ) {

			Set<String> newSourceFiles = new HashSet<String>();
			Set<String> deletedSourceFiles = new HashSet<String>();
			
			for ( String srcFileName: sourceFiles.keySet() ) {
				String asKey =  srcFileName;				
				if ( srcFileName.endsWith(".java") ) {
					asKey = asKey.substring(0, asKey.length()-"java".length()) + "class";
				}
				if ( !classFiles.containsKey(asKey) ) {
					newSourceFiles.add(srcFileName);
				}
			}
			
			for ( String classFileName: classFiles.keySet() ) {
				String asKey =  classFileName;				
				if ( classFileName.endsWith(".class") ) {
					asKey = asKey.substring(0, asKey.length()-"class".length()) + "java";
				}
				if ( !sourceFiles.containsKey(asKey) ) {
					deletedSourceFiles.add(classFileName);
				}
			}			
			
			
			project.setProperty(property, "inequal ammount of files! " + sourceFiles.size() + " != " + classFiles.size());
			if ( newSourceFiles.size() > 0 ) {
				System.out.println("New source files: " + newSourceFiles);
			}
			if ( deletedSourceFiles.size() > 0 ) {
				System.out.println("Deleted source files: " + deletedSourceFiles);
			}
			return;
		} else {
			for ( String srcFileName: sourceFiles.keySet() ) {
				String asKey =  srcFileName;				
				if ( srcFileName.endsWith(".java") ) {
					asKey = asKey.substring(0, asKey.length()-"java".length()) + "class";
				}
				
				Long sourceTimestamp = sourceFiles.get(srcFileName);
				Long classTimestamp = classFiles.get(asKey);
				
//				System.out.println(sourceTimestamp + " = " + classTimestamp);
				
				if ( classTimestamp == null || classTimestamp < sourceTimestamp ) {
					project.setProperty(property, "true");		
					return;
				}				
			}			
		}		
	}

	private Collection<FileSet> convertStringArrayToFileSetCollection(String concatenatedDirsString) {
		FileSet fileSet;
		Collection<FileSet> fileSets = new HashSet<FileSet>();
		for ( String dir: concatenatedDirsString.split(";") ) {
			fileSet = new FileSet();
			fileSet.setProject(getProject());
			fileSet.setDir(new File(dir));
			fileSets.add(fileSet);
		}
		return fileSets;
	}
	

	
//	public void addSources(FileSet sourceDir) {
//		this.sourceDirs.add(sourceDir);
//	}
//	
//	public void addClasses(FileSet classDir) {
//		this.classDirs.add(classDir);
//	}
	
	

	public String getProperty() {
		return property;
	}

	public String getSourceDirs() {
		return sourceDirs;
	}

	public void setSourceDirs(String sourceDirs) {
		this.sourceDirs = sourceDirs;
	}

	public String getClassDirs() {
		return classDirs;
	}

	public void setClassDirs(String classDirs) {
		this.classDirs = classDirs;
	}

	public void setProperty(String property) {
		this.property = property;
	}
}
