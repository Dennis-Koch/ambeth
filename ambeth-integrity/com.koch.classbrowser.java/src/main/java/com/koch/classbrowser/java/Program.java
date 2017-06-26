/**
 *
 */
package com.koch.classbrowser.java;

import java.io.Console;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

/**
 * @author juergen.panser
 *
 */
public class Program {

	// ---- CONSTANTS ----------------------------------------------------------

	private static final Pattern[] excludedJarFiles = {
			Pattern.compile("[\\\\/]jAmbeth-\\d[^\\\\/]+?$"),
			Pattern.compile("[\\\\/]jAmbeth-jUnit-\\d[^\\\\/]+?$") };

	public static final String DECO = "\n========================================\n";

	public static final String EXTENSION_JAR = "jar";

	public static final String ARG_KEY_HELP = "help";

	public static final String ARG_KEY_TARGETPATH = "targetPath";

	public static final String ARG_KEY_TEMP_PATH = "tempPath";

	public static final String ARG_KEY_JARFOLDERS = "jarFolders";

	public static final String ARG_KEY_LIBRARYJARFOLDERS = "libraryJarFolders";

	public static final String ARG_KEY_MODULEROOTPATH = "moduleRootPath";

	public static final String ARG_KEY_MODULES = "modulesToBeAnalyzed";

	public static final String ARG_PATH_DELIMITER = ",";

	// ---- VARIABLES ----------------------------------------------------------

	private static boolean doLog = true;

	// ---- METHODS ------------------------------------------------------------

	/**
	 * Entry point of the application.
	 *
	 * @param args
	 *          Program arguments
	 */
	public static void main(String[] args) {
		try {
			Properties.getApplication().fillWithCommandLineArgs(args);
			run();
			System.exit(ExitCode.SUCCESS.getCode());
		}
		catch (Throwable e) {
			e.printStackTrace();
			// showMessage(e.getMessage() + "\n\n");
		}
		System.exit(ExitCode.ERROR.getCode());
	}

	/**
	 * The program logic.
	 */
	private static void run() {

		if (wantsHelp()) {
			displayHelpAndWait();
			return;
		}

		String targetPath = getTargetPathEnsured();

		// Read the class files
		List<String> allLibraryJarFiles = readJarFiles(ARG_KEY_LIBRARYJARFOLDERS);
		List<String> allJarFilesToBeAnalyzed = readJarFiles(ARG_KEY_JARFOLDERS);
		preprocessJarFilesToBeAnalyzed(allJarFilesToBeAnalyzed); // TODO What should be filtered?
		if (allJarFilesToBeAnalyzed == null || allJarFilesToBeAnalyzed.isEmpty()) {
			log("No JAR files found to be analyzed!");
			return;
		}
		else {
			if (allLibraryJarFiles != null) {
				log("All " + allLibraryJarFiles.size() + " library JAR files successfully read.");
			}
			log("All " + allJarFilesToBeAnalyzed.size()
					+ " JAR files which have to be (possibly) analyzed successfully read.");
		}

		List<ClassHolder> classes = getClassesFromJars(allJarFilesToBeAnalyzed, allLibraryJarFiles);

		// Read the source files of all modules and create a map to identify the modules
		String moduleRootPath = getModuleRootPathEnsured();
		Map<String, String> moduleMap = createModuleMap(moduleRootPath);

		List<String> modulesToBeAnalyzed = readModulesToBeAnalyzed(ARG_KEY_MODULES);
		List<TypeDescription> foundTypes = ParserUtil.analyzeClasses(classes, moduleMap,
				modulesToBeAnalyzed);

		OutputUtil.export(foundTypes, targetPath);
		log("FINISHED!");
	}

	private static void preprocessJarFilesToBeAnalyzed(List<String> allJarFilesToBeAnalyzed) {
		for (int i = allJarFilesToBeAnalyzed.size(); i-- > 0;) {
			String jarFileName = allJarFilesToBeAnalyzed.get(i);
			for (Pattern excludedName : excludedJarFiles) {
				if (excludedName.matcher(jarFileName).find()) {
					allJarFilesToBeAnalyzed.remove(i);
					break;
				}
			}
		}
	}

	/**
	 * Check if help should be displayed.
	 *
	 * @return True if the help should be displayed
	 */
	private static boolean wantsHelp() {
		return Properties.getApplication().collectAllPropertyKeys().contains(ARG_KEY_HELP);
	}

	/**
	 * Display the help text.
	 */
	private static void displayHelpAndWait() {
		List<String> messages = Arrays.asList( //
				DECO, //
				"    Welcome to the JAVA class browser.", //
				DECO, //
				"The following arguments are supported:\n", //
				ARG_KEY_HELP + " displays this help screen\n", //
				ARG_KEY_TARGETPATH + "={path} sets the path where the export file should be written to\n", //
				ARG_KEY_MODULEROOTPATH
						+ "={modulerootpath} sets the root path to the source files needed to identify the modules; "
						+ "modules have to be direct children of this path\n", //
				ARG_KEY_MODULES
						+ "={modulelist} defines the a list of modules to be analyzed (optional; if omitted all classes of the jars "
						+ "from the jar path are analyzed); the path separator is the '" + ARG_PATH_DELIMITER
						+ "'; no whitespaces allowed", // )
				ARG_KEY_JARFOLDERS
						+ "={jarpaths} defines the paths to the jars to be analyzed; the path separator is the '"
						+ ARG_PATH_DELIMITER + "'\n\n", //
				ARG_KEY_LIBRARYJARFOLDERS
						+ "={jarpaths} defines the paths to library jars which are needed to create the class path; the path separator is the '"
						+ ARG_PATH_DELIMITER + "'\n\n", //
				"Example (which exports the file '" + OutputUtil.EXPORT_FILE_NAME
						+ "' to the given target path):\n", //
				"java -D" + ARG_KEY_JARFOLDERS + "=c:\\temp\\ClassBrowser\\MyJars" + ARG_PATH_DELIMITER
						+ "c:\\temp\\OtherJars -D" + //
						ARG_KEY_JARFOLDERS + "=c:\\temp\\ClassBrowser\\LibraryJars" + //
						ARG_KEY_TARGETPATH + "=c:\\temp\\export -jar JavaClassbrowser.jar", //
				DECO);
		Console console = System.console();
		if (console == null) {
			for (String message : messages) {
				log(message);
			}
			return;
		}
		for (String message : messages) {
			console.format(message);
		}
	}

	/**
	 * Show the given message (on console - if available).
	 *
	 * @param message
	 *          Message to display
	 */
	public static void showMessage(String message) {
		Console console = System.console();
		if (console == null) {
			if (!StringUtils.isBlank(message)) {
				log(message);
			}
		}
		else {
			if (!StringUtils.isBlank(message)) {
				console.format(message);
			}
		}
	}

	/**
	 * Log out the given message. Convenience method to have a single point where to change the
	 * behavior.
	 *
	 * @param message
	 *          Message to log
	 */
	public static void log(String message) {
		if (doLog && !StringUtils.isBlank(message)) {
			System.out.println(message);
		}
	}

	/**
	 * Get target path for the export file. Throws an exception if the path wasn't found or doesn't
	 * exist.
	 *
	 * @return Target path
	 */
	private static String getTargetPathEnsured() {
		return getPathEnsured(ARG_KEY_TARGETPATH, "Target path");
	}

	/**
	 * Get module root path. Throws an exception if the path wasn't found or doesn't exist.
	 *
	 * @return Module root path
	 */
	private static String getModuleRootPathEnsured() {
		return getPathEnsured(ARG_KEY_MODULEROOTPATH, "Module root path");
	}

	/**
	 * Get path from the given property and ensure that it is a directory. Throws an exception if the
	 * path wasn't found or doesn't exist.
	 *
	 * @param propertyKey
	 *          Property key to get the path from
	 * @param identifier
	 *          Identifier used in all message texts
	 * @return Path; never null
	 */
	private static String getPathEnsured(String propertyKey, String identifier) {
		String path = Properties.getApplication().getString(propertyKey);
		if (!StringUtils.isBlank(path)) {
			File file = new File(path);
			if (!file.exists()) {
				file.mkdirs();
			}
			if (file.isDirectory()) {
				return path;
			}
			else {
				throw new IllegalArgumentException(identifier + " '" + path + "' is not a directory!");
			}
		}
		throw new IllegalArgumentException(identifier + " not found!");
	}

	/**
	 * @param propertyKey
	 *          Property to read the jar folders from
	 * @return List of jars or null
	 */
	private static List<String> readJarFiles(String propertyKey) {
		List<String> allJarFiles = null;
		String jarFolderSequence = Properties.getApplication().getString(propertyKey);
		if (!StringUtils.isBlank(jarFolderSequence)) {
			String[] jarFolders = jarFolderSequence.split(ARG_PATH_DELIMITER);
			allJarFiles = readFiles(EXTENSION_JAR, jarFolders, false);
		}
		return allJarFiles;
	}

	/**
	 * Read all files with the given file extension found in the given folders.
	 *
	 * @param fileExtension
	 *          File extension of the classes to read
	 * @param folders
	 *          Folders to search in
	 * @param recursive
	 *          Flag if sub folders are searched as well
	 * @return List of classes; never null
	 */
	private static List<String> readFiles(String fileExtension, String[] folders, boolean recursive) {
		List<String> allFiles = new ArrayList<>();
		for (String folder : folders) {
			File directory = new File(folder);
			Collection<File> files = FileUtils.listFiles(directory, new String[] { fileExtension },
					recursive);
			for (File file : files) {
				String fullFileName = file.getAbsolutePath();
				allFiles.add(fullFileName);
			}
		}
		return allFiles;
	}

	/**
	 * Read the modules to be analyzed from the given property.
	 *
	 * @param propertyKey
	 *          Optional property to read the list from
	 * @return List of module names (lower case) to be analyzed or null (all modules are analyzed)
	 */
	private static List<String> readModulesToBeAnalyzed(String propertyKey) {
		List<String> modules = null;
		String modulesSequence = Properties.getApplication().getString(propertyKey);
		if (!StringUtils.isBlank(modulesSequence)) {
			if (modulesSequence.trim().endsWith(ARG_PATH_DELIMITER)) {
				throw new IllegalArgumentException(
						"Please check the argument '" + propertyKey + "'! It seems to be incomplete...");
			}
			String[] splittedModuleSequence = modulesSequence.split(ARG_PATH_DELIMITER);
			modules = new ArrayList<>(splittedModuleSequence.length);
			for (String moduleName : splittedModuleSequence) {
				modules.add(moduleName.trim().toLowerCase());
			}
		}
		return modules;
	}

	/**
	 * Create the map with the module name of each class file.
	 *
	 * @param rootPath
	 *          The root path - all modules have to be direct children of this path
	 * @return Map with the module name of each class file; key is the full qualified class name in
	 *         LOWER CASE and value the module name
	 */
	private static Map<String, String> createModuleMap(String rootPaths) {
		Map<String, String> moduleMap = new TreeMap<>();
		// Assumption: the modules are the first child hierarchy
		try {
			for (String rootPath : rootPaths.split(Pattern.quote(";"))) {
				File rootDir = new File(rootPath);
				if (!rootDir.isDirectory()) {
					// Path extractedFolder = Paths.get(getPathEnsured(ARG_KEY_TEMP_PATH, "Temporary path"));
					// JarFile jarFile = new JarFile(rootDir);
					// try {
					// Enumeration<JarEntry> entries = jarFile.entries();
					// while (entries.hasMoreElements()) {
					// JarEntry entry = entries.nextElement();
					// if (entry.isDirectory()) {
					// continue;
					// }
					// String entryName = entry.getName();
					// // Path extractedLocation =
					// // Files.newOutputStream(path, options)
					// jarFile.getInputStream(entry);
					// }
					// }
					// finally {
					// jarFile.close();
					// }
					throw new IllegalArgumentException("Root path '" + rootPath + "' is not a directory!");
				}
				File[] foundInRoot = rootDir.listFiles();
				if (foundInRoot != null) {
					for (File rootFile : foundInRoot) {
						if (rootFile.isDirectory()) {
							String moduleName = rootFile.getName();
							String modulePath = rootFile.getAbsolutePath();

							Collection<File> files = FileUtils.listFiles(rootFile, new String[] { "java" }, true);
							for (File file : files) {
								String fullFileName = file.getAbsolutePath();
								String relativeName = StringUtils.replace(fullFileName, modulePath,
										StringUtils.EMPTY);
								String[] splittedRelativeName = StringUtils.split(relativeName, "\\/");
								final String className;
								if (splittedRelativeName.length > 3 && "src".equals(splittedRelativeName[0])
										&& "java".equals(splittedRelativeName[2])) {
									String[] adaptedRelativeName = Arrays.copyOfRange(splittedRelativeName, 3,
											splittedRelativeName.length);
									className = StringUtils.join(adaptedRelativeName, ".").toLowerCase();
								}
								else if (splittedRelativeName.length > 1
										&& ("asm_src".equals(splittedRelativeName[0])
												|| "addon".equals(splittedRelativeName[0]))) {
									String[] adaptedRelativeName = Arrays.copyOfRange(splittedRelativeName, 1,
											splittedRelativeName.length);
									className = StringUtils.join(adaptedRelativeName, ".").toLowerCase();
								}
								else {
									className = StringUtils.join(splittedRelativeName, ".").toLowerCase();
								}
								moduleMap.put(className, moduleName);
							}
						}
					}
				}
			}
			return moduleMap;
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	/**
	 * @param analyzeJarPaths
	 *          Path to jars which should be analyzed; mandatory
	 * @param libraryJarPaths
	 *          Path to jars which are needed to create the complete class path (libraries,
	 *          dependencies); optional
	 * @return List of classes of the jars to be analyzed
	 */
	private static List<ClassHolder> getClassesFromJars(List<String> analyzeJarPaths,
			List<String> libraryJarPaths) {
		if (analyzeJarPaths == null) {
			throw new IllegalArgumentException("Jar paths to the analyzed jars missing!");
		}
		List<ClassHolder> classes = new ArrayList<>();
		try {
			List<URL> urls = new ArrayList<>();
			if (libraryJarPaths != null) {
				for (String pathToJar : libraryJarPaths) {
					urls.add(new URL("jar:file:" + pathToJar + "!/"));
				}
			}
			for (String pathToJar : analyzeJarPaths) {
				urls.add(new URL("jar:file:" + pathToJar + "!/"));
			}
			URLClassLoader cl = URLClassLoader.newInstance(urls.toArray(new URL[0]), null);
			for (String pathToJar : analyzeJarPaths) {
				JarFile jarFile = new JarFile(pathToJar);
				try {
					Enumeration<JarEntry> enumeration = jarFile.entries();
					while (enumeration.hasMoreElements()) {
						JarEntry je = enumeration.nextElement();
						if (je.isDirectory() || !je.getName().endsWith(".class")) {
							continue;
						}
						// -6 because of .class
						String className = je.getName().substring(0, je.getName().length() - 6);
						className = className.replace('/', '.');
						try {
							Class<?> c = cl.loadClass(className);
							classes.add(new ClassHolder(pathToJar, c));
						}
						catch (Throwable e) {
							throw RuntimeExceptionUtil.mask(e,
									"Error occurred while trying to load '" + className + "'");
						}
					}
				}
				catch (Throwable e) {
					throw RuntimeExceptionUtil.mask(e,
							"Error occurred while handling jar '" + pathToJar + "'");
				}
				finally {
					jarFile.close();
				}
			}
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		return classes;
	}

}
