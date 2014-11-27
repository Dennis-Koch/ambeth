package de.osthus.esmeralda.handler.csharp.transformer;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IMethodParameterProcessor;
import de.osthus.esmeralda.handler.IMethodTransformerExtension;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.TransformedMethod;
import de.osthus.esmeralda.handler.csharp.ICsHelper;
import de.osthus.esmeralda.handler.csharp.MethodKey;

public abstract class AbstractMethodTransformerExtension implements IMethodTransformerExtension, IInitializingBean
{
	public static final String defaultMethodTransformerExtensionProp = "DefaultMethodTransformerExtension";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IMethodParameterProcessor defaultMethodParameterProcessor;

	@Autowired
	protected ICsHelper languageHelper;

	@Autowired
	protected IMethodTransformerExtension defaultMethodTransformerExtension;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	protected final HashMap<MethodKey, ITransformedMethod> methodTransformationMap = new HashMap<MethodKey, ITransformedMethod>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// intended blank
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
		String[] parameters = new String[parameterTypes.length];
		for (int a = parameterTypes.length; a-- > 0;)
		{
			parameters[a] = parameterTypes[a].getName();
		}
		TransformedMethod tm = new TransformedMethod(targetOwner, targetMethodName, parameters, isProperty, false);
		tm.setParameterProcessor(defaultMethodParameterProcessor);
		methodTransformationMap.put(//
				new MethodKey(sourceOwner.getName(), sourceMethodName, parameters),//
				tm);
	}

	protected void mapTransformation(Class<?> sourceOwner, String sourceMethodName, String targetOwner, String targetMethodName, IMethodParameterProcessor mpp,
			Class<?>... parameterTypes)
	{
		String[] parameters = new String[parameterTypes.length];
		for (int a = parameterTypes.length; a-- > 0;)
		{
			parameters[a] = parameterTypes[a].getName();
		}
		TransformedMethod tm = new TransformedMethod(targetOwner, targetMethodName, parameters, false, false);
		tm.setParameterProcessor(mpp);
		methodTransformationMap.put(//
				new MethodKey(sourceOwner.getName(), sourceMethodName, parameters),//
				tm);
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
