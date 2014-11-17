package de.osthus.ambeth.extscanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.classbrowser.java.TypeDescription;

public abstract class AbstractLatexScanner implements IInitializingBean, IStartingBean
{
	public static final Pattern labelNamePattern = Pattern.compile(".*\\\\section\\{[^\\}]*\\}\\s*\\\\label\\{([^\\}]*)\\}.*", Pattern.DOTALL);

	public static final String[] setAPIs = { "\\SetAPI{nothing}",//
			"\\SetAPI{J}",//
			"\\SetAPI{C}",//
			"\\SetAPI{J-C}",//
			"\\SetAPI{JS}",//
			"\\SetAPI{J-JS}",//
			"\\SetAPI{C-JS}",//
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

	@Property(name = Main.CURRENT_TIME)
	protected long currentTime;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		SortedMap<String, TypeDescription> javaTypes = xmlFilesScanner.getJavaTypes();
		SortedMap<String, TypeDescription> csharpTypes = xmlFilesScanner.getCsharpTypes();

		buildModel(new LinkedHashMap<String, TypeDescription>(javaTypes), new LinkedHashMap<String, TypeDescription>(csharpTypes));
	}

	@Override
	public void afterStarted() throws Throwable
	{
		handleModel();
	}

	protected String writeSetAPI(StringBuilder sb, IMultiPlatformFeature multiPlatformFeature)
	{
		String api = getAPI(multiPlatformFeature);
		return replaceAllAvailables.matcher(sb).replaceAll(Matcher.quoteReplacement(api));
	}

	protected File getAllDir()
	{
		try
		{
			File allDir = new File(allTexPath);
			allDir.mkdirs();
			return allDir.getCanonicalFile();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	abstract protected void buildModel(IMap<String, TypeDescription> javaTypes, IMap<String, TypeDescription> csharpTypes) throws Throwable;

	abstract protected void handleModel() throws Throwable;

	protected String readLabelName(File currFile)
	{
		StringBuilder sb = readFileFully(currFile);
		Matcher matcher = labelNamePattern.matcher(sb);
		if (!matcher.matches())
		{
			log.warn("Label not found in file " + currFile);
			return null;
		}
		return matcher.group(1);
	}

	protected String getAPI(IMultiPlatformFeature multiPlatformFeature)
	{
		int index = (multiPlatformFeature.inJava() ? 1 : 0) + (multiPlatformFeature.inCSharp() ? 2 : 0) + +(multiPlatformFeature.inJavascript() ? 4 : 0);

		return setAPIs[index];
	}

	protected void writeJavadoc(String fqName, String simpleName, OutputStreamWriter fw) throws IOException
	{
		fw.append("\\javadoc{").append(fqName).append("}{").append(simpleName).append('}');
	}

	protected void searchForFiles(String baseDirs, IMap<String, IFileFoundDelegate> nameToFileFoundDelegates)
	{
		String[] pathItems = baseDirs.split(";");
		for (String pathItem : pathItems)
		{
			File rootDir;
			try
			{
				rootDir = new File(pathItem).getCanonicalFile();
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			searchForFiles(rootDir, rootDir, nameToFileFoundDelegates);
		}
	}

	protected void searchForFiles(File baseDir, File currFile, IMap<String, IFileFoundDelegate> nameToFileFoundDelegates)
	{
		if (currFile == null)
		{
			return;
		}
		if (currFile.isDirectory())
		{
			File[] listFiles = currFile.listFiles();
			for (File child : listFiles)
			{
				searchForFiles(baseDir, child, nameToFileFoundDelegates);
				if (nameToFileFoundDelegates.size() == 0)
				{
					return;
				}
			}
			return;
		}
		IFileFoundDelegate fileFoundDelegate = nameToFileFoundDelegates.remove(currFile.getName());
		if (fileFoundDelegate == null)
		{
			return;
		}
		String relativeFilePath = currFile.getPath().substring(baseDir.getPath().length() + 1).replaceAll(Pattern.quote("\\"), Matcher.quoteReplacement("/"));
		fileFoundDelegate.fileFound(currFile, relativeFilePath);
	}

	protected void queueFileSearch(TypeDescription typeDesc, String expectedSuffix, IMap<String, IFileFoundDelegate> nameToFileFoundDelegates,
			IFileFoundDelegate fileFoundDelegate)
	{
		if (typeDesc == null)
		{
			return;
		}
		String fileName = typeDesc.getName() + expectedSuffix;
		nameToFileFoundDelegates.put(fileName, fileFoundDelegate);
	}

	protected StringBuilder readFileFully(File file)
	{
		try
		{
			StringBuilder sb = new StringBuilder((int) file.length());
			BufferedReader rd = new BufferedReader(new FileReader(file));
			try
			{
				int oneByte;
				while ((oneByte = rd.read()) != -1)
				{
					sb.append((char) oneByte);
				}
				return sb;
			}
			finally
			{
				rd.close();
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void updateFileFully(File file, CharSequence sb)
	{
		log.info("Updating " + file);
		try
		{
			// update existing file
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8"));
			try
			{
				fw.append(sb);
			}
			finally
			{
				fw.close();
			}
			file.setLastModified(currentTime);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void findCorrespondingSourceFiles(Iterable<? extends ISourceFileAware> sourceFileAwares)
	{
		HashMap<String, IFileFoundDelegate> nameToFileFoundDelegates = new HashMap<String, IFileFoundDelegate>();

		for (ISourceFileAware sourceFileAware : sourceFileAwares)
		{
			final ISourceFileAware fAnnotationEntry = sourceFileAware;

			queueFileSearch(sourceFileAware.getJavaSrc(), ".java", nameToFileFoundDelegates, new IFileFoundDelegate()
			{
				@Override
				public void fileFound(File file, String relativeFilePath)
				{
					fAnnotationEntry.setJavaFile(file, relativeFilePath);
				}
			});
			queueFileSearch(sourceFileAware.getCsharpSrc(), ".cs", nameToFileFoundDelegates, new IFileFoundDelegate()
			{
				@Override
				public void fileFound(File file, String relativeFilePath)
				{
					fAnnotationEntry.setCsharpFile(getAllDir(), relativeFilePath);
				}
			});
		}
		searchForFiles(sourcePath, nameToFileFoundDelegates);
	}
}
