package com.koch.ambeth.util.transfer;

/*-
 * #%L
 * jambeth-util
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.util.StringConversionHelper;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.model.IMethodDescription;
import com.koch.ambeth.util.objectcollector.IObjectCollector;

@XmlRootElement(name = "MethodDescription", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class MethodDescription implements IMethodDescription
{
	@XmlElement(required = true)
	protected Class<?> serviceType;

	@XmlElement(required = true)
	protected String methodName;

	@XmlElement(required = true)
	protected Class<?>[] paramTypes;

	protected transient Method method;

	public void setMethodName(String methodName)
	{
		this.methodName = methodName;
	}

	public String getMethodName()
	{
		return methodName;
	}

	@Override
	public Method getMethod(IObjectCollector objectCollector)
	{
		if (method == null)
		{
			try
			{
				try
				{
					method = serviceType.getMethod(StringConversionHelper.upperCaseFirst(objectCollector, methodName), paramTypes);
				}
				catch (NoSuchMethodException e)
				{
					method = serviceType.getMethod(StringConversionHelper.lowerCaseFirst(objectCollector, methodName), paramTypes);
				}
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return method;
	}

	public Class<?> getServiceType()
	{
		return serviceType;
	}

	public void setServiceType(Class<?> serviceType)
	{
		this.serviceType = serviceType;
	}

	public Class<?>[] getParamTypes()
	{
		return paramTypes;
	}

	public void setParamTypes(Class<?>[] paramTypes)
	{
		this.paramTypes = paramTypes;
	}

}
