package de.osthus.ambeth.extscanner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedMap;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.classbrowser.java.OutputUtil;
import de.osthus.classbrowser.java.TypeDescription;

public abstract class AbstractLatexScanner implements IStartingBean
{
	public static final String availableInCsharpOnlyOpening = "\\AvailableInCsharpOnly{";

	public static final String availableInJavaOnlyOpening = "\\AvailableInJavaOnly{";

	public static final String availableInJavaAndCsharpOpening = "\\AvailableInJavaAndCsharp{";

	public static final Pattern replaceAllAvailables = Pattern.compile(Pattern.quote(availableInCsharpOnlyOpening) + "|"
			+ Pattern.quote(availableInJavaOnlyOpening) + "|" + Pattern.quote(availableInJavaAndCsharpOpening));

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = "scan-path")
	protected File scanPath;

	@Override
	public void afterStarted() throws Throwable
	{
		File javaFile = new File(scanPath, "export_java.xml");
		File csharpFile = new File(scanPath, "export_csharp.xml");
		if (!javaFile.exists())
		{
			throw new IllegalArgumentException("Java XML file not found: " + javaFile.getPath());
		}
		if (!csharpFile.exists())
		{
			throw new IllegalArgumentException("Java XML file not found: " + javaFile.getPath());
		}

		SortedMap<String, TypeDescription> javaTypes = OutputUtil.importFromFile(javaFile.getPath());
		SortedMap<String, TypeDescription> csharpTypes = OutputUtil.importFromFile(csharpFile.getPath());

		handle(new LinkedHashMap<String, TypeDescription>(javaTypes), new LinkedHashMap<String, TypeDescription>(csharpTypes));
	}

	abstract protected void handle(IMap<String, TypeDescription> javaTypes, IMap<String, TypeDescription> csharpTypes) throws Throwable;

	protected void writeJavadoc(String fqName, String simpleName, FileWriter fw) throws IOException
	{
		fw.append("\\javadoc{").append(fqName).append("}{").append(simpleName).append('}');
	}
}
