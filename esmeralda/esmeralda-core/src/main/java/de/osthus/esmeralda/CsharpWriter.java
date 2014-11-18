package de.osthus.esmeralda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCNewClass;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.util.StringConversionHelper;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.FieldInfo;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;
import demo.codeanalyzer.helper.ClassInfoDataSetter;

public class CsharpWriter implements IStartingBean
{
	protected static final Pattern genericTypePattern = Pattern.compile("(.+)<(.+)>");

	protected static final Pattern commaSplitPattern = Pattern.compile(",");

	protected static final HashMap<String, String> javaTypeToCsharpMap = new HashMap<String, String>();

	static
	{
		javaTypeToCsharpMap.put("boolean", "bool");
		javaTypeToCsharpMap.put("char", "char");
		javaTypeToCsharpMap.put("byte", "sbyte");
		javaTypeToCsharpMap.put("short", "short");
		javaTypeToCsharpMap.put("int", "int");
		javaTypeToCsharpMap.put("long", "long");
		javaTypeToCsharpMap.put("float", "float");
		javaTypeToCsharpMap.put("double", "double");
		javaTypeToCsharpMap.put("java.lang.Boolean", "bool?");
		javaTypeToCsharpMap.put("java.lang.Character", "char?");
		javaTypeToCsharpMap.put("java.lang.Byte", "sbyte?");
		javaTypeToCsharpMap.put("java.lang.Short", "short?");
		javaTypeToCsharpMap.put("java.lang.Integer", "int?");
		javaTypeToCsharpMap.put("java.lang.Long", "long?");
		javaTypeToCsharpMap.put("java.lang.Float", "float?");
		javaTypeToCsharpMap.put("java.lang.Double", "double?");
		javaTypeToCsharpMap.put("java.lang.String", "System.String");
		javaTypeToCsharpMap.put("java.lang.Class<?>", "System.Type");
		javaTypeToCsharpMap.put("java.util.Map.Entry", "De.Osthus.Ambeth.Collections.Entry");
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected CodeProcessor codeProcessor;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Property(name = "source-path")
	protected File[] sourcePath;

	@Property(name = "target-path")
	protected File targetPath;

	protected final ThreadLocal<Integer> codeLevel = new ThreadLocal<Integer>();

	protected final ThreadLocal<ISet<String>> usedTypesTL = new ThreadLocal<ISet<String>>();

	protected final ThreadLocal<IMap<String, String>> importsTL = new ThreadLocal<IMap<String, String>>();

	protected final ThreadLocal<List<String>> usingsTL = new ThreadLocal<List<String>>();

	@Override
	public void afterStarted() throws Throwable
	{
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);)
		{
			File[] allSourceFiles = findAllSourceFiles().toArray(File.class);
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

			convertJavaToCsharp(classInfo, targetFile);
		}
	}

	protected void convertJavaToCsharp(JavaClassInfo classInfo, File csharpFile)
	{
		// PHASE 1: parse the current classInfo to collect all used types. We need the usedTypes to decided later which type we can reference by its simple name
		// without ambiguity
		HashSet<String> usedTypes = new HashSet<String>();
		usedTypesTL.set(usedTypes);
		try
		{
			StringWriter writer = new StringWriter();
			writeToWriter(classInfo, writer);
		}
		finally
		{
			usedTypesTL.remove();
		}

		// PHASE 2: scan all usedTypes to decide if its simple name reference is ambiguous or not
		HashMap<String, Set<String>> simpleNameToPackagesMap = new HashMap<String, Set<String>>();
		for (String usedType : usedTypes)
		{
			Matcher matcher = ClassInfoDataSetter.fqPattern.matcher(usedType);
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
		// PHASE 3: fill imports and usings information for this class file
		LinkedHashMap<String, String> imports = new LinkedHashMap<String, String>();
		HashSet<String> usings = new HashSet<String>();
		for (Entry<String, Set<String>> entry : simpleNameToPackagesMap)
		{
			Set<String> packagesSet = entry.getValue();
			if (packagesSet.size() > 1)
			{
				continue;
			}
			String packageName = (String) packagesSet.toArray()[0];
			// simpleName is unique. So we can use an import for them
			imports.put(packageName + "." + entry.getKey(), entry.getKey());
			usings.add(packageName);
		}
		usingsTL.set(usings.toList());
		importsTL.set(imports);
		try
		{
			StringWriter writer = new StringWriter();
			writeToWriter(classInfo, writer);

			String newFileContent = writer.toString();

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
			importsTL.remove();
			usingsTL.remove();
		}
	}

	protected void writeToWriter(JavaClassInfo classInfo, Writer writer)
	{
		codeLevel.set(Integer.valueOf(0));
		try
		{
			writeNamespace(classInfo, writer);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			codeLevel.remove();
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

	protected Writer newLineIntend(Writer writer) throws IOException
	{
		writer.append('\n');
		Integer intendCount = codeLevel.get();
		for (int a = intendCount.intValue(); a-- > 0;)
		{
			writer.append('\t');
		}
		return writer;
	}

	protected void scopeIntend(Writer writer, IBackgroundWorkerDelegate run) throws Throwable
	{
		newLineIntend(writer).append('{');
		Integer intendLevel = codeLevel.get();
		codeLevel.set(Integer.valueOf(intendLevel.intValue() + 1));
		try
		{
			run.invoke();
		}
		finally
		{
			codeLevel.set(intendLevel);
		}
		newLineIntend(writer).append('}');
	}

	protected void writeNamespace(final JavaClassInfo classInfo, final Writer writer) throws Throwable
	{
		boolean firstLine = true;
		List<String> usings = usingsTL.get();
		if (usings != null && usings.size() > 0)
		{
			Collections.sort(usings);
			for (String using : usings)
			{
				if (firstLine)
				{
					firstLine = false;
				}
				else
				{
					newLineIntend(writer);
				}
				writer.append("using ").append(using).append(';');
			}
			newLineIntend(writer);
		}
		String packageName = classInfo.getPackageName();
		String camelCasePackageName = camelCaseName(packageName);
		if (firstLine)
		{
			firstLine = false;
		}
		else
		{
			newLineIntend(writer);
		}
		writer.append("namespace ").append(camelCasePackageName);
		scopeIntend(writer, new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				writeClass(classInfo, writer);
			}
		});
	}

	protected String camelCaseName(String typeName)
	{
		String[] packageSplit = typeName.split(Pattern.quote("."));
		StringBuilder sb = new StringBuilder();
		for (int a = 0, size = packageSplit.length; a < size; a++)
		{
			if (a > 0)
			{
				sb.append('.');
			}
			sb.append(StringConversionHelper.upperCaseFirst(objectCollector, packageSplit[a]));
		}
		return sb.toString();
	}

	protected void writeClass(final JavaClassInfo classInfo, final Writer writer) throws Throwable
	{
		newLineIntend(writer).append("public class ").append(classInfo.getName());
		boolean firstInterfaceName = true;
		for (String nameOfInterface : classInfo.getNameOfInterfaces())
		{
			if (firstInterfaceName)
			{
				writer.append(" : ");
			}
			else
			{
				writer.append(", ");
			}
			writeType(nameOfInterface, writer);
		}

		scopeIntend(writer, new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				for (Field field : classInfo.getFields())
				{
					writeField(field, writer);
				}
				for (Method method : classInfo.getMethods())
				{
					// writeMethod(method, writer);
				}
			}
		});
	}

	protected void writeField(Field field, final Writer writer) throws Throwable
	{
		boolean firstKeyWord = true;
		newLineIntend(writer);
		newLineIntend(writer);
		if (field.isPrivate())
		{
			writer.append("private");
			firstKeyWord = false;
		}
		else if (field.isProtected())
		{
			writer.append("protected");
			firstKeyWord = false;
		}
		else if (field.isPublic())
		{
			writer.append("public");
			firstKeyWord = false;
		}
		if (field.isStatic())
		{
			if (firstKeyWord)
			{
				firstKeyWord = false;
			}
			else
			{
				writer.append(' ');
			}
			writer.append("static");
		}
		if (field.isFinal())
		{
			if (firstKeyWord)
			{
				firstKeyWord = false;
			}
			else
			{
				writer.append(' ');
			}
			writer.append("readonly");
		}
		String[] fieldTypes = field.getFieldTypes().toArray(String.class);
		if (firstKeyWord)
		{
			firstKeyWord = false;
		}
		else
		{
			writer.append(' ');
		}
		writeType(fieldTypes[0], writer).append(' ').append(field.getName());

		ExpressionTree initializer = ((FieldInfo) field).getInitializer();
		if (initializer instanceof JCNewClass)
		{
			JCNewClass newClass = ((JCNewClass) initializer);
			List<JCExpression> args = newClass.args;
			List<Type> genericTypeArguments = ((ClassType) newClass.type).allparams_field;
			List<Type> argumentTypes = ((MethodType) newClass.constructor.type).argtypes;
			String owner = ((ClassSymbol) newClass.constructor.owner).fullname.toString();

			writer.append(" = new ");
			writeType(owner, writer);

			if (genericTypeArguments.size() > 0)
			{
				writer.append('<');
				for (int a = 0, size = genericTypeArguments.size(); a < size; a++)
				{
					Type genericTypeArgument = genericTypeArguments.get(a);
					if (a > 0)
					{
						writer.append(", ");
					}
					writeType(genericTypeArgument.toString(), writer);
				}
				writer.append('>');
			}

			writer.append('(');
			for (int a = 0, size = args.size(); a < size; a++)
			{
				JCExpression arg = args.get(a);
				if (a > 0)
				{
					writer.append(", ");
				}
				writer.append(arg.toString());
			}
			writer.append(')');
		}
		else if (initializer != null)
		{
			System.out.println();
		}

		writer.append(';');
	}

	protected Writer writeType(String typeName, Writer writer) throws Throwable
	{
		typeName = typeName.trim();
		String mappedTypeName = javaTypeToCsharpMap.get(typeName);
		if (mappedTypeName == null)
		{
			Matcher genericTypeMatcher = genericTypePattern.matcher(typeName);
			if (genericTypeMatcher.matches())
			{
				String plainType = genericTypeMatcher.group(1);

				writeType(plainType, writer).append('<');

				String typeArguments = genericTypeMatcher.group(2);
				String[] typeArgumentsSplit = commaSplitPattern.split(typeArguments);
				boolean firstArgument = true;
				for (String typeArgumentSplit : typeArgumentsSplit)
				{
					if (firstArgument)
					{
						firstArgument = false;
					}
					else
					{
						writer.append(',');
					}
					writeType(typeArgumentSplit, writer);
				}
				writer.append('>');
				return writer;
			}
			mappedTypeName = camelCaseName(typeName);
		}
		ISet<String> usedTypes = usedTypesTL.get();
		if (usedTypes != null)
		{
			usedTypes.add(mappedTypeName);
		}
		else
		{
			Map<String, String> imports = importsTL.get();
			if (imports != null)
			{
				String nameFromImplicitImport = imports.get(mappedTypeName);
				if (nameFromImplicitImport != null)
				{
					mappedTypeName = nameFromImplicitImport;
				}
			}
		}
		writer.append(mappedTypeName);
		return writer;
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
