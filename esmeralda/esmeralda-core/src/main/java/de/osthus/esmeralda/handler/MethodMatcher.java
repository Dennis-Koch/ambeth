package de.osthus.esmeralda.handler;

import java.util.Arrays;
import java.util.Set;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.ParamHolder;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.TypeResolveException;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class MethodMatcher implements IMethodMatcher
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

	@Override
	public String resolveMethodReturnType(String currOwner, String methodName, String... argTypes)
	{
		if ("super".equals(methodName))
		{
			JavaClassInfo ownerInfo = context.resolveClassInfo(currOwner);
			return resolveMethodReturnType(ownerInfo.getNameOfSuperClass(), "this", argTypes);
		}
		if ("this".equals(methodName))
		{
			return Void.TYPE.getName();
		}
		ParamHolder<Method> methodWithBoxingPH = new ParamHolder<Method>();
		Method method = searchMethodOnType(currOwner, methodName, argTypes, new HashSet<String>(), methodWithBoxingPH);
		if (method != null)
		{
			return method.getReturnType();
		}
		if (methodWithBoxingPH.getValue() != null)
		{
			// if no method with object inheritance match could be found we return the auto-box match if any
			return methodWithBoxingPH.getValue().getReturnType();
		}
		throw new TypeResolveException("No matching method found '" + methodName + "(" + Arrays.toString(argTypes) + ")");
	}

	protected boolean matchParameterByBoxing(String argType, String parameterTypeName)
	{
		String boxedArgType = ASTHelper.unboxedToBoxedTypeMap.get(argType);
		if (boxedArgType != null)
		{
			return matchParameterByInheritance(boxedArgType, parameterTypeName);
		}
		String unboxedArgType = ASTHelper.boxedToUnboxedTypeMap.get(argType);
		// check for auto-unboxing
		return (unboxedArgType != null && parameterTypeName.equals(unboxedArgType));
	}

	protected boolean matchParameterByInheritance(String argTypeName, String parameterTypeName)
	{
		if (parameterTypeName.equals(argTypeName))
		{
			// early match
			return true;
		}
		while (argTypeName.endsWith("[]") && parameterTypeName.endsWith("[]"))
		{
			// trim the array information if BOTH parameter types are an array
			argTypeName = argTypeName.substring(0, argTypeName.length() - 2);
			parameterTypeName = parameterTypeName.substring(0, parameterTypeName.length() - 2);
		}
		IConversionContext context = this.context.getCurrent();
		JavaClassInfo parameterType = context.resolveClassInfo(parameterTypeName);
		JavaClassInfo currExtendsFromType = parameterType.getExtendsFrom();
		while (currExtendsFromType != null)
		{
			if (currExtendsFromType.getName().equals(argTypeName) || currExtendsFromType.getFqName().equals(argTypeName))
			{
				return true;
			}
			currExtendsFromType = currExtendsFromType.getExtendsFrom();
		}
		JavaClassInfo argType = context.resolveClassInfo(argTypeName);
		currExtendsFromType = argType.getExtendsFrom();
		while (currExtendsFromType != null)
		{
			if (currExtendsFromType.getName().equals(parameterTypeName) || currExtendsFromType.getFqName().equals(parameterTypeName))
			{
				return true;
			}
			currExtendsFromType = currExtendsFromType.getExtendsFrom();
		}
		String nonGenericParameterTypeName = astHelper.extractNonGenericType(parameterTypeName);

		while (argTypeName != null)
		{
			if (parameterTypeName.equals(argTypeName) || nonGenericParameterTypeName.equals(argTypeName))
			{
				return true;
			}
			if (parameterTypeName.equals(nonGenericParameterTypeName))
			{
				// parameterTypeName is not a generic type. If that is the case we check whether we match against the non generic type of argType
				String nonGenericArgType = astHelper.extractNonGenericType(argTypeName);
				if (nonGenericArgType.equals(nonGenericParameterTypeName))
				{
					return true;
				}
			}
			JavaClassInfo argClassInfo = context.resolveClassInfo(argTypeName);
			if (argClassInfo == null)
			{
				return false;
			}
			for (String nameOfInterface : argClassInfo.getNameOfInterfaces())
			{
				if (matchParameterByInheritance(nameOfInterface, parameterTypeName))
				{
					return true;
				}
			}
			argTypeName = argClassInfo.getNameOfSuperClass();
		}
		return false;
	}

	protected Method searchMethodOnType(String type, String methodName, String[] argTypes, Set<String> alreadyTriedTypes, ParamHolder<Method> methodWithBoxingPH)
	{
		if (!alreadyTriedTypes.add(type))
		{
			// already tried
			return null;
		}
		Method methodWithBoxing = null;
		Method methodOnInterface = null;
		String currType = type;
		while (currType != null)
		{
			JavaClassInfo ownerInfo = context.resolveClassInfo(currType);
			if (ownerInfo == null)
			{
				throw new IllegalStateException("ClassInfo not resolved: '" + currType + "'");
			}
			for (Method method : ownerInfo.getMethods())
			{
				if (!method.getName().equals(methodName))
				{
					continue;
				}
				IList<VariableElement> parameters = method.getParameters();
				if (parameters.size() != argTypes.length)
				{
					continue;
				}
				Method oldMethod = context.getMethod();
				context.setMethod(method);
				try
				{
					if (matchParametersByInheritance(argTypes, parameters))
					{
						return method;
					}
					if (methodWithBoxing != null)
					{
						// we already found our auto-box candidate
						continue;
					}
					// we check if the current method might match for auto-boxing
					if (matchParametersByBoxing(argTypes, parameters))
					{
						methodWithBoxing = method;
					}
				}
				finally
				{
					context.setMethod(oldMethod);
				}
			}
			if (methodOnInterface == null)
			{
				for (String nameOfInterface : ownerInfo.getNameOfInterfaces())
				{
					methodOnInterface = searchMethodOnType(nameOfInterface, methodName, argTypes, alreadyTriedTypes, methodWithBoxingPH);
					if (methodOnInterface != null)
					{
						break;
					}
				}
			}
			currType = ownerInfo.getNameOfSuperClass();
		}
		if (methodOnInterface != null)
		{
			return methodOnInterface;
		}
		return methodWithBoxing;
	}

	protected boolean matchParametersByBoxing(String[] argTypes, IList<VariableElement> parameters)
	{
		for (int a = argTypes.length; a-- > 0;)
		{
			String argType = argTypes[a];
			TypeMirror parameterType = parameters.get(a).asType();
			String parameterTypeName = parameterType.toString();
			if (matchParameterByInheritance(argType, parameterTypeName))
			{
				continue;
			}
			if (!matchParameterByBoxing(argType, parameterTypeName))
			{
				return false;
			}
		}
		return true;
	}

	protected boolean matchParametersByInheritance(String[] argTypes, IList<VariableElement> parameters)
	{
		for (int a = argTypes.length; a-- > 0;)
		{
			String argType = argTypes[a];
			TypeMirror parameterType = parameters.get(a).asType();
			String parameterTypeName = parameterType.toString();
			if (!matchParameterByInheritance(argType, parameterTypeName))
			{
				return false;
			}
		}
		return true;
	}
}
