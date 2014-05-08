package de.osthus.ambeth.transfer;

import java.lang.reflect.Method;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.model.IServiceDescription;
import de.osthus.ambeth.objectcollector.IObjectCollector;
import de.osthus.ambeth.util.StringConversionHelper;

@XmlRootElement(name = "ServiceDescription", namespace = "http://schemas.osthus.de/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceDescription implements IServiceDescription
{
	@XmlElement(required = true)
	protected String serviceName;

	@XmlElement(required = true)
	protected String methodName;

	@XmlElement(required = true)
	protected Class<?>[] paramTypes;

	@XmlElement(required = true)
	protected ISecurityScope[] securityScopes;

	@XmlElement(required = true)
	protected Object[] arguments;

	protected transient Method method;

	@Override
	public String getServiceName()
	{
		return serviceName;
	}

	public void setServiceName(String serviceName)
	{
		this.serviceName = serviceName;
	}

	@Override
	public Method getMethod(Class<?> serviceType, IObjectCollector objectCollector)
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

	public void setMethodName(String methodName)
	{
		this.methodName = methodName;
	}

	public String getMethodName()
	{
		return methodName;
	}

	@Override
	public Object[] getArguments()
	{
		return arguments;
	}

	public Class<?>[] getParamTypes()
	{
		return paramTypes;
	}

	public void setParamTypes(Class<?>[] paramTypes)
	{
		this.paramTypes = paramTypes;
	}

	public void setArguments(Object[] arguments)
	{
		this.arguments = arguments;
	}

	@Override
	public ISecurityScope[] getSecurityScopes()
	{
		return securityScopes;
	}

	public void setSecurityScopes(ISecurityScope[] securityScopes)
	{
		this.securityScopes = securityScopes;
	}
}
