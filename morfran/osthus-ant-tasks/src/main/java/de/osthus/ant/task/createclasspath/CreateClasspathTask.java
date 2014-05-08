package de.osthus.ant.task.createclasspath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Path.PathElement;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.TreeTraverser;
import com.google.common.io.Closer;
import com.google.common.io.Files;

import de.osthus.ant.task.createclasspath.entity.Classpath;
import de.osthus.ant.task.createclasspath.entity.ClasspathEntry;
import de.osthus.ant.task.createclasspath.type.MatchedPathType;
import de.osthus.ant.task.createclasspath.type.PatternType;
import de.osthus.ant.task.createclasspath.type.ResourceLinkType;
import de.osthus.ant.task.createclasspath.util.ClasspathContainer;
import de.osthus.ant.task.createclasspath.util.ProjectArtifacts;
import de.osthus.ant.task.util.DotClasspathReader;

/**
 * reads a .classpath file and creates a ant 'path'.
 * other tasks, e.g. javac, can reference this path by 
 * the given pathid property
 * 
 * @author daniel.mueller
 *
 */
public class CreateClasspathTask extends Task {

	private static final String TASK_NAME = "create-classpath";
	private static final String CLASSPATH_FILE = ".classpath";
	private static final String BASEDIR_PROPERTY = "basedir";
	
	private static String DEFAULT_MAIN_INCLUDE_PATTERN = ".*";
	private static String DEFAULT_EMMA_PATTERN = ".*-emma.jar";	
	private static String DEFAULT_TESTDIR_PATTERN = "(?i).*?[/\\\\]TEST[/\\\\].*?";
	private static String DEFAULT_TESTJAR_PATTERN = "(?i).*-test\\.jar";
	
	private String targetdir = "/target";
	private String deploydir = targetdir + "/deploy";	
	private String projectLocationSuffix = ".location";	
	private String classpathFile;	
	private String defaultMainPathid = "main.classpath";
	private String defaultTestPathid = "test.classpath";
//	private String defaultEmmaPathid = "emma.classpath";
	private String classesDir;
	private String projectDir;
	
	private boolean verbose = false;	
	private boolean useDefaultMainPath = true;
	private boolean useDefaultTestPath = true;
//	private boolean useDefaultEmmaPath = true;
	
	private List<ResourceLinkType> resourceLinks = new ArrayList<ResourceLinkType>();
	
	private List<MatchedPathType> matchedPaths = new ArrayList<MatchedPathType>();
	
	
	private Set<String> alreadyProcessedClasspathFilesSet;
	
	


	@Override
	public void init() throws BuildException {
		super.init();		
		setTaskName(TASK_NAME);
		autowireRequiredDataTypes();		
	}
	
	private void autowireRequiredDataTypes() {
		Map<String, Object> references = getProject().getCopyOfReferences();
		Object ref;
		ResourceLinkType rl;
		for ( String s: references.keySet() ) {			
			ref = references.get(s);			
			if ( ref.getClass().getSimpleName().equals(ResourceLinkType.class.getSimpleName()) ) {
				try {
					Field localField  = ref.getClass().getDeclaredField("local");
					Field targetField = ref.getClass().getDeclaredField("target");
					localField.setAccessible(true);
					targetField.setAccessible(true);
					//System.out.println(localField.get(ref) + " to " + targetField.get(ref));
					rl = new ResourceLinkType((String) localField.get(ref), (String) targetField.get(ref));
					if ( !resourceLinks.contains(rl) ) {
						log("Injecting ResourceLink: " + rl);
						this.addResourceLink(rl);						
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new BuildException("Error cloning ResourceLinkType", e);
				}				
			}
		}
	}
	
	/**
	 * tests if the classpaths that would be created by this task have already been defined in the project.
	 * prevents multiple executions!
	 * 
	 * @return
	 */
	private boolean isPreviouslyInitialized() {
		Project project = getProject();
		for ( MatchedPathType mp: matchedPaths ) {
			if ( project.getReference(mp.getName()) == null ) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void execute() throws BuildException {
		Project project = getProject();
		alreadyProcessedClasspathFilesSet = new HashSet<String>();
		
		if ( useDefaultMainPath ) {
			MatchedPathType defaultMainPath = new MatchedPathType();
			defaultMainPath.setProject(project);
			defaultMainPath.setName(defaultMainPathid);
			defaultMainPath.addPattern(new PatternType(null, DEFAULT_EMMA_PATTERN, getProject()));
			defaultMainPath.addPattern(new PatternType(null, DEFAULT_TESTDIR_PATTERN, getProject()));
			defaultMainPath.addPattern(new PatternType(null, DEFAULT_TESTJAR_PATTERN, getProject()));
			defaultMainPath.addPattern(new PatternType(DEFAULT_MAIN_INCLUDE_PATTERN, null, getProject()));
			
			matchedPaths.add(defaultMainPath);
		}		
		
		if ( useDefaultTestPath ) {
			if ( classesDir != null && classesDir.trim().length() > 0 ) {
				MatchedPathType defaultTestPath = new MatchedPathType();
				defaultTestPath.setProject(getProject());
				defaultTestPath.setName(defaultTestPathid);
				defaultTestPath.addPattern(new PatternType(DEFAULT_TESTDIR_PATTERN, null, getProject()));
				defaultTestPath.addPattern(new PatternType(DEFAULT_TESTJAR_PATTERN, null, getProject()));
				defaultTestPath.addPattern(new PatternType(DEFAULT_EMMA_PATTERN, null, getProject()));
				defaultTestPath.addPattern(new PatternType(DEFAULT_MAIN_INCLUDE_PATTERN, null, getProject()));
				
				Path classesDirPath = new Path(project);
				classesDirPath.setPath(classesDir);
				defaultTestPath.add(classesDirPath);
				
				matchedPaths.add(defaultTestPath);
			} else {
				throw new BuildException("please specify the classesDir attribute when the default test-path is used");
			}
		}
		
//		if ( useDefaultEmmaPath ) {
//			MatchedPathType defaultEmmaPath = new MatchedPathType();
//			defaultEmmaPath.setProject(project);
//			defaultEmmaPath.setName(defaultEmmaPathid);
////			defaultEmmaPath.addPattern(new PatternType(null, DEFAULT_MAIN_EXCLUDE_PATTERN_EMMA, getProject()));
////			defaultEmmaPath.addPattern(new PatternType(null, DEFAULT_TESTDIR_PATTERN, getProject()));
////			defaultEmmaPath.addPattern(new PatternType(null, DEFAULT_TESTJAR_PATTERN, getProject()));
////			defaultEmmaPath.addPattern(new PatternType(DEFAULT_MAIN_INCLUDE_PATTERN, null, getProject()));
//			
//			matchedPaths.add(defaultEmmaPath);
//		}
		
		
		if ( projectDir != null && projectDir.trim().length() > 0 ) {
			projectDir = projectDir.replaceAll("\\\\", "/");
			this.classpathFile = projectDir + "/" + CLASSPATH_FILE;
			this.classpathFile = this.classpathFile.replaceAll("/{2,}", "/");
		} else {
			this.classpathFile = getProject().getProperty(BASEDIR_PROPERTY) + "/" + CLASSPATH_FILE;
			this.classpathFile = this.classpathFile.replaceAll("\\\\", "/").replaceAll("/{2,}", "/");
		}
		
				
		if ( !isPreviouslyInitialized() ) { 
			computeReferencedJars(new ClasspathContainer(matchedPaths));
			
			for ( MatchedPathType mp: matchedPaths ) {
				createAntPath(mp.getPathContent(), mp.getName(), mp.getAdditionalPaths());	
			}
		} else {
			log("Classpath already calculated!");
		}
	}
	
	
	private void createAntPath(Set<String> pathEntries, String pathId, Collection<Path> additionalPaths) {
		Project project = getProject();
		Path path = (Path) project.createDataType("path");
		project.addReference(pathId, path); 
		
		for ( String jar: pathEntries ) {
			PathElement pe = path.createPathElement();
			pe.setLocation(new File(jar));
			if ( verbose ) {
				log("["+pathId+"] Using: " + jar);
			}			
		}
		for ( Path p: additionalPaths ) {
			path.append(p);
			if ( verbose ) {
				log("["+pathId+"] Using additional: " + p);
			}
		}
	}
	
	private ClasspathContainer computeReferencedJars(ClasspathContainer cpc) {
		Closer closer = Closer.create();
		try {
			FileInputStream fis = closer.register(new FileInputStream(classpathFile));
			
			//DotClasspathReader dcpr = this.getDotClasspathReader();
			Classpath cp = DotClasspathReader.readDotClasspath(fis);	
			
			for ( ClasspathEntry cpe: cp.getClasspathEntries() ) {				
				this.processClasspathEntry(cpe, cpc);
			}
		} catch (FileNotFoundException e) {			
			throw new BuildException(e);
		} finally {
			try {
				closer.close();
			} catch (IOException e) {
				throw new BuildException(e);
			}
		}
		
		return cpc;
	}
	
	private void processClasspathEntry(ClasspathEntry cpe, ClasspathContainer cpc) {
		switch (cpe.getKind()) {
			case src:			
				processSourceClasspathEntry(cpe, cpc);
				break;
				
			case output:			
				processOutputClasspathEntry(cpe);
				break;
				
			case con:			
				processContainerClasspathEntry(cpe);
				break;
				
			case lib:			
				processLibraryClasspathEntry(cpe, cpc);
				break;				
	
			default:
				log("Unsupported classpath entry kind: " + cpe.getKind());
				break;
		}
	}
	
	
	/**
	 * Example:
	 * <classpathentry kind="src" path="src/main/java"/>
	 * <classpathentry kind="src" path="src/main/resources"/>
	 * <classpathentry kind="src" path="src/test/java"/>
	 * <classpathentry kind="src" path="src/test/resources"/>		 
	 * <classpathentry combineaccessrules="false" kind="src" path="/osthus-ant-tasks-test2"/>
	 */
	private void processSourceClasspathEntry(ClasspathEntry cpe, ClasspathContainer cpc) {
		Project project = getProject();
		String[] pathSplit = cpe.getPath().split("[/\\\\]");		
		String possibleProject = project.getProperty(pathSplit[pathSplit.length-1] + projectLocationSuffix);
		//if ( cpe.isCombineaccessrules() != null ) { //then: this is a referenced project
		if ( possibleProject != null ) {
			processReferencedProject(cpe, cpc);
		}
	}
	
	private void processReferencedProject(ClasspathEntry cpe, ClasspathContainer cpc) {
		Project project = getProject();		
		String cpePath = cpe.getPath();
		
		//1. include the projects referenced libs
		
		//remove the leading slash
		String refProjectName = cpePath.substring(1);
		String refProjectBaseDir = project.getProperty(refProjectName + projectLocationSuffix).replaceAll("\\\\", "/");
		
		String cpFile = refProjectBaseDir + "/" + CLASSPATH_FILE;
		if ( !alreadyProcessedClasspathFilesSet.add(cpFile) ) {
			return;
		}
		
		CreateClasspathTask recursiveTask = new CreateClasspathTask();
		recursiveTask.setProject(project);
		recursiveTask.setProjectDir(refProjectBaseDir);		
		recursiveTask.setClasspathFile(cpFile);
		recursiveTask.resourceLinks = resourceLinks;
		recursiveTask.setAlreadyProcessedClasspathFilesSet(alreadyProcessedClasspathFilesSet);
		recursiveTask.init();
		recursiveTask.computeReferencedJars(cpc);
		
		//2. add the projects build artifact(s) itself to the path 
		String refProjectDeployDir = refProjectBaseDir + deploydir;		
		//take all jars from deploy folder
		File refProjectDeployDirFile = new File(refProjectDeployDir);
		if ( refProjectDeployDirFile.exists() ) {
			
			TreeTraverser<File> fileTreeTraverser = Files.fileTreeTraverser();
			FluentIterable<File> i = fileTreeTraverser.breadthFirstTraversal(new File(refProjectDeployDir));
			
			Collection<File> jarFiles = i.filter(new Predicate<File>() {
				@Override
				public boolean apply(File input) {					
					return "jar".equals(Files.getFileExtension(input.getName()).toLowerCase());
				}
			}).toList();
			
			jarFiles = ProjectArtifacts.classifierSort(jarFiles);
			
			for ( File f: jarFiles ) {
				
				cpc.addToMatchingPath(f.getAbsolutePath().replaceAll("\\\\", "/"));
			}
		}
	}
	
	private void processOutputClasspathEntry(ClasspathEntry cpe) {
		// Ant ignores the IDE output folder!
	}

	/**
	 * Example:
	 * <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.7"/>	 
	 */
	private void processContainerClasspathEntry(ClasspathEntry cpe) {
		//intended blank: not yet used
	}

	/**
	 * Example:  
	 * <classpathentry exported="true" kind="lib" path="ambeth/jAmbeth-xml-dev-2.0.1466.jar" sourcepath="ambeth/jAmbeth-src-2.0.1466.jar"/>	 
	 */
	private void processLibraryClasspathEntry(ClasspathEntry cpe, ClasspathContainer cpc) {
		Project project = getProject();
		String cpePath = cpe.getPath().startsWith("/") ? cpe.getPath().substring(1) : cpe.getPath();
		
		// test if this is a referenced jar from a different workspace project
		String[] pathSplit = cpePath.split("[\\\\/]", 2);		
		if ( pathSplit.length > 1 ) {			
			//test if this is a Resource-Link			
			for ( ResourceLinkType rl: resourceLinks ) {
				if ( cpePath.startsWith(rl.getLocal()) ) {
					cpePath = cpePath.replace(rl.getLocal(), rl.getTarget().replaceAll("\\\\", "/"));
					cpc.addToMatchingPath(cpePath);
					return;
				}
			}			
			
			String possibleProjectName = pathSplit[0];
			String possibleProjectLocation = project.getProperty(possibleProjectName + projectLocationSuffix);
			if ( possibleProjectLocation != null ) {
				cpc.addToMatchingPath(possibleProjectLocation.replaceAll("\\\\", "/") + "/" + pathSplit[1]);
				return;
			}
		}
		
		//at this point, we can assume that its a reference within the project itself, not to a different project!
		
		
		//test if this is a Resource-Link
		String linkLocal;
		for ( ResourceLinkType rl: resourceLinks ) {
			//e.g. 
			//local=SharedLib/shared
			//remote=c:/foo/bar/lib
			//shared/cglib.2.2.2/cglib-nodep-2.2.2.jar
			linkLocal   = rl.getLocal().split("[\\\\/]", 2)[1];
			if ( cpePath.startsWith(linkLocal) ) {
				cpePath = cpePath.replace(linkLocal, rl.getTarget().replaceAll("\\\\", "/"));
				cpc.addToMatchingPath(cpePath);
				return;
			}
		}
		
		//not a resource link
		cpc.addToMatchingPath(getProjectDir() + "/" + cpePath);
	}

	public void setClassesDir(String classesDir) {
		this.classesDir = classesDir;
	}
	
	public String getProjectDir() {
		return projectDir;
	}

	public void setProjectDir(String projectDir) {
		this.projectDir = projectDir;
	}
	
	public void addMatchedPath(MatchedPathType matchedPathType) {
		this.matchedPaths.add(matchedPathType);
	}
	
	
	public void addResourceLink(ResourceLinkType resourceLinkType) {
		this.resourceLinks.add(resourceLinkType);
	}
	
	public String getClasspathFile() {
		return classpathFile;
	}

	public void setClasspathFile(String classpathFile) {
		this.classpathFile = classpathFile;
	}

	public String getProjectLocationSuffix() {
		return projectLocationSuffix;
	}

	public void setProjectLocationSuffix(String projectLocationSuffix) {
		this.projectLocationSuffix = projectLocationSuffix;
	}

	public String getDeploydir() {
		return deploydir;
	}

	public void setDeploydir(String deploydir) {
		this.deploydir = deploydir;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void setUseDefaultMainPath(boolean addDefaultMainPath) {
		this.useDefaultMainPath = addDefaultMainPath;
	}
	
	public void setUseDefaultTestPath(boolean addDefaultTestPath) {
		this.useDefaultTestPath = addDefaultTestPath;
	}
	
//	public void setUseDefaultEmmaPath(boolean addDefaultEmmaPath) {
//		this.useDefaultEmmaPath = addDefaultEmmaPath;
//	}
	
	public void setDefaultMainPathid(String defaultMainPathid) {
		this.defaultMainPathid = defaultMainPathid;
	}

	public void setDefaultTestPathid(String defaultTestPathid) {
		this.defaultTestPathid = defaultTestPathid;
	}
	
//	public void setDefaultEmmaPathid(String defaultEmmaPathid) {
//		this.defaultEmmaPathid = defaultEmmaPathid;
//	}
	

	public void setAlreadyProcessedClasspathFilesSet(
			Set<String> alreadyProcessedClasspathFilesSet) {
		this.alreadyProcessedClasspathFilesSet = alreadyProcessedClasspathFilesSet;
	}
}
