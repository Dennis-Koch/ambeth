package de.osthus.ambeth.extscanner;

import java.io.File;
import java.util.SortedMap;

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

	// protected ILinkedMap<CtClass, File> resolvePendantInCSharp(List<CtClass> extendableTypes)
	// {
	// final HashMap<String, CtClass> expectedNames = HashMap.create(extendableTypes.size());
	// final LinkedHashMap<CtClass, File> pendantInCSharp = LinkedHashMap.create(extendableTypes.size());
	// for (CtClass extendableType : extendableTypes)
	// {
	// String expectedFileName = extendableType.getSimpleName() + ".cs";
	// expectedNames.put(expectedFileName, extendableType);
	// pendantInCSharp.put(extendableType, null);
	// }
	// FileFilter fileFilter = new FileFilter()
	// {
	// @Override
	// public boolean accept(File pathname)
	// {
	// CtClass type = expectedNames.get(pathname.getName());
	// if (type == null)
	// {
	// return false;
	// }
	// pendantInCSharp.put(type, pathname);
	// return true;
	// }
	// };
	// applyFileFilterToCSharp(fileFilter);
	// return pendantInCSharp;
	// }
	//
	// protected void applyFileFilterToCSharp(FileFilter fileFilter)
	// {
	// String[] pathItems = scanPath.split(";");
	// ArrayList<File> csprojFiles = new ArrayList<File>();
	//
	// for (String pathItem : pathItems)
	// {
	// File file = new File(pathItem);
	// if (!file.exists())
	// {
	// continue;
	// }
	// findCsProjFiles(file, csprojFiles);
	// }
	// for (File csprojFile : csprojFiles)
	// {
	// File projectDir = csprojFile.getParentFile();
	// if (projectDir.getName().endsWith(".Test"))
	// {
	// log.debug("Skipping (test) " + projectDir.getPath());
	// continue;
	// }
	// else
	// {
	// log.debug("Searching in " + projectDir.getPath());
	// }
	// findExpectedFile(projectDir, fileFilter);
	// }
	// }
	//
	// protected void findExpectedFile(File file, FileFilter fileFilter)
	// {
	// if (file.isDirectory())
	// {
	// String lowerName = file.getName().toLowerCase();
	// if (lowerName.equals("bin") || lowerName.equals("target"))
	// {
	// return;
	// }
	// File[] children = file.listFiles();
	// for (File child : children)
	// {
	// findExpectedFile(child, fileFilter);
	// }
	// return;
	// }
	// fileFilter.accept(file);
	// }
	//
	// protected void findCsProjFiles(File file, List<File> csprojFiles)
	// {
	// if (!file.isDirectory())
	// {
	// return;
	// }
	// File[] children = file.listFiles();
	// for (File child : children)
	// {
	// if (child.getName().endsWith(".csproj"))
	// {
	// // within the same directory there is only 1 csproj expected and no embedded project file either
	// csprojFiles.add(child);
	// return;
	// }
	// }
	// for (File child : children)
	// {
	// findCsProjFiles(child, csprojFiles);
	// }
	// return;
	// }
}
