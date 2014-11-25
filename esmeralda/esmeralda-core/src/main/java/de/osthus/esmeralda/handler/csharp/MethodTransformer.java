package de.osthus.esmeralda.handler.csharp;

import java.util.List;
import java.util.regex.Matcher;

import com.sun.tools.javac.tree.JCTree.JCExpression;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.esmeralda.ConversionContext;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IMethodTransformer;
import de.osthus.esmeralda.handler.ITransformedMemberAccess;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.TransformedMemberAccess;
import de.osthus.esmeralda.handler.TransformedMethod;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class MethodTransformer implements IMethodTransformer, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected ICsHelper languageHelper;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IProxyFactory proxFactory;

	protected final HashMap<MethodKey, ITransformedMethod> methodTransformationMap = new HashMap<MethodKey, ITransformedMethod>();

	private ITransformedMethod nullTransformedMethod;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		nullTransformedMethod = proxFactory.createProxy(ITransformedMethod.class);

		mapTransformation(java.lang.Class.class, "getSimpleName", "System.Type", "Name", true);
		mapTransformation(java.lang.Class.class, "getName", "System.Type", "FullName", true);
		mapTransformation(java.io.PrintStream.class, "println", "System.Console", "WriteLine", false, String.class);
		mapTransformation(java.io.PrintStream.class, "print", "System.Console", "Write", false, String.class);
		mapTransformation(java.util.List.class, "size", "System.Collections.ICollection", "Count", true);
		mapTransformation(java.lang.Object.class, "hashCode", "System.Object", "GetHashCode", false);
		mapTransformation(java.lang.Object.class, "getClass", "System.Object", "GetType", false);
	}

	protected void mapTransformation(Class<?> sourceOwner, String sourceMethodName, String targetOwner, String targetMethodName, boolean isProperty,
			Class<?>... parameterTypes)
	{
		String[] parameters = new String[parameterTypes.length];
		for (int a = parameterTypes.length; a-- > 0;)
		{
			parameters[a] = parameterTypes[a].getName();
		}
		methodTransformationMap.put(//
				new MethodKey(sourceOwner.getName(), sourceMethodName, parameters),//
				new TransformedMethod(targetOwner, targetMethodName, parameters, isProperty, false));
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

	@Override
	public ITransformedMethod transform(String owner, String methodName, List<JCExpression> parameterTypes)
	{
		IConversionContext context = this.context.getCurrent();

		String[] argTypes = parseArgumentTypes(parameterTypes);

		String currOwner = owner;
		while (currOwner != null)
		{
			String nonGenericOwner = currOwner;
			Matcher genericTypeMatcher = ConversionContext.genericTypePattern.matcher(currOwner);
			if (genericTypeMatcher.matches())
			{
				nonGenericOwner = genericTypeMatcher.group(1);
			}
			ITransformedMethod transformedMethod = methodTransformationMap.get(new MethodKey(nonGenericOwner, methodName, argTypes));
			if (transformedMethod != null)
			{
				return transformedMethod;
			}
			JavaClassInfo classInfo = context.resolveClassInfo(currOwner);
			if (classInfo == null)
			{
				throw new IllegalStateException(currOwner);
			}
			for (String interfaceName : classInfo.getNameOfInterfaces())
			{
				transformedMethod = methodTransformationMap.get(new MethodKey(interfaceName, methodName, argTypes));
				if (transformedMethod != null)
				{
					return transformedMethod;
				}
			}
			currOwner = classInfo.getNameOfSuperClass();
		}

		String formattedMethodName = StringConversionHelper.upperCaseFirst(objectCollector, methodName);
		return new TransformedMethod(owner, formattedMethodName, argTypes, false, false);
	}

	@Override
	public ITransformedMethod transform(Method method)
	{
		return null;
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
}
