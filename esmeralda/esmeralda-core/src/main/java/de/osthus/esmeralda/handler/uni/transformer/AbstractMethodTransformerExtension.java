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
import demo.codeanalyzer.common.model.JavaClassInfo;

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

	@Override
	public ITransformedMethod buildMethodTransformation(MethodKey methodKey)
	{
		ITransformedMethod transformedMethod = methodTransformationMap.get(methodKey);
		if (transformedMethod != null)
		{
			return transformedMethod;
		}
		String[] parameters = methodKey.getParameters();
		String[] newParameters = null;
		for (int a = parameters.length; a-- > 0;)
		{
			String parameterName = parameters[a];
			JavaClassInfo parameterCI = context.resolveClassInfo(parameterName);
			String nonGenericParameterName = parameterCI.getPackageName() != null ? parameterCI.getPackageName() + "." + parameterCI.getNonGenericName()
					: parameterCI.getNonGenericName();
			if (parameterName.equals(nonGenericParameterName))
			{
				continue;
			}
			if (newParameters == null)
			{
				newParameters = new String[parameters.length];
				System.arraycopy(parameters, 0, newParameters, 0, parameters.length);
			}
			newParameters[a] = nonGenericParameterName;
		}
		if (newParameters != null)
		{
			MethodKey nonGenericMethodKey = new MethodKey(methodKey.getDeclaringTypeName(), methodKey.getMethodName(), newParameters);
			transformedMethod = methodTransformationMap.get(nonGenericMethodKey);
			if (transformedMethod != null)
			{
				return transformedMethod;
			}
		}
		transformedMethod = buildMethodTransformationIntern(methodKey);
		if (transformedMethod != null)
		{
			return transformedMethod;
		}

		return null;
	}

	protected ITransformedMethod buildMethodTransformationIntern(MethodKey methodKey)
	{
		return null;
	}

	protected TransformedMethod mapTransformation(Class<?> sourceOwner, String sourceMethodName, String targetOwner, String targetMethodName,
			boolean isProperty, Class<?>... parameterTypes)
	{
		return mapTransformation(sourceOwner, sourceMethodName, targetOwner, targetMethodName, isProperty, null, false, parameterTypes);
	}

	protected TransformedMethod mapTransformation(Class<?> sourceOwner, String sourceMethodName, String targetOwner, String targetMethodName,
			boolean isProperty, Boolean writeOwner, boolean isOwnerAType, Class<?>... parameterTypes)
	{
		String[] parameters = new String[parameterTypes.length];
		for (int a = parameterTypes.length; a-- > 0;)
		{
			Class<?> parameterType = parameterTypes[a];
			if (parameterType != null)
			{
				parameters[a] = parameterTypes[a].getName();
			}
		}
		TransformedMethod tm = new TransformedMethod(targetOwner, targetMethodName, parameters, false, writeOwner, isOwnerAType);
		tm.setPropertyInvocation(isProperty);
		tm.setParameterProcessor(defaultMethodParameterProcessor);
		methodTransformationMap.put(//
				new MethodKey(sourceOwner.getName(), sourceMethodName, parameters),//
				tm);
		return tm;
	}

	protected void mapTransformationOverloads(Class<?> sourceOwner, String sourceMethodName, String targetOwner, String targetMethodName, Boolean writeOwner,
			boolean isOwnerAType, Class<?>... singleParameterTypes)
	{
		for (Class<?> singleParameterType : singleParameterTypes)
		{
			mapTransformation(sourceOwner, sourceMethodName, targetOwner, targetMethodName, false, writeOwner, isOwnerAType, singleParameterType);
		}
	}

	protected TransformedMethod mapTransformation(Class<?> sourceOwner, String sourceMethodName, String targetOwner, String targetMethodName,
			IMethodParameterProcessor mpp, Class<?>... parameterTypes)
	{
		return mapTransformation(sourceOwner, sourceMethodName, targetOwner, targetMethodName, mpp, null, false, parameterTypes);
	}

	protected TransformedMethod mapTransformation(Class<?> sourceOwner, String sourceMethodName, String targetOwner, String targetMethodName,
			IMethodParameterProcessor mpp, Boolean writeOwner, boolean isOwnerAType, Class<?>... parameterTypes)
	{
		String[] parameters = new String[parameterTypes.length];
		for (int a = parameterTypes.length; a-- > 0;)
		{
			parameters[a] = parameterTypes[a].getName();
		}
		TransformedMethod tm = new TransformedMethod(targetOwner, targetMethodName, parameters, false, writeOwner, isOwnerAType);
		tm.setParameterProcessor(mpp);
		methodTransformationMap.put(//
				new MethodKey(sourceOwner.getName(), sourceMethodName, parameters),//
				tm);
		return tm;
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
