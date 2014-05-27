package de.osthus.ambeth.model;

import java.lang.reflect.Method;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.objectcollector.IObjectCollector;

@XmlType
public interface IServiceDescription
{
	String getServiceName();

	Method getMethod(Class<?> serviceType, IObjectCollector objectCollector);

	Object[] getArguments();

	ISecurityScope[] getSecurityScopes();
}
