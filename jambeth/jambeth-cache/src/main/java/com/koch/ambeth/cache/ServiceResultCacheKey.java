package com.koch.ambeth.cache;

import java.lang.reflect.Method;

public class ServiceResultCacheKey
{

	public Method method;

	public Object[] arguments;

	public String serviceName;

	@Override
	public int hashCode()
	{
		return method.hashCode() ^ serviceName.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof ServiceResultCacheKey))
		{
			return false;
		}
		ServiceResultCacheKey other = (ServiceResultCacheKey) obj;

		if (!this.method.equals(other.method) || !this.serviceName.equals(other.serviceName))
		{
			return false;
		}
		Object[] otherArgs = other.arguments;
		for (int a = otherArgs.length; a-- > 0;)
		{
			if (!this.arguments[a].equals(otherArgs[a]))
			{
				return false;
			}
		}
		return true;
	}

}
