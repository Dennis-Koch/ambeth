package de.osthus.esmeralda.handler.csharp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.ConversionContext;
import de.osthus.esmeralda.SkipGenerationException;
import de.osthus.esmeralda.TypeUsing;
import de.osthus.esmeralda.handler.INodeHandlerExtension;
import de.osthus.esmeralda.handler.INodeHandlerRegistry;
import de.osthus.esmeralda.misc.EsmeType;
import de.osthus.esmeralda.misc.IEsmeFileUtil;
import de.osthus.esmeralda.misc.Lang;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;
import demo.codeanalyzer.helper.ClassInfoDataSetter;

public class CsharpClassNodeHandler implements INodeHandlerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IEsmeFileUtil fileUtil;

	@Autowired
	protected ICsharpHelper languageHelper;

	@Autowired
	protected INodeHandlerRegistry nodeHandlerRegistry;

	@Override
	public void handle(Object astNode, ConversionContext context, Writer writer)
	{
		JavaClassInfo classInfo = context.getClassInfo();

		// PHASE 1: parse the current classInfo to collect all used types. We need the usedTypes to decided later which type we can reference by its simple name
		// without ambiguity
		HashSet<TypeUsing> usedTypes = new HashSet<TypeUsing>();
		context.setUsedTypes(usedTypes);
		try
		{
			writer = new StringWriter();
			writeToWriter(classInfo, context, writer);
			writer = null;
		}
		catch (SkipGenerationException e)
		{
			return;
		}
		finally
		{
			context.setUsedTypes(null);
		}

		// PHASE 2: scan all usedTypes to decide if its simple name reference is ambiguous or not
		HashMap<String, Set<String>> simpleNameToPackagesMap = new HashMap<String, Set<String>>();
		for (TypeUsing usedType : usedTypes)
		{
			Matcher matcher = ClassInfoDataSetter.fqPattern.matcher(usedType.getTypeName());
			if (!matcher.matches())
			{
				continue;
			}
			String packageName = matcher.group(1);
			String simpleName = matcher.group(2);
			Set<String> list = simpleNameToPackagesMap.get(simpleName);
			if (list == null)
			{
				list = new HashSet<String>();
				simpleNameToPackagesMap.put(simpleName, list);
			}
			list.add(packageName);
		}
		String classNamespace = languageHelper.camelCaseName(classInfo.getPackageName());

		// PHASE 3: fill imports and usings information for this class file
		LinkedHashMap<String, String> imports = new LinkedHashMap<String, String>();
		HashSet<TypeUsing> usings = new HashSet<TypeUsing>();
		for (Entry<String, Set<String>> entry : simpleNameToPackagesMap)
		{
			Set<String> packagesSet = entry.getValue();
			if (packagesSet.size() > 1)
			{
				continue;
			}
			String packageName = (String) packagesSet.toArray()[0];
			// simpleName is unique. So we can use an import for them
			String fqTypeName = packageName + "." + entry.getKey();
			imports.put(fqTypeName, entry.getKey());
			if (classNamespace.equals(packageName))
			{
				// do not create a "using" for types in our own namespace
				continue;
			}
			TypeUsing existingTypeUsing = usedTypes.get(new TypeUsing(fqTypeName, false));
			TypeUsing newPackageUsing = new TypeUsing(packageName, existingTypeUsing.isInSilverlightOnly());
			TypeUsing existingPackageUsing = usings.get(newPackageUsing);
			if (existingPackageUsing != null)
			{
				boolean isInSilverlightOnly = existingPackageUsing.isInSilverlightOnly() && newPackageUsing.isInSilverlightOnly();
				newPackageUsing = new TypeUsing(packageName, isInSilverlightOnly);
			}
			usings.add(newPackageUsing);
		}

		context.setUsings(usings.toList());
		context.setImports(imports);
		try
		{
			writer = new StringWriter();
			writeToWriter(classInfo, context, writer);

			String newFileContent = writer.toString();

			File csharpFile = languageHelper.createTargetFile(context);
			if (csharpFile.exists())
			{
				StringBuilder existingFileContent = readFileFully(csharpFile);
				if (existingFileContent.toString().equals(newFileContent))
				{
					if (log.isDebugEnabled())
					{
						log.debug("File is already up-to-date: " + csharpFile);
					}
					return;
				}
				if (log.isInfoEnabled())
				{
					log.info("Updating file: " + csharpFile);
				}
			}
			else
			{
				if (log.isInfoEnabled())
				{
					log.info("Creating file: " + csharpFile);
				}
			}
			try (Writer fileWriter = new OutputStreamWriter(new FileOutputStream(csharpFile), "UTF-8"))
			{
				fileWriter.append(newFileContent);
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		finally
		{
			context.setImports(null);
			context.setUsings(null);
		}
	}

	protected void writeToWriter(JavaClassInfo classInfo, ConversionContext context, Writer writer)
	{
		context.setIndentationLevel(0);
		try
		{
			writeNamespace(classInfo, context, writer);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			context.setIndentationLevel(0);
		}
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

	protected void writeNamespace(final JavaClassInfo classInfo, final ConversionContext context, final Writer writer) throws Throwable
	{
		boolean firstLine = true;
		List<TypeUsing> usings = context.getUsings();
		if (usings != null && usings.size() > 0)
		{
			Collections.sort(usings);
			boolean silverlightFlagActive = false;
			for (TypeUsing using : usings)
			{
				firstLine = languageHelper.newLineIntendIfFalse(firstLine, context, writer);
				if (silverlightFlagActive && !using.isInSilverlightOnly())
				{
					// deactivate flag
					writer.append("#endif");
					languageHelper.newLineIntend(context, writer);
					silverlightFlagActive = false;
				}
				else if (!silverlightFlagActive && using.isInSilverlightOnly())
				{
					// activate flag
					languageHelper.newLineIntend(context, writer).append("#if SILVERLIGHT");
					silverlightFlagActive = true;
				}
				writer.append("using ").append(using.getTypeName()).append(';');
			}
			if (silverlightFlagActive)
			{
				// deactivate flag
				languageHelper.newLineIntend(context, writer).append("#endif");

			}
			languageHelper.newLineIntend(context, writer);
		}

		String nameSpace = languageHelper.createNameSpace(context);
		firstLine = languageHelper.newLineIntendIfFalse(firstLine, context, writer);
		writer.append("namespace ").append(nameSpace);
		languageHelper.scopeIntend(context, writer, new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				writeClass(classInfo, context, writer);
			}
		});
	}

	protected void writeClass(final JavaClassInfo classInfo, final ConversionContext context, final Writer writer) throws Throwable
	{
		languageHelper.newLineIntend(context, writer);
		if (!classInfo.isInterface())
		{
			writer.append("public class ");
		}
		else
		{
			// FIXME classInfo.isInterface() is not working
			writer.append("public interface ");
		}
		writer.append(classInfo.getName());
		boolean firstInterfaceName = true;
		String nameOfSuperClass = classInfo.getNameOfSuperClass();
		if (nameOfSuperClass != null && nameOfSuperClass.length() > 0 && !Object.class.getName().equals(nameOfSuperClass) && !"<none>".equals(nameOfSuperClass))
		{
			writer.append(" : ");
			languageHelper.writeType(nameOfSuperClass, context, writer);
			firstInterfaceName = false;
		}
		for (String nameOfInterface : classInfo.getNameOfInterfaces())
		{
			if (firstInterfaceName)
			{
				writer.append(" : ");
				firstInterfaceName = false;
			}
			else
			{
				writer.append(", ");
			}
			languageHelper.writeType(nameOfInterface, context, writer);
		}

		final INodeHandlerExtension fieldHandler = nodeHandlerRegistry.get(Lang.C_SHARP + EsmeType.FIELD);
		final INodeHandlerExtension methodHandler = nodeHandlerRegistry.get(Lang.C_SHARP + EsmeType.METHOD);

		languageHelper.scopeIntend(context, writer, new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				for (Field field : classInfo.getFields())
				{
					context.setField(field);
					fieldHandler.handle(null, context, writer);
				}
				for (Method method : classInfo.getMethods())
				{
					context.setMethod(method);
					methodHandler.handle(null, context, writer);
				}
			}
		});
	}
}
