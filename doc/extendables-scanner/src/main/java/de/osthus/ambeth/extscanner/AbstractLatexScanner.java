package de.osthus.ambeth.extscanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.classbrowser.java.TypeDescription;

public abstract class AbstractLatexScanner implements IStartingBean
{
	public static final String availableInCsharpOnlyOpening = "\\AvailableInCsharpOnly{";

	public static final String availableInJavaOnlyOpening = "\\AvailableInJavaOnly{";

	public static final String availableInJavaAndCsharpOpening = "\\AvailableInJavaAndCsharp{";

	public static final Pattern replaceAllAvailables = Pattern.compile(Pattern.quote(availableInCsharpOnlyOpening) + "|"
			+ Pattern.quote(availableInJavaOnlyOpening) + "|" + Pattern.quote(availableInJavaAndCsharpOpening));

	public static final Pattern texFilePattern = Pattern.compile("(.+)\\.tex");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IXmlFilesScanner xmlFilesScanner;

	@Property(name = Main.CURRENT_TIME)
	protected long currentTime;

	@Override
	public void afterStarted() throws Throwable
	{
		SortedMap<String, TypeDescription> javaTypes = xmlFilesScanner.getJavaTypes();
		SortedMap<String, TypeDescription> csharpTypes = xmlFilesScanner.getCsharpTypes();

		handle(new LinkedHashMap<String, TypeDescription>(javaTypes), new LinkedHashMap<String, TypeDescription>(csharpTypes));
	}

	abstract protected void handle(IMap<String, TypeDescription> javaTypes, IMap<String, TypeDescription> csharpTypes) throws Throwable;

	protected void writeJavadoc(String fqName, String simpleName, FileWriter fw) throws IOException
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
			FileWriter fw = new FileWriter(file);
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
}
