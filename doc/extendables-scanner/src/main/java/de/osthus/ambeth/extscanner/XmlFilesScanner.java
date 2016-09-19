package de.osthus.ambeth.extscanner;

import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.classbrowser.java.OutputUtil;
import de.osthus.classbrowser.java.TypeDescription;

public class XmlFilesScanner implements IInitializingBean, IXmlFilesScanner
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = "scan-path")
	protected File scanPath;

	protected SortedMap<String, TypeDescription> javaTypes;

	protected SortedMap<String, TypeDescription> csharpTypes;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		File javaFile = new File(scanPath, "export_java.xml");
		File csharpFile = new File(scanPath, "export_csharp.xml");
		if (!javaFile.exists())
		{
			throw new IllegalArgumentException("Java XML file not found: " + javaFile.getPath());
		}
		if (!csharpFile.exists())
		{
			throw new IllegalArgumentException("Csharp XML file not found: " + csharpFile.getPath());
		}
		javaTypes = OutputUtil.importFromFile(javaFile.getPath());
		Iterator<Entry<String, TypeDescription>> iter = javaTypes.entrySet().iterator();
		while (iter.hasNext())
		{
			Entry<String, TypeDescription> entry = iter.next();
			TypeDescription typeDescr = entry.getValue();
			if (typeDescr.getModuleName().endsWith("-test") || typeDescr.getModuleName().endsWith(".Test"))
			{
				iter.remove();
			}
		}
		csharpTypes = OutputUtil.importFromFile(csharpFile.getPath());
		iter = csharpTypes.entrySet().iterator();
		while (iter.hasNext())
		{
			Entry<String, TypeDescription> entry = iter.next();
			TypeDescription typeDescr = entry.getValue();
			if (typeDescr.getModuleName().endsWith("-test") || typeDescr.getModuleName().endsWith(".Test"))
			{
				iter.remove();
			}
		}
	}

	@Override
	public SortedMap<String, TypeDescription> getCsharpTypes()
	{
		return csharpTypes;
	}

	@Override
	public SortedMap<String, TypeDescription> getJavaTypes()
	{
		return javaTypes;
	}
}
