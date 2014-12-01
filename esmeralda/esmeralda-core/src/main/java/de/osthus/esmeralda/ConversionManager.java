package de.osthus.esmeralda;

import java.io.File;

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
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.MaskingRuntimeException;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.handler.IClassInfoFactory;
import de.osthus.esmeralda.handler.INodeHandlerExtension;
import de.osthus.esmeralda.handler.INodeHandlerRegistry;
import de.osthus.esmeralda.misc.EsmeType;
import de.osthus.esmeralda.misc.IEsmeFileUtil;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.misc.StatementCount;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class ConversionManager implements IStartingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IASTHelper astHelper;

	@Autowired
	protected IClassInfoFactory classInfoFactory;

	@Autowired
	protected CodeProcessor codeProcessor;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IEsmeFileUtil fileUtil;

	@Autowired
	protected INodeHandlerRegistry nodeHandlerRegistry;

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

		INodeHandlerExtension csClassHandler = nodeHandlerRegistry.get(Lang.C_SHARP + EsmeType.CLASS);
		INodeHandlerExtension jsClassHandler = nodeHandlerRegistry.get(Lang.JS + EsmeType.CLASS);

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
			csContext.setSnippetPath(snippetPath);
			csContext.setTargetPath(targetPath);
			csContext.setLanguagePath("csharp");
			csContext.setGenericTypeSupported(true);
			csContext.setMetric(csMetric);
			csContext.setNsPrefixRemove("de.osthus.");
			csContext.setClassInfo(classInfo);
			csContext.setAstHelper(astHelper);
			csContext.setClassInfoFactory(classInfoFactory);

			invokeNodeHandler(csClassHandler, csContext);

			ConversionContext jsContext = new ConversionContext();
			jsContext.setFqNameToClassInfoMap(fqNameToClassInfoMap);
			jsContext.setSnippetPath(snippetPath);
			jsContext.setTargetPath(targetPath);
			jsContext.setLanguagePath("js");
			jsContext.setGenericTypeSupported(false);
			jsContext.setMetric(jsMetric);
			jsContext.setNsPrefixRemove("de.osthus.");
			jsContext.setClassInfo(classInfo);
			jsContext.setAstHelper(astHelper);
			jsContext.setClassInfoFactory(classInfoFactory);

			invokeNodeHandler(jsClassHandler, jsContext);

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
			log.info(csMetric.toString());
			log.info(jsMetric.toString());
		}
	}

	protected void invokeNodeHandler(INodeHandlerExtension nodeHandler, IConversionContext newContext)
	{
		IConversionContext oldContext = context.getCurrent();
		context.setCurrent(newContext);
		try
		{
			nodeHandler.handle(null);
		}
		catch (TypeResolveException e)
		{
			log.error(e);
		}
		catch (Throwable e)
		{
			JavaClassInfo classInfo = newContext.getClassInfo();
			log.error(new MaskingRuntimeException("Error occured while processing type '" + classInfo.getName() + "'", e));
		}
		finally
		{
			context.setCurrent(oldContext);
		}
	}
}
