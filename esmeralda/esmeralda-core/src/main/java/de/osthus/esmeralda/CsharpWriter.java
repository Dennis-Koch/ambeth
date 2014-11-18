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
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
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
import demo.codeanalyzer.common.model.Annotation;
import demo.codeanalyzer.common.model.BaseJavaClassModel;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.FieldInfo;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;
import demo.codeanalyzer.helper.ClassInfoDataSetter;

public class CsharpWriter implements IStartingBean
{
	protected static final Pattern genericTypePattern = Pattern.compile("([^<>]+)<(.+)>");

	protected static final Pattern commaSplitPattern = Pattern.compile(",");

	protected static final HashMap<String, String[]> javaTypeToCsharpMap = new HashMap<String, String[]>();

	static
	{
		put("void", "void");
		put("boolean", "bool");
		put("char", "char");
		put("byte", "sbyte");
		put("short", "short");
		put("int", "int");
		put("long", "long");
		put("float", "float");
		put("double", "double");
		put(Void.class.getName(), "void");
		put(Boolean.class.getName(), "bool?");
		put(Character.class.getName(), "char?");
		put(Byte.class.getName(), "sbyte?");
		put(Short.class.getName(), "short?");
		put(Integer.class.getName(), "int?");
		put(Long.class.getName(), "long?");
		put(Float.class.getName(), "float?");
		put(Double.class.getName(), "double?");
		put(String.class.getName(), "System.String");
		put("java.lang.Class<?>", "System.Type");
		put(ThreadLocal.class.getName(), "System.Threading.ThreadLocal", "De.Osthus.Ambeth.Util.ThreadLocal");

		put(java.util.List.class.getName(), "System.Collections.Generic.IList");
		put(de.osthus.ambeth.collections.IList.class.getName(), "System.Collections.Generic.IList");
		put(de.osthus.ambeth.collections.ArrayList.class.getName(), "System.Collections.Generic.List");
		put(de.osthus.ambeth.collections.HashSet.class.getName(), "De.Osthus.Ambeth.Collections.CHashSet");
		put("java.util.Map.Entry", "De.Osthus.Ambeth.Collections.Entry");
	}

	protected static final void put(String key, String... values)
	{
		javaTypeToCsharpMap.put(key, values);
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

	protected final ThreadLocal<ISet<TypeUsing>> usedTypesTL = new ThreadLocal<ISet<TypeUsing>>();

	protected final ThreadLocal<ISet<String>> slUsedTypesTL = new ThreadLocal<ISet<String>>();

	protected final ThreadLocal<IMap<String, String>> importsTL = new ThreadLocal<IMap<String, String>>();

	protected final ThreadLocal<List<TypeUsing>> usingsTL = new ThreadLocal<List<TypeUsing>>();

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
		HashSet<TypeUsing> usedTypes = new HashSet<TypeUsing>();
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
		for (TypeUsing usedType : usedTypes)
		{
			Matcher matcher = ClassInfoDataSetter.fqPattern.matcher(usedType.typeName);
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
		String classNamespace = camelCaseName(classInfo.getPackageName());
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

	protected boolean spaceIfIfFalse(boolean value, Writer writer) throws IOException
	{
		if (!value)
		{
			writer.append(' ');
		}
		return false;
	}

	protected boolean newLineIntendIfFalse(boolean value, Writer writer) throws IOException
	{
		if (!value)
		{
			newLineIntend(writer);
		}
		return false;
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
		List<TypeUsing> usings = usingsTL.get();
		if (usings != null && usings.size() > 0)
		{
			Collections.sort(usings);
			boolean silverlightFlagActive = false;
			for (TypeUsing using : usings)
			{
				firstLine = newLineIntendIfFalse(firstLine, writer);
				if (silverlightFlagActive && !using.isInSilverlightOnly())
				{
					// deactivate flag
					writer.append("#endif");
					newLineIntend(writer);
					silverlightFlagActive = false;
				}
				else if (!silverlightFlagActive && using.isInSilverlightOnly())
				{
					// activate flag
					newLineIntend(writer).append("#if SILVERLIGHT");
					silverlightFlagActive = true;
				}
				writer.append("using ").append(using.getTypeName()).append(';');
			}
			if (silverlightFlagActive)
			{
				// deactivate flag
				newLineIntend(writer).append("#endif");

			}
			newLineIntend(writer);
		}
		String packageName = classInfo.getPackageName();
		String camelCasePackageName = camelCaseName(packageName);
		firstLine = newLineIntendIfFalse(firstLine, writer);
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

	protected String[] camelCaseName(String[] typeNames)
	{
		String[] camelCase = new String[typeNames.length];
		for (int a = typeNames.length; a-- > 0;)
		{
			camelCase[a] = camelCaseName(typeNames[a]);
		}
		return camelCase;
	}

	protected void writeClass(final JavaClassInfo classInfo, final Writer writer) throws Throwable
	{
		newLineIntend(writer).append("public class ").append(classInfo.getName());
		boolean firstInterfaceName = true;
		String nameOfSuperClass = classInfo.getNameOfSuperClass();
		if (nameOfSuperClass != null && nameOfSuperClass.length() > 0 && !Object.class.getName().equals(nameOfSuperClass))
		{
			writer.append(" : ");
			writeType(nameOfSuperClass, writer);
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
			writeType(nameOfInterface, writer);
		}
		scopeIntend(writer, new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				boolean firstEntry = true;
				IList<Field> fields = classInfo.getFields();
				for (int a = 0, size = fields.size(); a < size; a++)
				{
					Field field = fields.get(a);
					firstEntry = newLineIntendIfFalse(firstEntry, writer);
					writeField(field, writer);
				}
				for (Method method : classInfo.getMethods())
				{
					firstEntry = newLineIntendIfFalse(firstEntry, writer);
					writeMethod(method, writer);
				}
			}
		});
	}

	protected boolean writeModifiers(BaseJavaClassModel javaClassModel, Writer writer) throws Throwable
	{
		boolean firstKeyWord = true;
		if (javaClassModel.isPrivate())
		{
			writer.append("private");
			firstKeyWord = false;
		}
		else if (javaClassModel.isProtected())
		{
			writer.append("protected");
			firstKeyWord = false;
		}
		else if (javaClassModel.isPublic())
		{
			writer.append("public");
			firstKeyWord = false;
		}
		if (javaClassModel.isAbstract())
		{
			if (firstKeyWord)
			{
				firstKeyWord = false;
			}
			else
			{
				writer.append(' ');
			}
			writer.append("abstract");
		}
		if (javaClassModel.isStatic())
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
		if (javaClassModel.isFinal())
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
		return firstKeyWord;
	}

	protected void writeMethod(Method method, final Writer writer) throws Throwable
	{
		writeAnnotations(method, writer);
		newLineIntend(writer);

		boolean firstKeyWord = writeModifiers(method, writer);
		firstKeyWord = spaceIfIfFalse(firstKeyWord, writer);
		writeType(method.getReturnType(), writer).append(' ');
		String methodName = StringConversionHelper.upperCaseFirst(objectCollector, method.getName());
		// TODO: remind of the changed method name on all invocations
		writer.append(methodName).append('(');
		IList<VariableElement> parameters = method.getParameters();
		for (int a = 0, size = parameters.size(); a < size; a++)
		{
			VariableElement parameter = parameters.get(a);
			if (a > 0)
			{
				writer.append(", ");
			}
			writeType(parameter.asType().toString(), writer).append(' ');
			writer.append(parameter.getSimpleName());
		}
		writer.append(')');

		scopeIntend(writer, new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				// abc
			}
		});
	}

	protected boolean isAnnotatedWith(BaseJavaClassModel model, Class<?> annotationType)
	{
		for (Annotation annotation : model.getAnnotations())
		{
			if (annotationType.getName().equals(annotation.getType()))
			{
				return true;
			}
		}
		return false;
	}

	protected void writeField(Field field, final Writer writer) throws Throwable
	{
		writeAnnotations(field, writer);
		newLineIntend(writer);

		boolean annotatedWithAutowired = isAnnotatedWith(field, Autowired.class);
		boolean annotatedWithProperty = isAnnotatedWith(field, Property.class);

		boolean firstKeyWord;
		if (annotatedWithAutowired || annotatedWithProperty)
		{
			writer.append("public");
			firstKeyWord = false;
		}
		else
		{
			firstKeyWord = writeModifiers(field, writer);
		}
		String[] fieldTypes = field.getFieldTypes().toArray(String.class);
		firstKeyWord = spaceIfIfFalse(firstKeyWord, writer);
		writeType(fieldTypes[0], writer).append(' ');

		boolean finishWithSemicolon = true;

		if (annotatedWithAutowired || annotatedWithProperty)
		{
			String name = StringConversionHelper.upperCaseFirst(objectCollector, field.getName());
			// TODO remind changed name of the field for later access to the property get/set
			writer.append(name).append(" { protected get; set; }");
			finishWithSemicolon = false;
		}
		else if (isAnnotatedWith(field, LogInstance.class))
		{
			String name = StringConversionHelper.upperCaseFirst(objectCollector, field.getName());
			// TODO remind changed name of the field for later access to the property get/set
			writer.append(name).append(" { private get; set; }");
			finishWithSemicolon = false;
		}
		else
		{
			writer.append(field.getName());
		}

		ExpressionTree initializer = ((FieldInfo) field).getInitializer();
		if (initializer instanceof JCNewClass)
		{
			writeNewInstance((JCNewClass) initializer, writer);
		}
		else if (initializer != null)
		{
			log.warn("Could not handle: " + initializer);
		}
		if (finishWithSemicolon)
		{
			writer.append(';');
		}
	}

	protected Writer writeAnnotations(BaseJavaClassModel model, Writer writer) throws Throwable
	{
		IList<Annotation> annotations = model.getAnnotations();
		for (int a = 0, size = annotations.size(); a < size; a++)
		{
			Annotation annotation = annotations.get(a);
			writeAnnotation(annotation, writer);
		}
		return writer;
	}

	protected Writer writeAnnotation(Annotation annotation, Writer writer) throws Throwable
	{
		if (SuppressWarnings.class.getName().equals(annotation.getType()))
		{
			// skip this annotation
			return writer;
		}
		newLineIntend(writer);
		writer.append('[');
		writeType(annotation.getType(), writer);
		IMap<String, AnnotationValue> properties = annotation.getProperties();
		if (properties.size() == 0)
		{
			writer.append(']');
			return writer;
		}
		writer.append('(');
		boolean firstProperty = true;
		for (Entry<String, AnnotationValue> entry : properties)
		{
			if (firstProperty)
			{
				firstProperty = false;
			}
			else
			{
				writer.append(", ");
			}
			String propertyName = StringConversionHelper.upperCaseFirst(objectCollector, entry.getKey());
			writer.append(propertyName).append("=");
			writer.append(entry.getValue().toString());
		}
		writer.append(')');
		return writer;
	}

	protected Writer writeNewInstance(JCNewClass newClass, Writer writer) throws Throwable
	{
		List<JCExpression> arguments = newClass.args;
		List<Type> genericTypeArguments = newClass.type != null ? newClass.type.allparams() : null;
		// List<Type> argumentTypes = ((MethodType) newClass.constructor.type).getTypeArguments();
		String owner = newClass.constructor != null ? ((ClassSymbol) newClass.constructor.owner).fullname.toString() : newClass.clazz.toString();

		writer.append(" = new ");
		writeType(owner, writer);

		writeGenericTypeArguments(genericTypeArguments, writer);
		writeMethodArguments(arguments, writer);
		return writer;
	}

	protected Writer writeGenericTypeArguments(List<Type> genericTypeArguments, Writer writer) throws Throwable
	{
		if (genericTypeArguments == null || genericTypeArguments.size() == 0)
		{
			return writer;
		}
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
		return writer;
	}

	protected Writer writeMethodArguments(List<JCExpression> methodArguments, Writer writer) throws Throwable
	{
		writer.append('(');
		for (int a = 0, size = methodArguments.size(); a < size; a++)
		{
			JCExpression arg = methodArguments.get(a);
			if (a > 0)
			{
				writer.append(", ");
			}
			writer.append(arg.toString());
		}
		writer.append(')');
		return writer;
	}

	protected Writer writeType(String typeName, Writer writer) throws Throwable
	{
		typeName = typeName.trim();
		String[] mappedTypeName = javaTypeToCsharpMap.get(typeName);
		if (mappedTypeName == null)
		{
			if (typeName.endsWith("[]"))
			{
				writeType(typeName.substring(0, typeName.length() - 2), writer);
				writer.append("[]");
				return writer;
			}
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
			mappedTypeName = camelCaseName(new String[] { typeName });
		}
		ISet<TypeUsing> usedTypes = usedTypesTL.get();
		if (usedTypes != null)
		{
			usedTypes.add(new TypeUsing(mappedTypeName[0], false));
			if (mappedTypeName.length > 1)
			{
				// TypeUsing silverlightTypeUsing = new TypeUsing(mappedTypeName[1], true);
				// TypeUsing existingTypeUsing = usedTypes.get(silverlightTypeUsing);
				// if (existingTypeUsing == null)
				// {
				// // add silverlight using only if it is not already added for non-silverlight
				// usedTypes.add(silverlightTypeUsing);
				// }
			}
		}
		else
		{
			Map<String, String> imports = importsTL.get();
			if (imports != null)
			{
				String nameFromImplicitImport = imports.get(mappedTypeName[0]);
				if (nameFromImplicitImport != null)
				{
					mappedTypeName = new String[] { nameFromImplicitImport };
				}
			}
		}
		writer.append(mappedTypeName[0]);
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
				if (sourceFiles.size() < 2000)
				{
					sourceFiles.add(file);
				}
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
