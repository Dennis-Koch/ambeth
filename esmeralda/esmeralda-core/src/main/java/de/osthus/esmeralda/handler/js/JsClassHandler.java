package de.osthus.esmeralda.handler.js;

import java.util.Collections;
import java.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.IPostProcess;
import de.osthus.esmeralda.TypeUsing;
import de.osthus.esmeralda.handler.IVariable;
import de.osthus.esmeralda.handler.csharp.ICsFieldHandler;
import de.osthus.esmeralda.handler.csharp.ICsMethodHandler;
import de.osthus.esmeralda.misc.IEsmeFileUtil;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.Field;
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
	protected ICsFieldHandler fieldHandler;

	@Autowired
	protected ICsMethodHandler methodHandler;

	@Override
	public void handle()
	{
		IConversionContext context = this.context.getCurrent();
		final JavaClassInfo classInfo = context.getClassInfo();

		context.setIndentationLevel(0);
		try
		{
			writeNamespace(classInfo);

			// languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
			// {
			// @Override
			// public void invoke() throws Throwable
			// {
			// writeClass(classInfo);
			// }
			// });
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

	@Override
	public IJsHelper getLanguageHelper()
	{
		return languageHelper;
	}

	protected void writeNamespace(JavaClassInfo classInfo)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		boolean firstLine = true;
		List<TypeUsing> usings = context.getUsings();
		if (usings != null && usings.size() > 0)
		{
			Collections.sort(usings);
			boolean silverlightFlagActive = false;
			for (TypeUsing using : usings)
			{
				firstLine = languageHelper.newLineIntendIfFalse(firstLine);
				if (silverlightFlagActive && !using.isInSilverlightOnly())
				{
					// deactivate flag
					writer.append("#endif");
					languageHelper.newLineIntend();
					silverlightFlagActive = false;
				}
				else if (!silverlightFlagActive && using.isInSilverlightOnly())
				{
					// activate flag
					languageHelper.newLineIntend();
					writer.append("#if SILVERLIGHT");
					silverlightFlagActive = true;
				}
				writer.append("using ").append(using.getTypeName()).append(';');
			}
			if (silverlightFlagActive)
			{
				// deactivate flag
				languageHelper.newLineIntend();
				writer.append("#endif");

			}
			languageHelper.newLineIntend();
		}

		String packageName = classInfo.getPackageName();
		String nameSpace = languageHelper.toNamespace(packageName);
		firstLine = languageHelper.newLineIntendIfFalse(firstLine);
		writer.append("namespace ").append(nameSpace);
	}

	protected void writeClass(final JavaClassInfo classInfo)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		languageHelper.writeAnnotations(classInfo);
		languageHelper.newLineIntend();
		boolean firstModifier = languageHelper.writeModifiers(classInfo);
		if (!classInfo.isPrivate() && !classInfo.isProtected() && !classInfo.isPublic())
		{
			// no visibility defined. so we default to "public"
			firstModifier = languageHelper.writeStringIfFalse(" ", firstModifier);
			writer.append("public");
		}
		if (classInfo.isEnum())
		{
			// an enum in java can never be inherited from - we convert this as a sealed class
			firstModifier = languageHelper.writeStringIfFalse(" ", firstModifier);
			writer.append("sealed");
		}
		if (!classInfo.isInterface())
		{
			firstModifier = languageHelper.writeStringIfFalse(" ", firstModifier);
			writer.append("class");
		}
		else
		{
			firstModifier = languageHelper.writeStringIfFalse(" ", firstModifier);
			writer.append("interface");
		}
		firstModifier = languageHelper.writeStringIfFalse(" ", firstModifier);
		writer.append(classInfo.getName());

		boolean firstInterfaceName = true;
		String nameOfSuperClass = classInfo.getNameOfSuperClass();
		if (nameOfSuperClass != null && nameOfSuperClass.length() > 0 && !Object.class.getName().equals(nameOfSuperClass) && !"<none>".equals(nameOfSuperClass))
		{
			writer.append(" : ");
			languageHelper.writeType(nameOfSuperClass);
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
			languageHelper.writeType(nameOfInterface);
		}

		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				IConversionContext context = JsClassHandler.this.context.getCurrent();
				if (classInfo.isAnonymous())
				{
					writeAnonymousClassBody(classInfo);
				}
				else
				{
					writeClassBody(classInfo);
				}
				for (IPostProcess postProcess : context.getPostProcesses())
				{
					postProcess.postProcess();
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
			languageHelper.newLineIntend();
			writer.append("private ");
			languageHelper.writeType(usedVariable.getType());
			writer.append(' ');
			writer.append(usedVariable.getName());
			writer.append(';');
		}
		languageHelper.newLineIntend();
		languageHelper.newLineIntend();
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
					languageHelper.newLineIntend();
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
			firstLine = languageHelper.newLineIntendIfFalse(firstLine);
			context.setField(field);
			fieldHandler.handle();
		}

		for (Method method : classInfo.getMethods())
		{
			firstLine = languageHelper.newLineIntendIfFalse(firstLine);
			context.setMethod(method);
			methodHandler.handle();
		}
	}
}
