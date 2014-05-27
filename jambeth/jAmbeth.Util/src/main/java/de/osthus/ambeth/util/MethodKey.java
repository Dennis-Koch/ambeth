package de.osthus.ambeth.util;

import java.util.Arrays;

public class MethodKey
{
	protected final String methodName;

	protected final Class<?>[] parameterTypes;

	public MethodKey(String methodName, Class<?>[] parameterTypes)
	{
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
	}

	public String getMethodName()
	{
		return methodName;
	}

	public Class<?>[] getParameterTypes()
	{
		return parameterTypes;
	}

	@Override
	public int hashCode()
	{
		return methodName.hashCode() ^ parameterTypes.length;
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
		MethodKey other = (MethodKey) obj;
		if (!EqualsUtil.equals(methodName, other.methodName))
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
