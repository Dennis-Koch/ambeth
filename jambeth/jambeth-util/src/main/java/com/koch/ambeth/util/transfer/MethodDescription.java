package com.koch.ambeth.util.transfer;

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
