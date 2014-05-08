package de.osthus.ambeth.typeinfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.osthus.ambeth.collections.IMap;

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

	IMap<String, IPropertyInfo> getPropertyMap(Object obj);

	/**
	 * Returns a property name to PropertyInfo Map with all public declared properties of this class.
	 * 
	 * @param type
	 * @return
	 */
	IMap<String, IPropertyInfo> getPropertyMap(Class<?> type);

	String getPropertyNameFor(Method method);

	String getPropertyNameFor(Field field);
}