package de.osthus.esmeralda;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.regex.Pattern;

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
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.esmeralda.handler.INodeHandlerExtension;
import de.osthus.esmeralda.handler.INodeHandlerRegistry;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class CsharpWriter implements IStartingBean
{
	protected static final Pattern genericTypePattern = Pattern.compile("([^<>]+)<(.+)>");

	protected static final Pattern commaSplitPattern = Pattern.compile(",");

	protected static final HashMap<String, String[]> javaTypeToCsharpMap = new HashMap<String, String[]>();

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected CodeProcessor codeProcessor;

	@Autowired
	protected IFileUtil fileUtil;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected INodeHandlerRegistry nodeHandlerRegistry;

	@Property(name = "source-path")
	protected File[] sourcePath;

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

		INodeHandlerExtension classHandler = nodeHandlerRegistry.get(Lang.C_SHARP + EsmeType.CLASS);
		ArrayList<JavaClassInfo> classInfos = codeProcessor.getClassInfos();
		for (JavaClassInfo classInfo : classInfos)
		{
			String packageName = classInfo.getPackageName();
			if (packageName == null)
			{
				continue;
			}
			packageName = classInfo.getPackageName().replace(".", "/");
			File targetFilePath = new File(targetPath, packageName);
			targetFilePath.mkdirs();
			File targetFile = new File(targetFilePath, classInfo.getName() + ".cs");

			ConversionContext context = new ConversionContext();
			context.setClassInfo(classInfo);
			context.setTargetFile(targetFile);

			classHandler.handle(null, context, null);
		}
	}

	protected IList<File> findAllSourceFiles()
	{
		final ArrayList<File> sourceFiles = new ArrayList<File>();

		searchForFiles(sourcePath, new FileFilter()
		{
			@Override
			public boolean accept(File file)
			{
				if (!file.getName().endsWith(".java"))
				{
					return false;
				}
				if (file.getPath().contains("repackaged"))
				{
					return false;
				}
				sourceFiles.add(file);
				return true;
			}
		});
		return sourceFiles;
	}

	protected void searchForFiles(File[] baseDirs, FileFilter fileFilter)
	{
		for (File pathItem : baseDirs)
		{
			File rootDir;
			try
			{
				rootDir = pathItem.getCanonicalFile();
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			searchForFiles(rootDir, rootDir, fileFilter);
		}
	}

	protected void searchForFiles(File baseDir, File currFile, FileFilter fileFilter)
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
				searchForFiles(baseDir, child, fileFilter);
			}
			return;
		}
		fileFilter.accept(currFile);
	}
}
