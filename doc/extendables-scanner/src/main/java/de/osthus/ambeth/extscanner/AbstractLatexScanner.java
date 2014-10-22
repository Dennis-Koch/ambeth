package de.osthus.ambeth.extscanner;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ClasspathScanner;

public class AbstractLatexScanner extends ClasspathScanner
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = "scan-path")
	protected String scanPath;

	@Override
	protected IList<URL> buildUrlsFromClasspath(ClassPool pool)
	{
		ArrayList<URL> urls = new ArrayList<URL>();
		String[] pathItems = scanPath.split(";");
		for (String pathItem : pathItems)
		{
			File file = new File(pathItem);
			findRealPath(file, urls);
		}
		return urls;
	}

	protected void findRealPath(File file, List<URL> urls)
	{
		if (!file.canRead())
		{
			throw new IllegalStateException("Can not access '" + file.getPath() + "'");
		}
		File srcPath = new File(file, "target/classes");
		if (srcPath.exists())
		{
			try
			{
				if (file.getName().endsWith("-test"))
				{
					log.debug("Skipping (test) " + srcPath.getCanonicalPath());
					return;
				}
				log.debug("Searching in " + srcPath.getCanonicalPath());
				urls.add(new URL("file://" + srcPath.getCanonicalPath()));
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			return;
		}
		File[] childFiles = file.listFiles();
		for (File childFile : childFiles)
		{
			if (childFile.isDirectory())
			{
				findRealPath(childFile, urls);
			}
		}
	}

	protected ILinkedMap<CtClass, File> resolvePendantInCSharp(List<CtClass> extendableTypes)
	{
		final HashMap<String, CtClass> expectedNames = HashMap.create(extendableTypes.size());
		final LinkedHashMap<CtClass, File> pendantInCSharp = LinkedHashMap.create(extendableTypes.size());
		for (CtClass extendableType : extendableTypes)
		{
			String expectedFileName = extendableType.getSimpleName() + ".cs";
			expectedNames.put(expectedFileName, extendableType);
			pendantInCSharp.put(extendableType, null);
		}
		FileFilter fileFilter = new FileFilter()
		{
			@Override
			public boolean accept(File pathname)
			{
				CtClass type = expectedNames.get(pathname.getName());
				if (type == null)
				{
					return false;
				}
				pendantInCSharp.put(type, pathname);
				return true;
			}
		};
		applyFileFilterToCSharp(fileFilter);
		return pendantInCSharp;
	}

	protected void applyFileFilterToCSharp(FileFilter fileFilter)
	{

		String[] pathItems = scanPath.split(";");
		ArrayList<File> csprojFiles = new ArrayList<File>();

		for (String pathItem : pathItems)
		{
			File file = new File(pathItem);
			if (!file.exists())
			{
				continue;
			}
			findCsProjFiles(file, csprojFiles);
		}
		for (File csprojFile : csprojFiles)
		{
			File projectDir = csprojFile.getParentFile();
			if (projectDir.getName().endsWith(".Test"))
			{
				log.debug("Skipping (test) " + projectDir.getPath());
				continue;
			}
			else
			{
				log.debug("Searching in " + projectDir.getPath());
			}
			findExpectedFile(projectDir, fileFilter);
		}
	}

	protected void findExpectedFile(File file, FileFilter fileFilter)
	{
		if (file.isDirectory())
		{
			String lowerName = file.getName().toLowerCase();
			if (lowerName.equals("bin") || lowerName.equals("target"))
			{
				return;
			}
			File[] children = file.listFiles();
			for (File child : children)
			{
				findExpectedFile(child, fileFilter);
			}
			return;
		}
		fileFilter.accept(file);
	}

	protected void findCsProjFiles(File file, List<File> csprojFiles)
	{
		if (!file.isDirectory())
		{
			return;
		}
		File[] children = file.listFiles();
		for (File child : children)
		{
			if (child.getName().endsWith(".csproj"))
			{
				// within the same directory there is only 1 csproj expected and no embedded project file either
				csprojFiles.add(child);
				return;
			}
		}
		for (File child : children)
		{
			findCsProjFiles(child, csprojFiles);
		}
		return;
	}
}
