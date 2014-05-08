package de.osthus.ant.task.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

public class EclipseTools {

	private static final String BINARY_LOCATION_FILE = ".location";
	private static final String PROJECTS_METADATA_DIR = ".metadata/.plugins/org.eclipse.core.resources/.projects";	

	/**
	 * reads the .location metadata file for the specified project and workspace
	 * to get the project's location
	 * 
	 * @param workspaceLocation
	 * @param projectName
	 * @return
	 */
	public static String getProjectLocation(File workspaceLocation, String projectName) {
		String projectLocation = null;
		File metadataProjectsDir = new File(workspaceLocation, PROJECTS_METADATA_DIR);
		File metadataProjectDir = new File(metadataProjectsDir, projectName);
		File location = new File(metadataProjectDir, EclipseTools.BINARY_LOCATION_FILE);
		if (location.exists()) {
			//InputStream inputStream = null;
			try {
				//inputStream = new FileInputStream(location);
				String fileContent = CharStreams.toString(CharStreams.newReaderSupplier(Files.newInputStreamSupplier(location), Charsets.UTF_8));
				//String fileContent = new String(IOUtils.toByteArray(inputStream));

				String pattern = Pattern.quote(projectName);				
				if (fileContent.length() > 0 && fileContent.matches("(?s).*"+pattern+"\\x00{2,}.*")) {
					fileContent = fileContent.replaceAll("(?s)\\x00{2,}.*", "");
					fileContent = fileContent.substring(fileContent.indexOf("file:") + 4);
					while (!Character.isLetterOrDigit(fileContent.charAt(0))) {
						fileContent = fileContent.substring(1);
					}
					if (fileContent.indexOf(':') < 0) {
						fileContent = File.separator + fileContent;
					}
					projectLocation = fileContent;
				}

			} catch (IOException e) {
				e.printStackTrace();
				projectLocation = "unknown";
			} finally {
				//IOUtils.closeQuietly(inputStream);
			}
		}
		
//		if (projectLocation == null) {
//			File projectBase = new File(workspaceLocation, projectName);
//			if (projectBase.isDirectory() ) {
//				projectLocation = projectBase.getAbsolutePath();
//			}
//		}
		return projectLocation;
	}
	
	public static Map<String, String> listWorkspaceProjects(File workspaceLocation) {
		final Set<String> EXCLUDED_FILE_NAMES = new HashSet<String>();
		EXCLUDED_FILE_NAMES.add("RemoteSystemsTempFiles");
		
		File projectsDir = new File(workspaceLocation, EclipseTools.PROJECTS_METADATA_DIR);	
		File[] projectDirs = projectsDir.listFiles(new FileFilter() {			
			@Override
			public boolean accept(File file) {
				return file.isDirectory() && !EXCLUDED_FILE_NAMES.contains(file.getName());
			}
		});
		
		Map<String, String> projects = new HashMap<String, String>();
		String location;
		for ( File f: projectDirs ) {
			location = getProjectLocation(workspaceLocation, f.getName());
			if ( location != null ) {
				projects.put(f.getName(), location);
			}
		}
		
		return projects;
	}
}
