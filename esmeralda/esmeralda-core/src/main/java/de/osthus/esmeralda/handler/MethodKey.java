package de.osthus.esmeralda.handler;

import java.util.Arrays;

public class MethodKey
{
	protected final String declaringTypeName;

	protected final String methodName;

	protected final String[] parameters;

	public MethodKey(String declaringTypeName, String methodName, String[] parameters)
	{
		this.declaringTypeName = declaringTypeName;
		this.methodName = methodName;
		this.parameters = parameters;
	}

	public String getDeclaringTypeName()
	{
		return declaringTypeName;
	}

	public String getMethodName()
	{
		return methodName;
	}

	public String[] getParameters()
	{
		return parameters;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof MethodKey))
		{
			return false;
		}
		MethodKey other = (MethodKey) obj;
		return getMethodName().equals(other.getMethodName()) && Arrays.equals(getParameters(), other.getParameters())
				&& getDeclaringTypeName().equals(other.getDeclaringTypeName());
	}

	@Override
	public int hashCode()
	{
		return getDeclaringTypeName().hashCode() ^ getMethodName().hashCode() ^ Arrays.hashCode(getParameters());
	}

}
