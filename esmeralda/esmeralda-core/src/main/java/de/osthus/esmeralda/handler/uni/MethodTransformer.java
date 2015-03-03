package de.osthus.esmeralda.handler.uni;

import java.util.List;

import javax.lang.model.element.VariableElement;

import com.sun.tools.javac.tree.JCTree.JCExpression;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.handler.IMethodTransformer;
import de.osthus.esmeralda.handler.IMethodTransformerExtension;
import de.osthus.esmeralda.handler.IMethodTransformerExtensionRegistry;
import de.osthus.esmeralda.handler.ITransformedMemberAccess;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.MethodKey;
import de.osthus.esmeralda.handler.TransformedMemberAccess;
import de.osthus.esmeralda.ioc.EsmeraldaCoreModule;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class MethodTransformer implements IMethodTransformer
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IASTHelper astHelper;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IMethodTransformerExtensionRegistry methodTransformerExtensionRegistry;

	@Override
	public ITransformedMethod transform(String owner, String methodName, List<JCExpression> parameterTypes)
	{
		String[] argTypes = parseArgumentTypes(parameterTypes);
		return transformIntern(owner, methodName, argTypes);
	}

	@Override
	public ITransformedMethod transformMethodDeclaration(Method method)
	{
		String owner = method.getOwningClass().getFqName();
		String methodName = method.getName();
		String[] argTypes = parseArgumentTypes(method.getParameters());
		return transformIntern(owner, methodName, argTypes);
	}

	protected String[] parseArgumentTypes(final IList<VariableElement> parameterTypes)
	{
		final String[] argTypes = new String[parameterTypes.size()];
		for (int a = 0, size = parameterTypes.size(); a < size; a++)
		{
			VariableElement arg = parameterTypes.get(a);
			argTypes[a] = arg.asType().toString();
		}
		return argTypes;
	}

	protected ITransformedMethod transformIntern(String owner, String methodName, String[] argTypes)
	{
		IConversionContext context = this.context.getCurrent();
		String language = context.getLanguage();

		if ("super".equals(methodName) || "this".equals(methodName))
		{
			MethodKey methodKey = new MethodKey(owner, methodName, argTypes);
			IMethodTransformerExtension defaultMethodTransformerExtension = methodTransformerExtensionRegistry.getExtension(language
					+ EsmeraldaCoreModule.DefaultMethodTransformerName);
			return defaultMethodTransformerExtension.buildMethodTransformation(methodKey);
		}

		String currOwner = owner;
		while (currOwner != null)
		{
			IMethodTransformerExtension methodTransformerExtension = methodTransformerExtensionRegistry.getExtension(language + currOwner);
			if (methodTransformerExtension != null)
			{
				MethodKey methodKey = new MethodKey(currOwner, methodName, argTypes);
				ITransformedMethod transformedMethod = methodTransformerExtension.buildMethodTransformation(methodKey);
				if (transformedMethod != null)
				{
					return transformedMethod;
				}
			}
			String nonGenericOwner = astHelper.extractNonGenericType(currOwner);
			if (!nonGenericOwner.equals(currOwner))
			{
				methodTransformerExtension = methodTransformerExtensionRegistry.getExtension(language + nonGenericOwner);
				if (methodTransformerExtension != null)
				{
					MethodKey methodKey = new MethodKey(nonGenericOwner, methodName, argTypes);
					ITransformedMethod transformedMethod = methodTransformerExtension.buildMethodTransformation(methodKey);
					if (transformedMethod != null)
					{
						return transformedMethod;
					}
				}
			}
			JavaClassInfo classInfo = context.resolveClassInfo(currOwner);
			if (classInfo == null)
			{
				throw new IllegalStateException("Must never happen: " + currOwner);
			}
			for (String interfaceName : classInfo.getNameOfInterfaces())
			{
				methodTransformerExtension = methodTransformerExtensionRegistry.getExtension(language + interfaceName);
				if (methodTransformerExtension == null)
				{
					continue;
				}
				MethodKey methodKey = new MethodKey(interfaceName, methodName, argTypes);
				ITransformedMethod transformedMethod = methodTransformerExtension.buildMethodTransformation(methodKey);
				if (transformedMethod != null)
				{
					return transformedMethod;
				}
			}
			currOwner = classInfo.getNameOfSuperClass();
		}

		IMethodTransformerExtension defaultMethodTransformerExtension = methodTransformerExtensionRegistry.getExtension(language
				+ EsmeraldaCoreModule.DefaultMethodTransformerName);
		MethodKey methodKey = new MethodKey(owner, methodName, argTypes);
		return defaultMethodTransformerExtension.buildMethodTransformation(methodKey);
	}

	@Override
	public ITransformedMemberAccess transformFieldAccess(final String owner, final String name)
	{
		IConversionContext context = this.context.getCurrent();
		JavaClassInfo internalClassInfo = context.resolveClassInfo(owner + "." + name, true);
		if (internalClassInfo != null)
		{
			return new TransformedMemberAccess(internalClassInfo.getFqName(), null, internalClassInfo.getFqName());
		}
		JavaClassInfo classInfo = context.resolveClassInfo(owner);
		Field field = classInfo.getField(name);

		return new TransformedMemberAccess(owner, name, field.getFieldType());
	}

	protected String[] parseArgumentTypes(final List<JCExpression> parameterTypes)
	{
		String[] argTypes = astHelper.writeToStash(new IResultingBackgroundWorkerDelegate<String[]>()
		{
			@Override
			public String[] invoke() throws Throwable
			{
				IConversionContext context = MethodTransformer.this.context.getCurrent();
				ILanguageHelper languageHelper = context.getLanguageHelper();
				int size = parameterTypes.size();
				String[] argTypes = new String[size];
				for (int a = 0; a < size; a++)
				{
					JCExpression arg = parameterTypes.get(a);
					languageHelper.writeExpressionTree(arg);
					String typeOnStack = context.getTypeOnStack();
					argTypes[a] = typeOnStack;
				}
				return argTypes;
			}
		});
		return argTypes;
	}
}
