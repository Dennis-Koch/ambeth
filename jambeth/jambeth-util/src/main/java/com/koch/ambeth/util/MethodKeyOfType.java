package com.koch.ambeth.util;

import java.util.Arrays;

import org.objectweb.asm.Type;

public class MethodKeyOfType
{
	protected final String methodName;

	protected final Type returnType;

	protected final Type[] parameterTypes;

	public MethodKeyOfType(String methodName, Type returnType, Type[] parameterTypes)
	{
		this.methodName = methodName;
		this.returnType = returnType;
		this.parameterTypes = parameterTypes;
	}

	public String getMethodName()
	{
		return methodName;
	}

	public Type getReturnType()
	{
		return returnType;
	}

	public Type[] getParameterTypes()
	{
		return parameterTypes;
	}

	@Override
	public int hashCode()
	{
		return methodName.hashCode() ^ parameterTypes.length ^ returnType.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		MethodKeyOfType other = (MethodKeyOfType) obj;
		if (!EqualsUtil.equals(methodName, other.methodName) || !EqualsUtil.equals(returnType, other.returnType))
		{
			return false;
		}
		if (!Arrays.equals(parameterTypes, other.parameterTypes))
		{
			return false;
		}
		return true;
	}
}
