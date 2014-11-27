package de.osthus.esmeralda.handler.csharp;

import java.util.List;

import com.sun.tools.javac.tree.JCTree.JCExpression;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.MapExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.handler.IMethodTransformer;
import de.osthus.esmeralda.handler.IMethodTransformerExtension;
import de.osthus.esmeralda.handler.IMethodTransformerExtensionExtendable;
import de.osthus.esmeralda.handler.ITransformedMemberAccess;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.TransformedMemberAccess;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class MethodTransformer implements IMethodTransformer, IMethodTransformerExtensionExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IASTHelper astHelper;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IMethodTransformerExtension defaultMethodTransformerExtension;

	@Autowired
	protected ICsHelper languageHelper;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	protected final MapExtendableContainer<String, IMethodTransformerExtension> methodTransformerExtensions = new MapExtendableContainer<String, IMethodTransformerExtension>(
			"methodTransformerExtension", "fqTypeName");

	@Override
	public ITransformedMethod transform(String owner, String methodName, List<JCExpression> parameterTypes)
	{
		IConversionContext context = this.context.getCurrent();

		String[] argTypes = parseArgumentTypes(parameterTypes);

		String currOwner = owner;
		while (currOwner != null)
		{
			IMethodTransformerExtension methodTransformerExtension = methodTransformerExtensions.getExtension(currOwner);
			if (methodTransformerExtension != null)
			{
				MethodKey methodKey = new MethodKey(currOwner, methodName, argTypes);
				return methodTransformerExtension.buildMethodTransformation(methodKey);
			}
			String nonGenericOwner = astHelper.extractNonGenericType(currOwner);
			if (!nonGenericOwner.equals(currOwner))
			{
				methodTransformerExtension = methodTransformerExtensions.getExtension(nonGenericOwner);
				if (methodTransformerExtension != null)
				{
					MethodKey methodKey = new MethodKey(nonGenericOwner, methodName, argTypes);
					return methodTransformerExtension.buildMethodTransformation(methodKey);
				}
			}
			JavaClassInfo classInfo = context.resolveClassInfo(currOwner);
			if (classInfo == null)
			{
				throw new IllegalStateException("Must never happen: " + currOwner);
			}
			for (String interfaceName : classInfo.getNameOfInterfaces())
			{
				methodTransformerExtension = methodTransformerExtensions.getExtension(interfaceName);
				if (methodTransformerExtension == null)
				{
					continue;
				}
				MethodKey methodKey = new MethodKey(interfaceName, methodName, argTypes);
				return methodTransformerExtension.buildMethodTransformation(methodKey);
			}
			currOwner = classInfo.getNameOfSuperClass();
		}
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
		final String[] argTypes = new String[parameterTypes.size()];
		languageHelper.writeToStash(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				for (int a = 0, size = parameterTypes.size(); a < size; a++)
				{
					JCExpression arg = parameterTypes.get(a);
					languageHelper.writeExpressionTree(arg);
					String typeOnStack = context.getTypeOnStack();
					argTypes[a] = typeOnStack;
				}
			}
		});
		return argTypes;
	}

	@Override
	public void registerMethodTransformerExtension(IMethodTransformerExtension methodTransformerExtension, String fqClassType)
	{
		methodTransformerExtensions.register(methodTransformerExtension, fqClassType);
	}

	@Override
	public void unregisterMethodTransformerExtension(IMethodTransformerExtension methodTransformerExtension, String fqClassType)
	{
		methodTransformerExtensions.unregister(methodTransformerExtension, fqClassType);
	}
}
