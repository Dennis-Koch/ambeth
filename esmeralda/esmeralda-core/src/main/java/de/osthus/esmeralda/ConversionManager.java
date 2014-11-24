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
import de.osthus.esmeralda.handler.IClassInfoFactory;
import de.osthus.esmeralda.handler.INodeHandlerExtension;
import de.osthus.esmeralda.handler.INodeHandlerRegistry;
import de.osthus.esmeralda.misc.EsmeType;
import de.osthus.esmeralda.misc.IEsmeFileUtil;
import de.osthus.esmeralda.misc.Lang;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class ConversionManager implements IStartingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

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
			String fqName = classInfo.getPackageName() + "." + classInfo.getName();
			if (!fqNameToClassInfoMap.putIfNotExists(fqName, classInfo))
			{
				throw new IllegalStateException("Full qualified name is not unique: " + fqName);
			}
		}
		for (JavaClassInfo classInfo : classInfos)
		{
			String packageName = classInfo.getPackageName();
			if (packageName == null)
			{
				log.warn("Skipped classinfo without a package name: " + classInfo);
				continue;
			}

			ConversionContext csContext = new ConversionContext();
			csContext.setFqNameToClassInfoMap(fqNameToClassInfoMap);
			csContext.setSnippetPath(snippetPath);
			csContext.setTargetPath(targetPath);
			csContext.setLanguagePath("csharp");
			csContext.setNsPrefixRemove("de.osthus.");
			csContext.setClassInfo(classInfo);
			csContext.setClassInfoFactory(classInfoFactory);

			invokeNodeHandler(csClassHandler, csContext);

			ConversionContext jsContext = new ConversionContext();
			jsContext.setFqNameToClassInfoMap(fqNameToClassInfoMap);
			jsContext.setSnippetPath(snippetPath);
			jsContext.setTargetPath(targetPath);
			jsContext.setLanguagePath("js");
			jsContext.setNsPrefixRemove("de.osthus.");
			jsContext.setClassInfo(classInfo);
			csContext.setClassInfoFactory(classInfoFactory);

			invokeNodeHandler(jsClassHandler, jsContext);
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
