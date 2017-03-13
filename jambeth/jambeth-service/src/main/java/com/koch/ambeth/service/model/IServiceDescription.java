package com.koch.ambeth.service.model;

import java.lang.reflect.Method;

import com.koch.ambeth.util.annotation.XmlType;
import com.koch.ambeth.util.objectcollector.IObjectCollector;

@XmlType
public interface IServiceDescription
{
	String getServiceName();

	Method getMethod(Class<?> serviceType, IObjectCollector objectCollector);

	Object[] getArguments();

	ISecurityScope[] getSecurityScopes();
}
