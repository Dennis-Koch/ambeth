package com.koch.ambeth.extscanner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.Charsets;

import com.koch.ambeth.extscanner.model.IMultiPlatformFeature;
import com.koch.ambeth.extscanner.model.ISourceFileAware;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.classbrowser.java.TypeDescription;

public abstract class AbstractLatexScanner implements IInitializingBean, IStartingBean {
	public static final Pattern labelNamePattern =
			Pattern.compile(".*\\\\section\\{[^\\}]*\\}\\s*\\\\label\\{([^\\}]*)\\}.*", Pattern.DOTALL);

	public static final String[] setAPIs = {"\\SetAPI{nothing}", //
			"\\SetAPI{J}", //
			"\\SetAPI{C}", //
			"\\SetAPI{J-C}", //
			"\\SetAPI{JS}", //
			"\\SetAPI{J-JS}", //
			"\\SetAPI{C-JS}", //
			"\\SetAPI{J-C-JS}",//
	};

	public static final Pattern replaceAllAvailables = Pattern.compile("\\\\SetAPI\\{[^\\{\\}]*\\}");

	public static final Pattern texFilePattern = Pattern.compile("(.+)\\.tex");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IXmlFilesScanner xmlFilesScanner;

	@Property(name = "target-all-dir")
	protected String allTexPath;

	@Property(name = "source-path")
	protected String sourcePath;

	@Property(name = "target-source-path", mandatory = false)
	protected String targetSourcePath;

	@Property(name = Main.CURRENT_TIME)
	protected long currentTime;

	@Override
	public void afterPropertiesSet() throws Throwable {
		SortedMap<String, TypeDescription> javaTypes = xmlFilesScanner.getJavaTypes();
		SortedMap<String, TypeDescription> csharpTypes = xmlFilesScanner.getCsharpTypes();

		buildModel(new LinkedHashMap<>(javaTypes), new LinkedHashMap<>(csharpTypes));
	}

	@Override
	public void afterStarted() throws Throwable {
		handleModel();
	}

	protected String writeSetAPI(StringBuilder sb, IMultiPlatformFeature multiPlatformFeature) {
		String api = getAPI(multiPlatformFeature);
		return replaceAllAvailables.matcher(sb).replaceAll(Matcher.quoteReplacement(api));
	}

	protected File getAllDir() {
		try {
			File allDir = new File(allTexPath);
			allDir.mkdirs();
			return allDir.getCanonicalFile();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	abstract protected void buildModel(IMap<String, TypeDescription> javaTypes,
			IMap<String, TypeDescription> csharpTypes) throws Throwable;

	abstract protected void handleModel() throws Throwable;

	protected String readLabelName(File currFile) {
		StringBuilder sb = readFileFully(currFile);
		Matcher matcher = labelNamePattern.matcher(sb);
		if (!matcher.matches()) {
			log.warn("Label not found in file " + currFile);
			return null;
		}
		return matcher.group(1);
	}

	protected String getAPI(IMultiPlatformFeature multiPlatformFeature) {
		int index = (multiPlatformFeature.inJava() ? 1 : 0) + (multiPlatformFeature.inCSharp() ? 2 : 0)
				+ +(multiPlatformFeature.inJavascript() ? 4 : 0);

		return setAPIs[index];
	}

	protected void writeJavadoc(String fqName, String simpleName, OutputStreamWriter fw)
			throws IOException {
		fw.append("\\javadoc{").append(fqName).append("}{").append(simpleName).append('}');
	}

	protected void searchForFiles(String baseDirs,
			IMap<String, IFileFoundDelegate> nameToFileFoundDelegates,
			IFileFoundDelegate... allMatchDelegates) {
		String[] pathItems = baseDirs.split(";");
		for (String pathItem : pathItems) {
			File rootDir;
			try {
				rootDir = new File(pathItem).getCanonicalFile();
			}
			catch (IOException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
			searchForFiles(rootDir, rootDir, nameToFileFoundDelegates, allMatchDelegates);
		}
	}

	protected void searchForFiles(File baseDir, File currFile,
			IMap<String, IFileFoundDelegate> nameToFileFoundDelegates,
			IFileFoundDelegate[] allMatchDelegates) {
		if (currFile == null) {
			return;
		}
		if (currFile.isDirectory()) {
			File[] listFiles = currFile.listFiles();
			for (File child : listFiles) {
				searchForFiles(baseDir, child, nameToFileFoundDelegates, allMatchDelegates);
				if (nameToFileFoundDelegates.size() == 0 && allMatchDelegates.length == 0) {
					return;
				}
			}
			return;
		}
		String relativeFilePath =
				currFile.getPath().substring(baseDir.getPath().length() + 1).replace('\\', '/');

		for (IFileFoundDelegate allMatchDelegate : allMatchDelegates) {
			allMatchDelegate.fileFound(currFile, relativeFilePath);
		}
		IFileFoundDelegate fileFoundDelegate = nameToFileFoundDelegates.remove(currFile.getName());
		if (fileFoundDelegate == null) {
			return;
		}
		fileFoundDelegate.fileFound(currFile, relativeFilePath);
	}

	protected void queueFileSearch(TypeDescription typeDesc, String expectedSuffix,
			IMap<String, IFileFoundDelegate> nameToFileFoundDelegates,
			IFileFoundDelegate fileFoundDelegate) {
		if (typeDesc == null) {
			return;
		}
		String fileName = typeDesc.getName() + expectedSuffix;
		nameToFileFoundDelegates.put(fileName, fileFoundDelegate);
	}

	protected StringBuilder readFileFully(File file) {
		try {
			StringBuilder sb = new StringBuilder((int) file.length());
			BufferedReader rd = new BufferedReader(new FileReader(file));
			try {
				int oneByte;
				while ((oneByte = rd.read()) != -1) {
					sb.append((char) oneByte);
				}
				return sb;
			}
			finally {
				rd.close();
			}
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void updateFileFully(File file, CharSequence sb) {
		log.info("Updating " + file);
		try {
			// update existing file
			OutputStreamWriter fw =
					new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8"));
			try {
				fw.append(sb);
			}
			finally {
				fw.close();
			}
			file.setLastModified(currentTime);
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void escapeValue(String value, OutputStreamWriter fw) throws Exception {
		// http://tex.stackexchange.com/questions/34580/escape-character-in-latex
		for (int a = 0, size = value.length(); a < size; a++) {
			char oneChar = value.charAt(a);
			switch (oneChar) {
				case '&':
				case '%':
				case '$':
				case '#':
				case '_':
				case '{':
				case '}': {
					fw.append('\\');
					fw.append(oneChar);
					continue;
				}
				case '~': {
					fw.append("\\textasciitilde{}");
					continue;
				}
				case '^': {
					fw.append("\\textasciicircum{}");
					continue;
				}
				case '\\': {
					fw.append("\\textbackslash{}");
					continue;
				}
			}
			fw.append(oneChar);
		}
	}

	protected void writeAvailability(IMultiPlatformFeature feature, OutputStreamWriter fw)
			throws Exception {
		// Java
		escapeValue(feature.inJava() ? "X" : " ", fw);
		fw.append(" & ");

		// C#
		escapeValue(feature.inCSharp() ? "X" : " ", fw);
		// fw.append(" & ");

		// // Javascript
		// escapeValue(feature.inJavascript() ? "X" : " ", fw);
	}

	protected void findCorrespondingSourceFiles(
			Iterable<? extends ISourceFileAware> sourceFileAwares) {
		HashMap<String, IFileFoundDelegate> nameToFileFoundDelegates = new HashMap<>();

		for (ISourceFileAware sourceFileAware : sourceFileAwares) {
			final ISourceFileAware fAnnotationEntry = sourceFileAware;

			queueFileSearch(sourceFileAware.getJavaSrc(), ".java", nameToFileFoundDelegates,
					new IFileFoundDelegate() {
						@Override
						public void fileFound(File file, String relativeFilePath) {
							file = filterFileContent(file, relativeFilePath);
							fAnnotationEntry.setJavaFile(file, relativeFilePath);
						}
					});
			queueFileSearch(sourceFileAware.getCsharpSrc(), ".cs", nameToFileFoundDelegates,
					new IFileFoundDelegate() {
						@Override
						public void fileFound(File file, String relativeFilePath) {
							file = filterFileContent(file, relativeFilePath);
							fAnnotationEntry.setCsharpFile(getAllDir(), relativeFilePath);
						}
					});
		}
		searchForFiles(sourcePath, nameToFileFoundDelegates, new IFileFoundDelegate[0]);
	}

	protected File filterFileContent(File file, String relativeFilePath) {
		if (targetSourcePath == null) {
			return file;
		}
		Path targetFile = Paths.get(targetSourcePath, relativeFilePath);
		try {
			long lastModified = file.lastModified();
			if (Files.exists(targetFile)
					&& Files.getLastModifiedTime(targetFile).toMillis() == lastModified) {
				return file;
			}
			Files.createDirectories(targetFile.getParent());

			try (BufferedWriter os = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(targetFile, StandardOpenOption.CREATE), Charsets.UTF_8))) {
				BufferedReader reader = Files.newBufferedReader(file.toPath(), Charsets.UTF_8);
				int step = 0;
				int emptyLineCount = 0;
				String line;
				while ((line = reader.readLine()) != null) {
					if (step < 4) {
						switch (step) {
							case 0: {
								if (line.equals("/*-")) {
									step++;
								}
								break;
							}
							case 1: {
								if (line.equals(" * #%L")) {
									step++;
								}
								break;
							}
							case 2: {
								if (line.equals(" * #L%")) {
									step++;
								}
								break;
							}
							case 3: {
								if (line.equals(" */")) {
									step++;
									continue;
								}
								break;
							}
						}
					}
					if (step == 0 || step == 4) {
						if (line.isEmpty()) {
							emptyLineCount++;
						}
						else {
							emptyLineCount = 0;
						}
						if (emptyLineCount > 1) {
							continue; // skip multiple empty lines
						}
						os.write(line);
						os.write(System.lineSeparator());
					}
				}
			}
			Files.setLastModifiedTime(targetFile, FileTime.fromMillis(lastModified));
			return targetFile.toFile();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e, "Error occurred while processing '" + file + "'");
		}
	}
}
