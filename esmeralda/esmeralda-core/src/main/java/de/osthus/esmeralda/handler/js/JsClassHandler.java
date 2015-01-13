package de.osthus.esmeralda.handler.js;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.lang.model.element.VariableElement;

import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree.JCLiteral;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.IToDoWriter;
import de.osthus.esmeralda.handler.IVariable;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.BaseJavaClassModel;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.FieldInfo;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class JsClassHandler implements IJsClassHandler
{
	@SuppressWarnings("rawtypes")
	private static final ArrayList EMPTY_ARRAY_LIST = new ArrayList();

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IJsClasspathManager jsClasspathManager;

	@Autowired
	protected IJsHelper languageHelper;

	@Autowired
	protected IJsFieldHandler fieldHandler;

	@Autowired
	protected IJsMethodHandler methodHandler;

	@Autowired
	protected IToDoWriter toDoWriter;

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
		String fullClassName = namespace + "." + classInfo.getName().split("<", 2)[0];
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
				writeCreateFunction(fieldsToInit, writer);
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

	protected HashMap<String, ArrayList<Method>> findOverloadedMethods(IList<Method> methods)
	{
		HashMap<String, ArrayList<Method>> buckets = new HashMap<>();
		for (Method method : methods)
		{
			if (method.isConstructor())
			{
				continue;
			}

			String name = method.getName();
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

	protected void writeName(JavaClassInfo classInfo)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		String namespace = languageHelper.createNamespace();

		writer.append("'");
		if (!namespace.isEmpty())
		{
			writer.append(namespace).append(".");
		}
		languageHelper.writeSimpleName(classInfo);
		writer.append("'");
	}

	protected void writeData(final JavaClassInfo classInfo)
	{
		IConversionContext context = this.context.getCurrent();
		final IWriter writer = context.getWriter();

		writer.append("function(");
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

		// languageHelper.writeAnnotations(classInfo);
		// languageHelper.newLineIntend();
		// boolean firstModifier = languageHelper.writeModifiers(classInfo);
		// if (!classInfo.isPrivate() && !classInfo.isProtected() && !classInfo.isPublic())
		// {
		// // no visibility defined. so we default to "public"
		// firstModifier = languageHelper.writeStringIfFalse(" ", firstModifier);
		// writer.append("public");
		// }
		// if (classInfo.isEnum())
		// {
		// // an enum in java can never be inherited from - we convert this as a sealed class
		// firstModifier = languageHelper.writeStringIfFalse(" ", firstModifier);
		// writer.append("sealed");
		// }
		// if (!classInfo.isInterface())
		// {
		// firstModifier = languageHelper.writeStringIfFalse(" ", firstModifier);
		// writer.append("class");
		// }
		// else
		// {
		// firstModifier = languageHelper.writeStringIfFalse(" ", firstModifier);
		// writer.append("interface");
		// }
		// firstModifier = languageHelper.writeStringIfFalse(" ", firstModifier);
		// writer.append(classInfo.getName());
		//
		// boolean firstInterfaceName = true;
		// String nameOfSuperClass = classInfo.getNameOfSuperClass();
		// if (nameOfSuperClass != null && nameOfSuperClass.length() > 0 && !Object.class.getName().equals(nameOfSuperClass) &&
		// !"<none>".equals(nameOfSuperClass))
		// {
		// writer.append(" : ");
		// languageHelper.writeType(nameOfSuperClass);
		// firstInterfaceName = false;
		// }
		// for (String nameOfInterface : classInfo.getNameOfInterfaces())
		// {
		// if (firstInterfaceName)
		// {
		// writer.append(" : ");
		// firstInterfaceName = false;
		// }
		// else
		// {
		// writer.append(", ");
		// }
		// languageHelper.writeType(nameOfInterface);
		// }
		//
		// languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		// {
		// @Override
		// public void invoke() throws Throwable
		// {
		// IConversionContext context = JsClassHandler.this.context.getCurrent();
		// if (classInfo.isAnonymous())
		// {
		// writeAnonymousClassBody(classInfo);
		// }
		// else
		// {
		// writeClassBody(classInfo);
		// }
		// for (IPostProcess postProcess : context.getPostProcesses())
		// {
		// postProcess.postProcess();
		// }
		// }
		// });
	}

	protected boolean writePrivateStaticVars(JavaClassInfo classInfo, IWriter writer, boolean firstLine)
	{
		IList<Field> privateStaticFields = createView(classInfo.getFields(), Boolean.TRUE, Boolean.TRUE);
		for (Field field : privateStaticFields)
		{
			firstLine = languageHelper.newLineIndentIfFalse(firstLine);
			context.setField(field);
			fieldHandler.handle();
		}

		return firstLine;
	}

	protected boolean writeExtend(JavaClassInfo classInfo, IWriter writer, boolean firstLine)
	{
		String nameOfSuperClass = classInfo.getNameOfSuperClass();
		if (nameOfSuperClass == null || nameOfSuperClass.isEmpty() || Object.class.getName().equals(nameOfSuperClass) || "<none>".equals(nameOfSuperClass))
		{
			nameOfSuperClass = "Ambeth.Base";
		}

		firstLine = languageHelper.newLineIndentWithCommaIfFalse(firstLine);
		languageHelper.newLineIndent();
		writer.append("extend: '");
		languageHelper.writeType(nameOfSuperClass);
		writer.append("'");

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
		writer.append("implements: [");

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
				writer.append("'");
				languageHelper.writeType(interfaceName);
				writer.append("'");
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
		String name = classInfo.getName().replaceAll("<.*>", "");
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

					writer.append("requires: [");
					context.incrementIndentationLevel();
					languageHelper.newLineIndent();
					firstRequires = false;
				}
				else
				{
					firstRequires = languageHelper.newLineIndentWithCommaIfFalse(firstRequires);
				}
				writer.append("'").append(className).append("'");
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
		writer.append("static: ");
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
		if (nonStaticPrivateFields.isEmpty())
		{
			return firstLine;
		}

		for (Field field : nonStaticPrivateFields)
		{
			firstLine = languageHelper.newLineIndentWithCommaIfFalse(firstLine);
			context.setField(field);
			fieldHandler.handle();
		}

		return firstLine;
	}

	protected boolean writePublicStaticMethods(JavaClassInfo classInfo, IWriter writer, boolean firstLine)
	{
		JsSpecific languageSpecific = languageHelper.getLanguageSpecific();

		IList<Method> publicStaticMethods = createView(classInfo.getMethods(), Boolean.FALSE, Boolean.TRUE);

		HashMap<String, ArrayList<Method>> overloadedMethods = findOverloadedMethods(publicStaticMethods);
		languageSpecific.setOverloadedMethods(overloadedMethods);
		try
		{
			for (Method method : publicStaticMethods)
			{
				if (!method.isStatic())
				{
					continue;
				}

				firstLine = languageHelper.newLineIndentWithCommaIfFalse(firstLine);
				context.setMethod(method);
				methodHandler.handle();
			}
		}
		finally
		{
			languageSpecific.setOverloadedMethods(null);
		}
		firstLine = writeOverloadHubMethods(classInfo, overloadedMethods, firstLine);

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
		writer.append("privates: ");
		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				boolean firstLine = true;
				for (Field field : privateNonStaticFields)
				{
					firstLine = languageHelper.newLineIndentIfFalse(firstLine);
					context.setField(field);
					fieldHandler.handle();
				}
			}
		});

		return firstLine;
	}

	protected boolean writeFields(JavaClassInfo classInfo, IWriter writer, boolean firstLine)
	{
		ArrayList<Field> nonPrivateNonStaticFields = createView(classInfo.getFields(), Boolean.FALSE, Boolean.FALSE);
		if (nonPrivateNonStaticFields.isEmpty())
		{
			return firstLine;
		}

		for (Field field : nonPrivateNonStaticFields)
		{
			firstLine = languageHelper.newLineIndentWithCommaIfFalse(firstLine);
			context.setField(field);
			fieldHandler.handle();
		}

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
		writer.append("config: ");
		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				for (IVariable usedVariable : allUsedVariables)
				{
					FieldInfo field = new FieldInfo();
					field.setPrivateFlag(true);
					field.setFieldType(usedVariable.getType());
					field.setName(usedVariable.getName());

					context.setField(field);
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
		JsSpecific languageSpecific = languageHelper.getLanguageSpecific();

		IList<Method> nonStaticMethods = createView(classInfo.getMethods(), null, Boolean.FALSE);

		HashMap<String, ArrayList<Method>> overloadedMethods = findOverloadedMethods(nonStaticMethods);
		languageSpecific.setOverloadedMethods(overloadedMethods);
		try
		{
			for (Method method : nonStaticMethods)
			{
				if (method.isConstructor())
				{
					continue;
				}

				firstLine = languageHelper.newLineIndentWithCommaIfFalse(firstLine);
				context.setMethod(method);
				methodHandler.handle();
			}
		}
		finally
		{
			languageSpecific.setOverloadedMethods(null);
		}
		firstLine = writeOverloadHubMethods(classInfo, overloadedMethods, firstLine);

		return firstLine;
	}

	protected boolean writeOverloadHubMethods(JavaClassInfo classInfo, HashMap<String, ArrayList<Method>> overloadedMethods, boolean firstLine)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		Iterator<Entry<String, ArrayList<Method>>> iter = overloadedMethods.iterator();
		while (iter.hasNext())
		{
			Entry<String, ArrayList<Method>> entry = iter.next();
			String methodName = entry.getKey();
			ArrayList<Method> methods = entry.getValue();

			final ArrayList<Method>[] methodBuckets = bucketSortMethods(methods);

			firstLine = languageHelper.newLineIndentWithCommaIfFalse(firstLine);

			// Documentation
			languageHelper.startDocumentation();
			languageHelper.newLineIndentDocumentation();
			writer.append("Hub for overloaded method '").append(methodName).append("()'.");
			languageHelper.newLineIndentDocumentation();
			writer.append("@param {JSON} parameters");
			String returnType = methods.get(0).getReturnType();
			if (!"void".equals(returnType))
			{
				String convertedType = languageHelper.convertType(returnType, false);
				languageHelper.newLineIndentDocumentation();
				writer.append("@return {").append(convertedType).append("}");
			}
			languageHelper.endDocumentation();

			// Signature
			languageHelper.newLineIndent();
			writer.append(methodName).append(": function(parameters)");
			languageHelper.preBlockWhiteSpaces();

			// Body
			writeOverloadHubMethodBody(methodBuckets);
		}

		return firstLine;
	}

	protected void writeOverloadHubMethodBody(final ArrayList<Method>[] methodBuckets)
	{
		final IConversionContext context = this.context.getCurrent();
		final IWriter writer = context.getWriter();

		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				boolean ambiguousParameterNames = false;

				languageHelper.newLineIndent();
				writer.append("var methods = [");
				boolean firstBucket = true;
				context.incrementIndentationLevel();
				for (int i = 0, length = methodBuckets.length; i < length; i++)
				{
					final ArrayList<Method> bucket = methodBuckets[i];
					if (bucket == null)
					{
						firstBucket = languageHelper.writeStringIfFalse(",", firstBucket);
						languageHelper.newLineIndent();
						writer.append("null");
					}
					else
					{
						HashMap<String, Method> paramNamesMaps = new HashMap<>();

						boolean singleMethod = bucket.size() == 1;
						firstBucket = languageHelper.writeStringIfFalse(",", firstBucket);
						languageHelper.newLineIndent();
						writer.append("[");
						boolean firstMethod = true;
						context.incrementIndentationLevel();
						for (Method method : bucket)
						{
							String[] paramNames = new String[i];

							String methodNamePostfix = languageHelper.createOverloadedMethodNamePostfix(method.getParameters());
							firstMethod = languageHelper.writeStringIfFalse(",", firstMethod);
							languageHelper.newLineIndentIfFalse(singleMethod);
							writer.append("{ 'method': this.").append(method.getName()).append(methodNamePostfix);
							if (!singleMethod)
							{
								writer.append(", 'paramNames': [");
								IList<VariableElement> parameters = method.getParameters();
								boolean firstParam = true;
								for (int j = 0, jLength = parameters.size(); j < jLength; j++)
								{
									VariableElement param = parameters.get(j);
									firstParam = languageHelper.writeStringIfFalse(", ", firstParam);
									VarSymbol var = (VarSymbol) param;
									String paramName = var.name.toString();
									writer.append('"').append(paramName).append('"');
									paramNames[j] = paramName;
								}
								writer.append(']');

								if (!ambiguousParameterNames)
								{
									Arrays.sort(paramNames);
									Method existing = paramNamesMaps.put(Arrays.deepToString(paramNames), method);
									if (existing != null)
									{
										ambiguousParameterNames = true;
										StringBuilder sb = new StringBuilder();
										sb.append("in ").append(method.getOwningClass().getFqName()).append(" on method ").append(existing.getName())
												.append("()");
										toDoWriter.write("Ambiguous parameter names", sb.toString());
									}
								}
							}
							writer.append(" }");
						}
						context.decrementIndentationLevel();
						languageHelper.newLineIndentIfFalse(singleMethod);
						writer.append(']');
					}
				}
				context.decrementIndentationLevel();
				languageHelper.newLineIndent();
				writer.append("];");
				languageHelper.newLineIndent();
				writer.append("Ambeth.util.OverloadUtil.handle(this, methods, parameters);");
			}
		});
	}

	protected ArrayList<Method>[] bucketSortMethods(ArrayList<Method> methods)
	{
		int maxParams = 0;
		for (Method method : methods)
		{
			int size = method.getParameters().size();
			if (size > maxParams)
			{
				maxParams = size;
			}
		}

		@SuppressWarnings("unchecked")
		ArrayList<Method>[] methodBuckets = new ArrayList[maxParams + 1];
		for (Method method : methods)
		{
			int size = method.getParameters().size();
			ArrayList<Method> bucket = methodBuckets[size];
			if (bucket == null)
			{
				bucket = new ArrayList<Method>();
				methodBuckets[size] = bucket;
			}
			bucket.add(method);
		}

		return methodBuckets;
	}

	protected void writeCreateFunction(final ArrayList<Field> fieldsToInit, final IWriter writer)
	{
		writer.append("function() ");
		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				for (Field field : fieldsToInit)
				{
					ExpressionTree initializer = ((FieldInfo) field).getInitializer();
					if (initializer != null)
					{
						languageHelper.newLineIndent();
						writer.append("this.").append(field.getName()).append(" = ");
						languageHelper.writeExpressionTree(initializer);
						writer.append(";");
					}
				}
			}
		});
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
			methodHandler.handle();
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
			methodHandler.handle();
		}
	}
}
