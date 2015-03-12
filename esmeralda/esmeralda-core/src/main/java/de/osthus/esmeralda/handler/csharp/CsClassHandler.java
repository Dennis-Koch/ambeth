package de.osthus.esmeralda.handler.csharp;

import java.util.Collections;
import java.util.List;

import com.sun.source.tree.StatementTree;
import com.sun.source.tree.TypeParameterTree;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.IClassInfoManager;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.IPostProcess;
import de.osthus.esmeralda.TypeUsing;
import de.osthus.esmeralda.handler.IClassHandler;
import de.osthus.esmeralda.handler.IFieldHandler;
import de.osthus.esmeralda.handler.IMethodHandler;
import de.osthus.esmeralda.handler.IUsedVariableDelegate;
import de.osthus.esmeralda.handler.IVariable;
import de.osthus.esmeralda.misc.IEsmeFileUtil;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class CsClassHandler implements IClassHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IClassInfoManager classInfoManager;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IEsmeFileUtil fileUtil;

	@Autowired
	protected ICsHelper languageHelper;

	@Autowired("csFieldHandler")
	protected IFieldHandler fieldHandler;

	@Autowired("csMethodHandler")
	protected IMethodHandler methodHandler;

	@Override
	public void handle()
	{
		IConversionContext context = this.context.getCurrent();
		final JavaClassInfo classInfo = context.getClassInfo();

		context.getPostProcesses().clear();

		context.setIndentationLevel(0);
		try
		{
			writeNamespace(classInfo);

			languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
			{
				@Override
				public void invoke() throws Throwable
				{
					writeClass(classInfo);
				}
			});
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
	public ICsHelper getLanguageHelper()
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
				firstLine = languageHelper.newLineIndentIfFalse(firstLine);
				if (silverlightFlagActive && !using.isInSilverlightOnly())
				{
					// deactivate flag
					writer.append("#endif");
					languageHelper.newLineIndent();
					silverlightFlagActive = false;
				}
				else if (!silverlightFlagActive && using.isInSilverlightOnly())
				{
					// activate flag
					languageHelper.newLineIndent();
					writer.append("#if SILVERLIGHT");
					silverlightFlagActive = true;
				}
				writer.append("using ").append(using.getTypeName()).append(';');
			}
			if (silverlightFlagActive)
			{
				// deactivate flag
				languageHelper.newLineIndent();
				writer.append("#endif");

			}
			languageHelper.newLineIndent();
		}

		String nameSpace = languageHelper.createNamespace();
		firstLine = languageHelper.newLineIndentIfFalse(firstLine);
		writer.append("namespace ").append(nameSpace);
	}

	protected void writeClass(final JavaClassInfo classInfo)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		languageHelper.writeAnnotations(classInfo);
		languageHelper.newLineIndent();
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
		languageHelper.writeSimpleName(classInfo);

		boolean firstInterfaceName = true;
		String nameOfSuperClass = classInfo.getNameOfSuperClass();
		if (nameOfSuperClass != null && !nameOfSuperClass.isEmpty() && !Object.class.getName().equals(nameOfSuperClass) && !"<none>".equals(nameOfSuperClass))
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
		boolean firstTypeArgument = true;
		for (TypeParameterTree typeParameter : classInfo.getClassTree().getTypeParameters())
		{
			JavaClassInfo typeParameterCI = classInfoManager.resolveClassInfo(typeParameter.getName().toString());
			JavaClassInfo extendsFrom = typeParameterCI.getExtendsFrom();
			if (Object.class.getName().equals(extendsFrom.getFqName()))
			{
				continue;
			}
			if (firstTypeArgument)
			{
				writer.append(" where ");
			}
			firstTypeArgument = languageHelper.writeStringIfFalse(",", firstTypeArgument);
			writer.append(typeParameter.getName());
			writer.append(" : ");
			languageHelper.writeType(extendsFrom.getFqName());
		}

		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				IConversionContext context = CsClassHandler.this.context.getCurrent();
				if (!classInfo.isAnonymous())
				{
					writeClassBody(classInfo);
				}
				else
				{
					writeAnonymousClassBody(classInfo);
				}

				postProcessing(context);
			}

			protected void postProcessing(IConversionContext context)
			{
				final IList<IPostProcess> postProcesses = context.getPostProcesses();
				if (postProcesses.isEmpty())
				{
					return;
				}
				if (!classInfo.isAnnotation())
				{
					throw new UnsupportedOperationException("PostProcessing only implemented for Annotations");
				}

				ILanguageHelper languageHelper = context.getLanguageHelper();
				IWriter writer = context.getWriter();

				String className = classInfo.getName();
				String methodName = languageHelper.createMethodName(className);

				languageHelper.newLineIndent();
				languageHelper.newLineIndent();
				writer.append("public ").append(methodName).append("()");
				languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
				{
					@Override
					public void invoke() throws Throwable
					{
						for (IPostProcess postProcess : postProcesses)
						{
							postProcess.postProcess();
						}
					}
				});
			}
		});
	}

	protected void writeAnonymousClassBody(JavaClassInfo classInfo)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		for (Field field : classInfo.getFields())
		{
			languageHelper.newLineIndent();
			context.setField(field);
			fieldHandler.handle();
		}

		languageHelper.forAllUsedVariables(new IUsedVariableDelegate()
		{
			@Override
			public void invoke(IVariable usedVariable, boolean firstVariable, IConversionContext context, ILanguageHelper languageHelper, IWriter writer)
			{
				languageHelper.newLineIndent();
				writer.append("private ");
				languageHelper.writeType(usedVariable.getType());
				writer.append(' ');
				writer.append(usedVariable.getName());
				writer.append(';');
			}
		});

		languageHelper.newLineIndent();
		languageHelper.newLineIndent();
		writer.append("public ");
		languageHelper.writeSimpleNonGenericName(classInfo);
		writer.append('(');
		languageHelper.forAllUsedVariables(new IUsedVariableDelegate()
		{
			@Override
			public void invoke(IVariable usedVariable, boolean firstVariable, IConversionContext context, ILanguageHelper languageHelper, IWriter writer)
			{
				firstVariable = languageHelper.writeStringIfFalse(", ", firstVariable);
				languageHelper.writeType(usedVariable.getType());
				writer.append(' ');
				writer.append(usedVariable.getName());
			}
		});
		writer.append(')');
		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				languageHelper.forAllUsedVariables(new IUsedVariableDelegate()
				{
					@Override
					public void invoke(IVariable usedVariable, boolean firstVariable, IConversionContext context, ILanguageHelper languageHelper, IWriter writer)
					{
						languageHelper.newLineIndent();
						writer.append("this.");
						writer.append(usedVariable.getName());
						writer.append(" = ");
						writer.append(usedVariable.getName());
						writer.append(";");
					}
				});
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
		IList<Method> constructors = classInfo.getConstructors();

		for (Method method : classInfo.getMethods())
		{
			if (method.isConstructor() && constructors.size() == 1 && method.getParameters().size() == 0 && method.isPublic())
			{
				List<? extends StatementTree> statements = method.getMethodTree().getBody().getStatements();
				if (statements.size() == 0)
				{
					continue;
				}
				if (statements.size() == 1 && "super()".equals(statements.toString()))
				{
					// default constructor without a single statement in its body can be omitted
					// the single statement is the mandatory call to the super constructor
					continue;
				}
			}
			firstLine = languageHelper.newLineIndentIfFalse(firstLine);
			context.setMethod(method);
			methodHandler.handle();
		}
	}
}
