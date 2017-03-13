package com.koch.ambeth.util;

import java.util.Arrays;

public class MethodKey implements IPrintable
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

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append("MethodKey: ").append(methodName).append('(');
		for (int a = 0, size = parameterTypes.length; a < size; a++)
		{
			if (a > 0)
			{
				sb.append(", ");
			}
			sb.append(parameterTypes[a].getName());
		}
		sb.append(')');
	}
}
