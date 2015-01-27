package de.osthus.esmeralda.handler.uni.transformer;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IMethodParameterProcessor;
import de.osthus.esmeralda.handler.IMethodTransformerExtension;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.MethodKey;
import de.osthus.esmeralda.handler.TransformedMethod;

public abstract class AbstractMethodTransformerExtension implements IMethodTransformerExtension, IInitializingBean
{
	public static final String defaultMethodTransformerExtensionProp = "DefaultMethodTransformerExtension";

	public static final String defaultMethodParameterProcessorProp = "DefaultMethodParameterProcessor";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	private IServiceContext serviceContext;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IConversionContext context;

	protected IMethodParameterProcessor defaultMethodParameterProcessor;

	protected IMethodTransformerExtension defaultMethodTransformerExtension;

	protected final HashMap<MethodKey, ITransformedMethod> methodTransformationMap = new HashMap<MethodKey, ITransformedMethod>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// intended blank
	}

	public void setDefaultMethodParameterProcessor(IMethodParameterProcessor defaultMethodParameterProcessor)
	{
		this.defaultMethodParameterProcessor = defaultMethodParameterProcessor;
	}

	public void setDefaultMethodTransformerExtension(IMethodTransformerExtension defaultMethodTransformerExtension)
	{
		this.defaultMethodTransformerExtension = defaultMethodTransformerExtension;
	}

	@Override
	public final ITransformedMethod buildMethodTransformation(MethodKey methodKey)
	{
		ITransformedMethod transformedMethod = methodTransformationMap.get(methodKey);
		if (transformedMethod != null)
		{
			return transformedMethod;
		}
		transformedMethod = buildMethodTransformationIntern(methodKey);
		if (transformedMethod != null)
		{
			return transformedMethod;
		}

		return defaultMethodTransformerExtension.buildMethodTransformation(methodKey);
	}

	protected ITransformedMethod buildMethodTransformationIntern(MethodKey methodKey)
	{
		return null;
	}

	protected void mapTransformation(Class<?> sourceOwner, String sourceMethodName, String targetOwner, String targetMethodName, boolean isProperty,
			Class<?>... parameterTypes)
	{
		mapTransformation(sourceOwner, sourceMethodName, targetOwner, targetMethodName, isProperty, null, false, parameterTypes);
	}

	protected void mapTransformation(Class<?> sourceOwner, String sourceMethodName, String targetOwner, String targetMethodName, boolean isProperty,
			Boolean writeOwner, boolean isOwnerAType, Class<?>... parameterTypes)
	{
		String[] parameters = new String[parameterTypes.length];
		for (int a = parameterTypes.length; a-- > 0;)
		{
			parameters[a] = parameterTypes[a].getName();
		}
		TransformedMethod tm = new TransformedMethod(targetOwner, targetMethodName, parameters, isProperty, false, writeOwner, isOwnerAType);
		tm.setParameterProcessor(defaultMethodParameterProcessor);
		methodTransformationMap.put(//
				new MethodKey(sourceOwner.getName(), sourceMethodName, parameters),//
				tm);
	}

	protected void mapTransformationOverloads(Class<?> sourceOwner, String sourceMethodName, String targetOwner, String targetMethodName, Boolean writeOwner,
			boolean isOwnerAType, Class<?>... singleParameterTypes)
	{
		for (Class<?> singleParameterType : singleParameterTypes)
		{
			mapTransformation(sourceOwner, sourceMethodName, targetOwner, targetMethodName, false, writeOwner, isOwnerAType, singleParameterType);
		}
	}

	protected void mapTransformation(Class<?> sourceOwner, String sourceMethodName, String targetOwner, String targetMethodName, IMethodParameterProcessor mpp,
			Class<?>... parameterTypes)
	{
		mapTransformation(sourceOwner, sourceMethodName, targetOwner, targetMethodName, mpp, null, false, parameterTypes);
	}

	protected void mapTransformation(Class<?> sourceOwner, String sourceMethodName, String targetOwner, String targetMethodName, IMethodParameterProcessor mpp,
			Boolean writeOwner, boolean isOwnerAType, Class<?>... parameterTypes)
	{
		String[] parameters = new String[parameterTypes.length];
		for (int a = parameterTypes.length; a-- > 0;)
		{
			parameters[a] = parameterTypes[a].getName();
		}
		TransformedMethod tm = new TransformedMethod(targetOwner, targetMethodName, parameters, false, false, writeOwner, isOwnerAType);
		tm.setParameterProcessor(mpp);
		methodTransformationMap.put(//
				new MethodKey(sourceOwner.getName(), sourceMethodName, parameters),//
				tm);
	}

	protected void mapTransformationOverloads(Class<?> sourceOwner, String sourceMethodName, String targetOwner, String targetMethodName,
			IMethodParameterProcessor mpp, Boolean writeOwner, Class<?>... singleParameterTypes)
	{
		for (Class<?> singleParameterType : singleParameterTypes)
		{
			mapTransformation(sourceOwner, sourceMethodName, targetOwner, targetMethodName, mpp, singleParameterType);
		}
	}

	protected MethodKey buildMethodKey(java.lang.reflect.Method method)
	{
		Class<?>[] parameterTypes = method.getParameterTypes();
		String[] parameters = new String[parameterTypes.length];
		for (int a = parameterTypes.length; a-- > 0;)
		{
			parameters[a] = parameterTypes[a].getName();
		}
		return new MethodKey(method.getDeclaringClass().getName(), method.getName(), parameters);
	}
}
