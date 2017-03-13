package com.koch.ambeth.util.typeinfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.koch.ambeth.util.collections.IMap;

public interface IPropertyInfoProvider
{
	IPropertyInfo getProperty(Object obj, String propertyName);

	IPropertyInfo getProperty(Class<?> type, String propertyName);

	IPropertyInfo[] getProperties(Object obj);

	/**
	 * Returns a PropertyInfo array with all public declared properties of this class.
	 * 
	 * @param type
	 * @return
	 */
	IPropertyInfo[] getProperties(Class<?> type);

	IPropertyInfo[] getIocProperties(Class<?> beanType);

	IPropertyInfo[] getPrivateProperties(Class<?> type);

	IMap<String, IPropertyInfo> getPropertyMap(Object obj);

	/**
	 * Returns a property name to PropertyInfo Map with all public declared properties of this class.
	 * 
	 * @param type
	 * @return
	 */
	IMap<String, IPropertyInfo> getPropertyMap(Class<?> type);

	IMap<String, IPropertyInfo> getIocPropertyMap(Class<?> type);

	IMap<String, IPropertyInfo> getPrivatePropertyMap(Class<?> type);

	String getPropertyNameFor(Method method);

	String getPropertyNameFor(Field field);
}