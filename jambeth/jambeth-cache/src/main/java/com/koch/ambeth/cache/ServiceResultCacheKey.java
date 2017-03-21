package com.koch.ambeth.cache;

/*-
 * #%L
 * jambeth-cache
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

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
