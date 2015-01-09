package de.osthus.esmeralda;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

import javax.annotation.processing.AbstractProcessor;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.handler.IClassHandler;
import de.osthus.esmeralda.handler.IClassInfoFactory;
import de.osthus.esmeralda.handler.csharp.ICsClassHandler;
import de.osthus.esmeralda.handler.csharp.ICsHelper;
import de.osthus.esmeralda.handler.js.IJsClassHandler;
import de.osthus.esmeralda.handler.js.IJsClasspathManager;
import de.osthus.esmeralda.handler.js.IJsHelper;
import de.osthus.esmeralda.misc.IEsmeFileUtil;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.misc.StatementCount;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.helper.ClassInfoDataSetter;

public class ConversionManager implements IStartingBean
{
	private static final Comparator<Object[]> METHOD_NAME_COUNT_COMPARATOR = new Comparator<Object[]>()
	{
		@Override
		public int compare(Object[] o1, Object[] o2)
		{
			int result = ((Integer) o2[1]).compareTo((Integer) o1[1]);
			if (result == 0)
			{
				result = ((String) o1[0]).compareTo((String) o2[0]);
			}
			return result;
		}
	};

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IASTHelper astHelper;

	@Autowired
	protected IClassInfoFactory classInfoFactory;

	@Autowired
	protected IJsClasspathManager jsClasspathManager;

	@Autowired
	protected CodeProcessor codeProcessor;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected ICsClassHandler csClassHandler;

	@Autowired
	protected IJsClassHandler jsClassHandler;

	@Autowired
	protected ICsHelper csHelper;

	@Autowired
	protected IJsHelper jsHelper;

	@Autowired
	protected IEsmeFileUtil fileUtil;

	@Property(name = "source-path")
	protected File[] sourcePath;

	@Property(name = "snippet-path")
	protected File snippetPath;

	@Property(name = "target-path")
	protected File targetPath;

	@Override
	public void afterStarted() throws Throwable
	{
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);)
		{
			File[] allSourceFiles = fileUtil.findAllSourceFiles(sourcePath).toArray(File.class);
			Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(allSourceFiles);

			CompilationTask task = compiler.getTask(null, fileManager, new DiagnosticListener<JavaFileObject>()
			{
				@Override
				public void report(Diagnostic<? extends JavaFileObject> diagnostic)
				{
					System.out.println(diagnostic.getMessage(null));
				}
			}, null, null, fileObjects);

			ArrayList<AbstractProcessor> processors = new ArrayList<AbstractProcessor>();
			processors.add(codeProcessor);
			task.setProcessors(processors);

			try
			{
				task.call();
			}
			catch (FastBreakException e)
			{
				// intended blank
			}
			catch (RuntimeException e)
			{
				if (!(e.getCause() instanceof FastBreakException))
				{
					throw e;
				}
				// intended blank
			}
		}

		ArrayList<JavaClassInfo> classInfos = codeProcessor.getClassInfos();
		HashMap<String, JavaClassInfo> fqNameToClassInfoMap = new HashMap<String, JavaClassInfo>();

		for (JavaClassInfo classInfo : classInfos)
		{
			String fqName = classInfo.getFqName();
			if (!fqNameToClassInfoMap.putIfNotExists(fqName, classInfo))
			{
				throw new IllegalStateException("Full qualified name is not unique: " + fqName);
			}
			String nonGenericFqName = astHelper.extractNonGenericType(classInfo.getFqName());
			if (!nonGenericFqName.equals(fqName) && !fqNameToClassInfoMap.putIfNotExists(nonGenericFqName, classInfo))
			{
				throw new IllegalStateException("Full qualified name is not unique: " + nonGenericFqName);
			}
		}

		StatementCount csMetric = new StatementCount("C#");
		StatementCount jsMetric = new StatementCount("JS");
		HashMap<String, Integer> csCalledMethods = new HashMap<>();
		HashSet<String> csDefinedMethods = new HashSet<>();
		HashMap<String, Integer> jsCalledMethods = new HashMap<>();
		HashSet<String> jsDefinedMethods = new HashSet<>();

		int classInfoProgress = 0, classInfoCount = classInfos.size();
		long lastLog = System.currentTimeMillis();

		for (JavaClassInfo classInfo : classInfos)
		{
			String packageName = classInfo.getPackageName();
			if (packageName == null)
			{
				log.warn("Skipped classinfo without a package name: " + classInfo);
				classInfoCount--;
				continue;
			}

			ConversionContext csContext = new ConversionContext();
			csContext.setFqNameToClassInfoMap(fqNameToClassInfoMap);
			csContext.setLanguage(Lang.C_SHARP);
			csContext.setSnippetPath(snippetPath);
			csContext.setTargetPath(targetPath);
			csContext.setLanguagePath("csharp");
			csContext.setGenericTypeSupported(true);
			csContext.setMetric(csMetric);
			csContext.setNsPrefixRemove("de.osthus.");
			csContext.setClassInfo(classInfo);
			csContext.setAstHelper(astHelper);
			csContext.setClassInfoFactory(classInfoFactory);
			csContext.setLanguageHelper(csHelper);
			csContext.setCalledMethods(csCalledMethods);
			csContext.setDefinedMethods(csDefinedMethods);

			invokeClassHandler(csClassHandler, csContext);

			ConversionContext jsContext = new ConversionContext();
			jsContext.setFqNameToClassInfoMap(fqNameToClassInfoMap);
			jsContext.setLanguage(Lang.JS);
			jsContext.setSnippetPath(snippetPath);
			jsContext.setTargetPath(targetPath);
			jsContext.setLanguagePath("js");
			jsContext.setGenericTypeSupported(false);
			jsContext.setMetric(jsMetric);
			jsContext.setNsPrefixRemove("de.osthus.");
			jsContext.setClassInfo(classInfo);
			jsContext.setAstHelper(astHelper);
			jsContext.setClassInfoFactory(classInfoFactory);
			jsContext.setLanguageHelper(jsHelper);
			jsContext.setCalledMethods(jsCalledMethods);
			jsContext.setDefinedMethods(jsDefinedMethods);

			invokeClassHandler(jsClassHandler, jsContext);

			classInfoProgress++;
			if (System.currentTimeMillis() - lastLog < 5000)
			{
				continue;
			}
			log.info("Handled " + ((int) ((classInfoProgress * 10000) / (double) classInfoCount)) / 100.0 + "% of java source. Last conversion '"
					+ classInfo.toString() + "'");
			lastLog = System.currentTimeMillis();
		}

		if (log.isInfoEnabled())
		{
			// log.info("Missing methods in CS:");
			// logMissingMethods(csCalledMethods, csDefinedMethods, IClassPathManager.EXISTING_METHODS_CS);
			log.info("Missing methods in JS:");
			logMissingMethods(jsCalledMethods, jsDefinedMethods, jsClasspathManager);

			log.info(csMetric.toString());
			log.info(jsMetric.toString());
		}
	}

	protected void invokeClassHandler(IClassHandler classHandler, IConversionContext newContext)
	{
		IConversionContext oldContext = context.getCurrent();
		context.setCurrent(newContext);
		try
		{
			invokeHandleMethod(classHandler);
		}
		catch (TypeResolveException e)
		{
			log.error(e);
		}
		catch (Throwable e)
		{
			JavaClassInfo classInfo = newContext.getClassInfo();
			log.error(RuntimeExceptionUtil.mask(e, "Error occured while processing type '" + classInfo.getName() + "'"));
		}
		finally
		{
			context.setCurrent(oldContext);
		}
	}

	protected void invokeHandleMethod(final IClassHandler classHandler)
	{
		IConversionContext context = this.context.getCurrent();

		ILanguageHelper languageHelper = classHandler.getLanguageHelper();

		IBackgroundWorkerDelegate writeToWriterDelegate = new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				classHandler.handle();
			}
		};

		// PHASE 1: parse the current classInfo to collect all used types. We need the usedTypes to decided later which type we can reference by its simple name
		// without ambiguity
		HashSet<TypeUsing> usedTypes = new HashSet<TypeUsing>();
		context.setDryRun(true);
		context.setUsedTypes(usedTypes);
		try
		{
			astHelper.writeToStash(writeToWriterDelegate);
		}
		catch (SkipGenerationException e)
		{
			return;
		}
		finally
		{
			context.setUsedTypes(null);
			context.setDryRun(false);
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
		String classNamespace = languageHelper.createNamespace();

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

		String newFileContent;
		context.setUsings(usings.toList());
		context.setImports(imports);
		try
		{
			newFileContent = astHelper.writeToStash(writeToWriterDelegate);
		}
		finally
		{
			context.setImports(null);
			context.setUsings(null);
		}
		fileUtil.updateFile(newFileContent, languageHelper.createTargetFile());
	}

	protected void logMissingMethods(HashMap<String, Integer> calledMethods, HashSet<String> definedMethods, IClasspathManager classpathManager)
	{
		HashMap<String, Integer> missingMethods = new HashMap<>(calledMethods);
		HashSet<String> existingMethods = new HashSet<>();
		existingMethods.addAll(definedMethods);
		existingMethods.addAll(classpathManager.getClasspathMethods());

		for (String existingMethod : existingMethods)
		{
			missingMethods.remove(existingMethod);
		}

		Object[][] methodArray = new Object[missingMethods.size()][];
		int index = 0;
		for (Entry<String, Integer> missingMethod : missingMethods)
		{
			methodArray[index++] = new Object[] { missingMethod.getKey(), missingMethod.getValue() };
		}
		Arrays.sort(methodArray, METHOD_NAME_COUNT_COMPARATOR);

		for (Object[] fullMethodNameCount : methodArray)
		{
			String fullMethodName = (String) fullMethodNameCount[0];
			// Workaround since interface methods are not known as 'defined'
			if (fullMethodName.startsWith("Ambeth."))
			{
				continue;
			}

			Integer count = (Integer) fullMethodNameCount[1];
			log.info(count + "\t" + fullMethodName);
		}
	}
}
