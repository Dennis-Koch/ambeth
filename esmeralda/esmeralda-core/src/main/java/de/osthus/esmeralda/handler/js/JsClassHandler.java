package de.osthus.esmeralda.handler.js;

import java.util.Collections;

import com.sun.source.tree.ExpressionTree;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IVariable;
import de.osthus.esmeralda.misc.IEsmeFileUtil;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.BaseJavaClassModel;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.FieldInfo;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class JsClassHandler implements IJsClassHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IEsmeFileUtil fileUtil;

	@Autowired
	protected IJsHelper languageHelper;

	@Autowired
	protected IJsFieldHandler fieldHandler;

	@Autowired
	protected IJsMethodHandler methodHandler;

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

		ArrayList<Field> fieldsToInit = createInitOps(classInfo);

		context.setIndentationLevel(0);
		try
		{
			writer.append("Ext.define(");
			writeName(classInfo);
			writer.append(", ");
			writeData(classInfo);
			if (fieldsToInit != null && !fieldsToInit.isEmpty())
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
			return null;
		}

		ArrayList<Field> fieldsToInit = new ArrayList<>();
		for (Field field : fields)
		{
			ExpressionTree initializer = ((FieldInfo) field).getInitializer();
			if (initializer != null)
			{
				fieldsToInit.add(field);
			}
		}

		return fieldsToInit;
	}

	protected void writeName(JavaClassInfo classInfo)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		String className = classInfo.getName();
		String namespace = languageHelper.createNamespace();

		writer.append("'");
		if (!namespace.isEmpty())
		{
			writer.append(namespace).append(".");
		}
		writer.append(className).append("'");
	}

	protected void writeData(final JavaClassInfo classInfo)
	{
		IConversionContext context = this.context.getCurrent();
		final IWriter writer = context.getWriter();

		String className = classInfo.getName();

		writer.append("function(").append(className).append(") ");

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
						firstLine = writerAccessors(firstLine);

						firstLine = writerMethods(classInfo, writer, firstLine);
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
			return firstLine;
		}

		firstLine = languageHelper.newLineIntendWithCommaIfFalse(firstLine);
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

		context.incremetIndentationLevel();
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
			context.decremetIndentationLevel();
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

		IList<String> requires = new ArrayList<>(importsMap.keySet());
		Collections.sort(requires);

		firstLine = languageHelper.newLineIntendWithCommaIfFalse(firstLine);
		languageHelper.newLineIndent();
		writer.append("requires: [");

		context.incremetIndentationLevel();
		try
		{
			boolean additional = false;
			for (String className : requires)
			{
				if (additional)
				{
					writer.append(",");
				}
				else
				{
					additional = true;
				}
				languageHelper.newLineIndent();
				writer.append("'").append(className).append("'");
			}
		}
		finally
		{
			context.decremetIndentationLevel();
		}

		languageHelper.newLineIndent();
		writer.append("]");

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

		firstLine = languageHelper.newLineIntendWithCommaIfFalse(firstLine);
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
			if (checkPrivate != null && !checkPrivate.equals(element.isPrivate()))
			{
				continue;
			}
			if (checkStatic != null && !checkStatic.equals(element.isStatic()))
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
			firstLine = languageHelper.newLineIntendWithCommaIfFalse(firstLine);
			context.setField(field);
			fieldHandler.handle();
		}

		return firstLine;
	}

	protected boolean writePublicStaticMethods(JavaClassInfo classInfo, IWriter writer, boolean firstLine)
	{
		IList<Method> methods = classInfo.getMethods();
		for (Method method : methods)
		{
			if (!method.isStatic())
			{
				continue;
			}

			firstLine = languageHelper.newLineIntendWithCommaIfFalse(firstLine);
			context.setMethod(method);
			methodHandler.handle();
		}

		return firstLine;
	}

	protected boolean writePrivates(JavaClassInfo classInfo, final IWriter writer, boolean firstLine)
	{
		final IList<Field> privateNonStaticFields = createView(classInfo.getFields(), Boolean.TRUE, Boolean.FALSE);
		if (privateNonStaticFields.isEmpty())
		{
			return firstLine;
		}

		firstLine = languageHelper.newLineIntendWithCommaIfFalse(firstLine);
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
		nonPrivateNonStaticFields = createView(nonPrivateNonStaticFields, Boolean.FALSE, null);
		if (nonPrivateNonStaticFields.isEmpty())
		{
			return firstLine;
		}

		for (Field field : nonPrivateNonStaticFields)
		{
			firstLine = languageHelper.newLineIntendWithCommaIfFalse(firstLine);
			context.setField(field);
			fieldHandler.handle();
		}

		return firstLine;
	}

	protected boolean writeConfig(JavaClassInfo classInfo, final IWriter writer, boolean firstLine)
	{
		firstLine = languageHelper.newLineIntendWithCommaIfFalse(firstLine);
		languageHelper.newLineIndent();
		writer.append("config: ");
		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				languageHelper.newLineIndent();
				writer.append("// ...");
			}
		});

		return firstLine;
	}

	protected boolean writerAccessors(boolean firstLine)
	{
		return firstLine;
	}

	protected boolean writerMethods(JavaClassInfo classInfo, IWriter writer, boolean firstLine)
	{
		IList<Method> nonStaticMethods = createView(classInfo.getMethods(), null, Boolean.FALSE);
		for (Method method : nonStaticMethods)
		{
			if (method.isConstructor())
			{
				continue;
			}

			firstLine = languageHelper.newLineIntendWithCommaIfFalse(firstLine);
			context.setMethod(method);
			methodHandler.handle();
		}

		return firstLine;
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
						// TODO replace
						writer.append(initializer.toString());
						// languageHelper.writeExpressionTree(initializer);
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
		writeClassBody(classInfo);
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
