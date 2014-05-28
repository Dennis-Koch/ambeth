package de.osthus.ambeth.util;

import java.util.Arrays;

import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

public class MethodKeyOfType
{
	protected final String methodName;

	protected final Type[] parameterTypes;

	public MethodKeyOfType(String methodName, Type[] parameterTypes)
	{
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
	}

	public String getMethodName()
	{
		return methodName;
	}

	public Type[] getParameterTypes()
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
		MethodKeyOfType other = (MethodKeyOfType) obj;
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
