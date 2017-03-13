package com.koch.ambeth.util.model;

import java.lang.reflect.Method;

import com.koch.ambeth.util.annotation.XmlType;
import com.koch.ambeth.util.objectcollector.IObjectCollector;

@XmlType
public interface IMethodDescription
{
	Method getMethod(IObjectCollector objectCollector);
}
