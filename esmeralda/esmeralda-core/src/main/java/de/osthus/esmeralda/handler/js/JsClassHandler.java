package de.osthus.esmeralda.handler.js;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.lang.model.element.VariableElement;

import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCNewClass;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.IClasspathManager;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.handler.IClassHandler;
import de.osthus.esmeralda.handler.IFieldHandler;
import de.osthus.esmeralda.handler.IMethodHandler;
import de.osthus.esmeralda.handler.IVariable;
import de.osthus.esmeralda.handler.js.transformer.DefaultMethodTransformer;
import de.osthus.esmeralda.misc.IToDoWriter;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.snippet.ISnippetManager;
import de.osthus.esmeralda.snippet.ISnippetManagerFactory;
import de.osthus.esmeralda.snippet.SnippetTrigger;
import demo.codeanalyzer.common.model.BaseJavaClassModel;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.FieldInfo;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class JsClassHandler implements IClassHandler
{
	@SuppressWarnings("rawtypes")
	private static final ArrayList EMPTY_ARRAY_LIST = new ArrayList();

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IASTHelper astHelper;

	@Autowired("jsClasspathManager")
	protected IClasspathManager jsClasspathManager;

	@Autowired
	protected IJsHelper languageHelper;

	@Autowired("jsFieldHandler")
	protected IFieldHandler fieldHandler;

	@Autowired("jsMethodHandler")
	protected IMethodHandler methodHandler;

	@Autowired(IJsOverloadManager.STATIC)
	protected IJsOverloadManager overloadManagerStatic;

	@Autowired(IJsOverloadManager.NON_STATIC)
	protected IJsOverloadManager overloadManagerNonStatic;

	@Autowired
	protected ISnippetManagerFactory snippetManagerFactory;

	@Autowired
	protected IToDoWriter todoWriter;

	@Override
	public IJsHelper getLanguageHelper()
	{
		return languageHelper;
	}

	@Override
	public void handle()
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		JavaClassInfo classInfo = context.getClassInfo();

		String namespace = languageHelper.createNamespace();
		String fullClassName = namespace + "." + languageHelper.removeGenerics(classInfo.getName());
		if (jsClasspathManager.isInClasspath(fullClassName))
		{
			Path source = jsClasspathManager.getFullPath(fullClassName);
			try
			{
				byte[] allBytes = Files.readAllBytes(source);
				writer.append(new String(allBytes));
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			return;
		}

		ArrayList<Field> fieldsToInit = createInitOps(classInfo);
		ArrayList<String> duplicateNames = findFieldMethodNameCollisions(classInfo);

		ISnippetManager snippetManager = snippetManagerFactory.createSnippetManager();
		context.setSnippetManager(snippetManager);
		context.setIndentationLevel(0);
		try
		{
			writer.append("Ext.define(");
			writeName(classInfo);
			writer.append(", ");
			if (!classInfo.isInterface())
			{
				writeData(classInfo);
			}
			else
			{
				writer.append("{}");
			}
			if (!fieldsToInit.isEmpty())
			{
				writer.append(", ");
				writeCreateFunction(fieldsToInit, classInfo, writer);
			}
			writer.append(");");
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			context.setIndentationLevel(0);
			context.setSnippetManager(null);
		}
	}

	protected ArrayList<Field> createInitOps(JavaClassInfo classInfo)
	{
		ArrayList<Field> fields = new ArrayList<>(classInfo.getFields());
		ArrayList<Field> privateStaticFields = createView(fields, Boolean.TRUE, Boolean.TRUE);
		fields.removeAll(privateStaticFields);
		if (fields.isEmpty())
		{
			@SuppressWarnings("unchecked")
			ArrayList<Field> emptyArrayList = EMPTY_ARRAY_LIST;
			return emptyArrayList;
		}

		ArrayList<Field> fieldsToInit = new ArrayList<>();
		for (Field field : fields)
		{
			ExpressionTree initializer = ((FieldInfo) field).getInitializer();
			if (initializer != null && !(initializer instanceof JCLiteral))
			{
				fieldsToInit.add(field);
			}
		}

		return fieldsToInit;
	}

	protected ArrayList<String> findFieldMethodNameCollisions(JavaClassInfo classInfo)
	{
		IList<Field> fields = classInfo.getFields();
		IList<Method> methods = classInfo.getMethods();

		HashSet<String> methodNames = new HashSet<>((int) (methods.size() / 0.75));
		ArrayList<String> duplicateNames = new ArrayList<String>();

		for (Method method : methods)
		{
			methodNames.add(method.getName());
		}

		for (Field field : fields)
		{
			String fieldName = field.getName();
			if (methodNames.contains(fieldName))
			{
				duplicateNames.add(fieldName);
				todoWriter.write("Field and Method with the same name", fieldName, classInfo, field.getLocationInfo().getStartOffset());
			}
		}

		return duplicateNames;
	}

	protected void writeName(JavaClassInfo classInfo)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		String namespace = languageHelper.createNamespace();
		String name = classInfo.getName();

		writer.append('"');
		if (!namespace.isEmpty())
		{
			name = namespace + "." + name;
		}
		languageHelper.writeType(name);
		writer.append('"');
	}

	protected void writeData(final JavaClassInfo classInfo)
	{
		IConversionContext context = this.context.getCurrent();
		final IWriter writer = context.getWriter();

		IList<Method> methods = classInfo.getMethods();

		HashMap<String, ArrayList<Method>> overloadedMethodsStatic = findOverloadedMethods(methods, true);
		overloadManagerStatic.registerOverloads(classInfo.getFqName(), overloadedMethodsStatic);
		HashMap<String, ArrayList<Method>> overloadedMethodsNonStatic = findOverloadedMethods(methods, false);
		overloadManagerNonStatic.registerOverloads(classInfo.getFqName(), overloadedMethodsNonStatic);

		writer.append("function (");
		languageHelper.writeSimpleName(classInfo);
		writer.append(") ");

		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				boolean firstLine = true;

				firstLine = writePrivateStaticVars(classInfo, writer, firstLine);

				firstLine = languageHelper.newLineIndentIfFalse(firstLine);
				languageHelper.newLineIndent();
				writer.append("return ");
				languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
				{
					@Override
					public void invoke() throws Throwable
					{
						boolean firstLine = true;

						firstLine = writeExtend(classInfo, writer, firstLine);
						firstLine = writeImplements(classInfo, writer, firstLine);
						firstLine = writeRequires(classInfo, writer, firstLine);

						firstLine = writerStatic(classInfo, writer, firstLine);
						firstLine = writePrivates(classInfo, writer, firstLine);

						firstLine = writeFields(classInfo, writer, firstLine);

						firstLine = writeConfig(classInfo, writer, firstLine);
						firstLine = writeAccessors(firstLine);

						firstLine = writeMethods(classInfo, writer, firstLine);
					}
				});
				writer.append(";");
			}
		});
	}

	protected boolean writePrivateStaticVars(JavaClassInfo classInfo, IWriter writer, boolean firstLine)
	{
		IList<Field> privateStaticFields = createView(classInfo.getFields(), Boolean.TRUE, Boolean.TRUE);
		firstLine = writeFieldListWithSnippets(privateStaticFields, writer, firstLine, false);
		return firstLine;
	}

	protected boolean writeExtend(JavaClassInfo classInfo, IWriter writer, boolean firstLine)
	{
		String nameOfSuperClass = classInfo.getNameOfSuperClass();

		firstLine = languageHelper.newLineIndentWithCommaIfFalse(firstLine);
		languageHelper.newLineIndent();
		writer.append("\"extend\": \"");
		if (nameOfSuperClass == null || nameOfSuperClass.isEmpty() || Object.class.getName().equals(nameOfSuperClass) || "<none>".equals(nameOfSuperClass))
		{
			nameOfSuperClass = "Ambeth.Base";
			languageHelper.writeTypeDirect(nameOfSuperClass);
		}
		else
		{
			languageHelper.writeType(nameOfSuperClass);
		}
		writer.append('"');

		return firstLine;
	}

	protected boolean writeImplements(JavaClassInfo classInfo, IWriter writer, boolean firstLine)
	{
		IList<String> interfaceNames = classInfo.getNameOfInterfaces();
		if (interfaceNames.isEmpty())
		{
			return firstLine;
		}

		if (!firstLine)
		{
			writer.append(",");
		}

		firstLine = false;

		languageHelper.newLineIndent();
		writer.append("\"implements\": [");

		Collections.sort(interfaceNames);

		context.incrementIndentationLevel();
		try
		{
			boolean additional = false;
			for (String interfaceName : interfaceNames)
			{
				if (additional)
				{
					writer.append(", ");
				}
				else
				{
					additional = true;
				}
				languageHelper.newLineIndent();
				writer.append('"');
				languageHelper.writeType(interfaceName);
				writer.append('"');
			}
		}
		finally
		{
			context.decrementIndentationLevel();
		}

		languageHelper.newLineIndent();
		writer.append("]");

		return firstLine;
	}

	protected boolean writeRequires(JavaClassInfo classInfo, IWriter writer, boolean firstLine)
	{
		IConversionContext context = this.context.getCurrent();

		IMap<String, String> importsMap = context.getImports();
		if (importsMap == null || importsMap.isEmpty())
		{
			return firstLine;
		}

		String namespace = languageHelper.createNamespace();
		String name = languageHelper.removeGenerics(classInfo.getName());
		String fullName = namespace + "." + name;

		IList<String> requires = new ArrayList<>(importsMap.keySet());
		requires.remove(fullName); // Do not require yourself
		Collections.sort(requires);

		boolean firstRequires = true;
		try
		{
			String convertedSuperClass = languageHelper.convertType(classInfo.getNameOfSuperClass(), false);
			for (String className : requires)
			{
				if (className.equals(convertedSuperClass) || className.equals("Ambeth.Base"))
				{
					// extends already implies the requires
					continue;
				}
				if (firstRequires)
				{
					languageHelper.newLineIndentIfFalse(!firstLine);
					firstLine = languageHelper.newLineIndentWithCommaIfFalse(firstLine);

					writer.append("\"requires\": [");
					context.incrementIndentationLevel();
					languageHelper.newLineIndent();
					firstRequires = false;
				}
				else
				{
					firstRequires = languageHelper.newLineIndentWithCommaIfFalse(firstRequires);
				}
				writer.append('"');
				languageHelper.writeType(className);
				writer.append('"');
			}
		}
		finally
		{
			if (!firstRequires)
			{
				context.decrementIndentationLevel();
			}
		}
		if (!firstRequires)
		{
			languageHelper.newLineIndent();
			writer.append("]");
		}
		return firstLine;
	}

	protected boolean writerStatic(final JavaClassInfo classInfo, final IWriter writer, boolean firstLine)
	{
		IList<Field> staticFields = createView(classInfo.getFields(), Boolean.FALSE, Boolean.TRUE);
		IList<Method> staticMethods = createView(classInfo.getMethods(), null, Boolean.TRUE);
		if (staticFields.isEmpty() && staticMethods.isEmpty())
		{
			return firstLine;
		}

		firstLine = languageHelper.newLineIndentWithCommaIfFalse(firstLine);
		languageHelper.newLineIndent();
		writer.append("\"static\": ");
		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				boolean firstLine = true;
				firstLine = writePublicStaticVars(classInfo, writer, firstLine);
				firstLine = writePublicStaticMethods(classInfo, writer, firstLine);
			}
		});

		return firstLine;
	}

	protected HashMap<String, ArrayList<Method>> findOverloadedMethods(IList<Method> methods, boolean staticOnly)
	{
		HashMap<String, ArrayList<Method>> buckets = new HashMap<>();
		for (Method method : methods)
		{
			if ((method.isPrivate() && method.isStatic()) || method.isStatic() != staticOnly)
			{
				continue;
			}

			String name = !method.isConstructor() ? method.getName() : DefaultMethodTransformer.THIS;
			ArrayList<Method> bucket = buckets.get(name);
			if (bucket == null)
			{
				bucket = new ArrayList<>();
				buckets.put(name, bucket);
			}
			bucket.add(method);
		}

		Iterator<Entry<String, ArrayList<Method>>> iter = buckets.iterator();
		while (iter.hasNext())
		{
			Entry<String, ArrayList<Method>> entry = iter.next();
			ArrayList<Method> bucket = entry.getValue();
			if (bucket.size() == 1)
			{
				iter.remove();
			}
		}

		return buckets;
	}

	protected <T extends BaseJavaClassModel> ArrayList<T> createView(IList<T> elements, Boolean checkPrivate, Boolean checkStatic)
	{
		ArrayList<T> view = new ArrayList<>();

		for (T element : elements)
		{
			if (checkPrivate != null && !checkPrivate.booleanValue() == element.isPrivate())
			{
				continue;
			}
			if (checkStatic != null && !checkStatic.booleanValue() == element.isStatic())
			{
				continue;
			}
			view.add(element);
		}

		return view;
	}

	protected boolean writePublicStaticVars(JavaClassInfo classInfo, IWriter writer, boolean firstLine)
	{
		ArrayList<Field> nonStaticPrivateFields = createView(classInfo.getFields(), Boolean.FALSE, Boolean.TRUE);
		firstLine = writeFieldListWithSnippets(nonStaticPrivateFields, writer, firstLine, true);
		return firstLine;
	}

	protected boolean writePublicStaticMethods(JavaClassInfo classInfo, IWriter writer, boolean firstLine)
	{
		IList<Method> publicStaticMethods = createView(classInfo.getMethods(), Boolean.FALSE, Boolean.TRUE);

		for (Method method : publicStaticMethods)
		{
			if (!method.isStatic())
			{
				continue;
			}

			firstLine = languageHelper.newLineIndentWithCommaIfFalse(firstLine);
			context.setMethod(method);
			try
			{
				methodHandler.handle();
			}
			finally
			{
				context.setMethod(null);
			}
		}
		firstLine = writeOverloadHubMethods(classInfo, true, firstLine);

		return firstLine;
	}

	protected boolean writePrivates(JavaClassInfo classInfo, final IWriter writer, boolean firstLine)
	{
		final IList<Field> privateNonStaticFields = createView(classInfo.getFields(), Boolean.TRUE, Boolean.FALSE);
		if (privateNonStaticFields.isEmpty())
		{
			return firstLine;
		}

		firstLine = languageHelper.newLineIndentWithCommaIfFalse(firstLine);
		languageHelper.newLineIndent();
		writer.append("\"privates\": ");
		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				boolean firstLine = true; // new scope
				writeFieldListWithSnippets(privateNonStaticFields, writer, firstLine, true);
			}
		});

		return firstLine;
	}

	protected boolean writeFields(JavaClassInfo classInfo, IWriter writer, boolean firstLine)
	{
		ArrayList<Field> nonPrivateNonStaticFields = createView(classInfo.getFields(), Boolean.FALSE, Boolean.FALSE);
		firstLine = writeFieldListWithSnippets(nonPrivateNonStaticFields, writer, firstLine, true);
		return firstLine;
	}

	protected boolean writeConfig(JavaClassInfo classInfo, final IWriter writer, boolean firstLine)
	{
		final IList<IVariable> allUsedVariables = classInfo.getAllUsedVariables();
		if (allUsedVariables.isEmpty())
		{
			return firstLine;
		}

		firstLine = languageHelper.newLineIndentWithCommaIfFalse(firstLine);
		languageHelper.newLineIndent();
		writer.append("\"config\": ");
		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				HashSet<String> alreadyHandled = new HashSet<>();
				boolean firstVar = true;
				for (IVariable usedVariable : allUsedVariables)
				{
					String name = usedVariable.getName();
					if (!alreadyHandled.add(name))
					{
						// The IVariable instances have no equals(). So there are duplicates.
						continue;
					}

					FieldInfo field = new FieldInfo();
					field.setPrivateFlag(true);
					field.setFieldType(usedVariable.getType());
					field.setName(name);
					context.setField(field);

					firstVar = languageHelper.writeStringIfFalse(",", firstVar);
					fieldHandler.handle();
				}
			}
		});

		return firstLine;
	}

	protected boolean writeAccessors(boolean firstLine)
	{
		return firstLine;
	}

	protected boolean writeMethods(JavaClassInfo classInfo, IWriter writer, boolean firstLine)
	{
		IList<Method> nonStaticMethods = createView(classInfo.getMethods(), null, Boolean.FALSE);

		for (Method method : nonStaticMethods)
		{
			boolean hasConstructor = method.isConstructor();
			boolean hasOverloads = overloadManagerNonStatic.hasOverloads(method);
			IList<VariableElement> parameters = method.getParameters();
			if (hasConstructor && !hasOverloads && parameters.isEmpty())
			{
				// Do not write the empty default constructor if not needed.
				continue;
			}

			firstLine = languageHelper.newLineIndentWithCommaIfFalse(firstLine);
			context.setMethod(method);
			try
			{
				methodHandler.handle();
			}
			finally
			{
				context.setMethod(null);
			}
		}
		firstLine = writeOverloadHubMethods(classInfo, false, firstLine);

		return firstLine;
	}

	protected boolean writeOverloadHubMethods(JavaClassInfo classInfo, boolean staticOnly, boolean firstLine)
	{
		IConversionContext context = this.context.getCurrent();
		final IWriter writer = context.getWriter();

		IJsOverloadManager overloadManager = staticOnly ? overloadManagerStatic : overloadManagerNonStatic;
		HashMap<String, ArrayList<Method>> overloadedMethods = overloadManager.getOverloadedMethods(classInfo.getFqName());

		Iterator<Entry<String, ArrayList<Method>>> iter = overloadedMethods.iterator();
		while (iter.hasNext())
		{
			Entry<String, ArrayList<Method>> entry = iter.next();
			String methodName = entry.getKey();
			ArrayList<Method> methods = entry.getValue();

			String returnType = findReturnType(methods);

			Method firstMethod = methods.get(0);

			firstLine = languageHelper.newLineIndentWithCommaIfFalse(firstLine);

			// Documentation
			languageHelper.startDocumentation();
			languageHelper.newLineIndentDocumentation();
			writer.append("Hub for overloaded method '").append(methodName).append("()'.");
			languageHelper.newLineIndentDocumentation();
			writer.append("@param {JSON} parameters");
			languageHelper.newLineIndentDocumentation();
			writer.append("@return {");
			languageHelper.writeType(returnType);
			writer.append("}");
			languageHelper.endDocumentation();

			// Signature
			final String signatureMethodName = firstMethod.isConstructor() ? "constructor" : firstMethod.getName();
			languageHelper.newLineIndent();
			writer.append('"').append(signatureMethodName).append("\": function(parameters)");
			languageHelper.preBlockWhiteSpaces();

			// Body
			languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
			{
				@Override
				public void invoke() throws Throwable
				{
					languageHelper.newLineIndent();
					writer.append("return Ambeth.util.OverloadUtil.handle(this, this.m$f_").append(signatureMethodName).append(".overloads, parameters);");
				}
			});

			languageHelper.writeMetadata(signatureMethodName, returnType, methods);
		}

		return firstLine;
	}

	protected boolean writeFieldListWithSnippets(IList<Field> fields, IWriter writer, boolean firstLine, boolean useComma)
	{
		if (fields.isEmpty())
		{
			return firstLine;
		}

		IConversionContext context = this.context.getCurrent();
		JavaClassInfo classInfo = context.getClassInfo();
		ISnippetManager snippetManager = context.getSnippetManager();
		ArrayList<String> untranslatableFieldDefinitions = new ArrayList<>();
		boolean dryRun = context.isDryRun();
		for (Field field : fields)
		{
			if (useComma)
			{
				firstLine = languageHelper.newLineIndentWithCommaIfFalse(firstLine);
			}
			else
			{
				firstLine = languageHelper.newLineIndentIfFalse(firstLine);
			}
			context.setField(field);
			try
			{
				String statementString = astHelper.writeToStash(new IBackgroundWorkerDelegate()
				{
					@Override
					public void invoke() throws Throwable
					{
						fieldHandler.handle();
					}
				});

				// Important to check here to keep the code in order
				checkUntranslatableList(untranslatableFieldDefinitions, snippetManager, dryRun);

				context.getWriter().append(statementString);
			}
			catch (SnippetTrigger snippetTrigger)
			{
				if (!dryRun)
				{
					String topic = snippetTrigger.getMessage();
					String message = "Field initializer: " + ((FieldInfo) field).getInitializer().toString();
					todoWriter.write(topic, message, classInfo, field.getLocationInfo().getStartOffset());
					if (log.isInfoEnabled())
					{
						log.info(topic);
					}
				}
				addToUntranslatableList(untranslatableFieldDefinitions, field, dryRun, context);
			}
		}
		checkUntranslatableList(untranslatableFieldDefinitions, snippetManager, dryRun);

		return firstLine;
	}

	protected void addToUntranslatableList(ArrayList<String> untranslatableFieldDefinitions, Field field, boolean dryRun, IConversionContext context)
	{
		if (dryRun)
		{
			return;
		}
		if (log.isInfoEnabled())
		{
			log.info(context.getClassInfo().getFqName() + ": unhandled - FIELD: " + field.toString());
		}

		String untranslatableFieldDefinition = field.toString();
		untranslatableFieldDefinitions.add(untranslatableFieldDefinition);
	}

	protected void checkUntranslatableList(ArrayList<String> untranslatableStatements, ISnippetManager snippetManager, boolean dryRun)
	{
		if (dryRun || untranslatableStatements.isEmpty())
		{
			return;
		}

		snippetManager.writeSnippet(untranslatableStatements);
		untranslatableStatements.clear();
	}

	protected String findReturnType(ArrayList<Method> methods)
	{
		HashSet<String> returnTypes = new HashSet<>();
		for (Method method : methods)
		{
			String returnType = method.getReturnType();
			returnTypes.add(returnType);
		}
		if (returnTypes.size() == 1)
		{
			return returnTypes.iterator(false).next();
		}
		returnTypes.remove("void");
		if (returnTypes.size() == 1)
		{
			return returnTypes.iterator(false).next();
		}
		return "java.lang.Object";
	}

	protected void writeCreateFunction(final ArrayList<Field> fieldsToInit, final JavaClassInfo classInfo, IWriter writer)
	{
		if (fieldsToInit.isEmpty())
		{
			return;
		}

		final boolean enumType = classInfo.isEnum();

		writer.append("function() ");
		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				final IConversionContext context = JsClassHandler.this.context.getCurrent();
				ISnippetManager snippetManager = context.getSnippetManager();
				ArrayList<String> untranslatableFieldDefinitions = new ArrayList<>();
				boolean dryRun = context.isDryRun();
				for (final Field field : fieldsToInit)
				{
					final ExpressionTree initializer = ((FieldInfo) field).getInitializer();
					if (initializer == null)
					{
						continue;
					}
					try
					{
						String statementString = astHelper.writeToStash(new IBackgroundWorkerDelegate()
						{
							@Override
							public void invoke() throws Throwable
							{
								IWriter writer = context.getWriter();
								languageHelper.newLineIndent();
								writer.append("this.").append(field.getName()).append(" = ");
								if (enumType && initializer instanceof JCNewClass)
								{
									writeEnumConstruction((JCNewClass) initializer, field.getName(), writer);
								}
								else
								{
									languageHelper.writeExpressionTree(initializer);
								}
								writer.append(";");
							}
						});

						// Important to check here to keep the code in order
						checkUntranslatableList(untranslatableFieldDefinitions, snippetManager, dryRun);

						context.getWriter().append(statementString);
					}
					catch (SnippetTrigger snippetTrigger)
					{
						String topic = snippetTrigger.getMessage();
						String message = "Field initializer: " + ((FieldInfo) field).getInitializer().toString();
						todoWriter.write(topic, message, classInfo, field.getLocationInfo().getStartOffset());
						if (log.isInfoEnabled() && !dryRun)
						{
							log.info(topic);
						}
						addToUntranslatableList(untranslatableFieldDefinitions, field, dryRun, context);
					}
				}
				checkUntranslatableList(untranslatableFieldDefinitions, snippetManager, dryRun);
			}
		});
	}

	protected void writeEnumConstruction(JCNewClass newClass, String name, IWriter writer)
	{
		String owner = newClass.clazz.toString();
		writer.append("Ext.create('");
		languageHelper.writeType(owner);
		writer.append("', { 'name_' : \"").append(name).append("\" })");
	}

	protected void writeAnonymousClassBody(JavaClassInfo classInfo)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		final IList<IVariable> allUsedVariables = classInfo.getAllUsedVariables();

		for (Field field : classInfo.getFields())
		{
			languageHelper.newLineIndent();
			context.setField(field);
			fieldHandler.handle();
		}
		for (IVariable usedVariable : allUsedVariables)
		{
			languageHelper.newLineIndent();
			writer.append("private ");
			languageHelper.writeType(usedVariable.getType());
			writer.append(' ');
			writer.append(usedVariable.getName());
			writer.append(';');
		}
		languageHelper.newLineIndent();
		languageHelper.newLineIndent();
		writer.append("public ");
		writer.append(classInfo.getName());
		writer.append('(');
		boolean firstVariable = true;
		for (IVariable usedVariable : allUsedVariables)
		{
			firstVariable = languageHelper.writeStringIfFalse(", ", firstVariable);
			languageHelper.writeType(usedVariable.getType());
			writer.append(' ');
			writer.append(usedVariable.getName());
		}
		writer.append(')');
		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				IConversionContext context = JsClassHandler.this.context.getCurrent();
				IWriter writer = context.getWriter();
				for (IVariable usedVariable : allUsedVariables)
				{
					languageHelper.newLineIndent();
					writer.append("this.");
					writer.append(usedVariable.getName());
					writer.append(" = ");
					writer.append(usedVariable.getName());
					writer.append(";");
				}
			}
		});
		for (Method method : classInfo.getMethods())
		{
			if (method.isConstructor())
			{
				// skip the constructors defined in anonymous classes. we already created our single necessary constructor explicitly
				continue;
			}
			languageHelper.newLineIndent();
			context.setMethod(method);
			try
			{
				methodHandler.handle();
			}
			finally
			{
				context.setMethod(null);
			}
		}
	}

	protected void writeClassBody(JavaClassInfo classInfo)
	{
		IConversionContext context = this.context.getCurrent();

		boolean firstLine = true;
		for (Field field : classInfo.getFields())
		{
			firstLine = languageHelper.newLineIndentIfFalse(firstLine);
			context.setField(field);
			fieldHandler.handle();
		}

		for (Method method : classInfo.getMethods())
		{
			firstLine = languageHelper.newLineIndentIfFalse(firstLine);
			context.setMethod(method);
			context.setMethod(method);
			try
			{
				methodHandler.handle();
			}
			finally
			{
				context.setMethod(null);
			}
		}
	}
}
