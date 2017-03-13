package com.koch.ambeth.util.typeinfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public interface IPropertyInfo
{
	Class<?> getEntityType();

	String getName();

	Class<?> getPropertyType();

	Class<?> getDeclaringType();

	Class<?> getElementType();

	boolean isReadable();

	boolean isWritable();

	Field getBackingField();

	int getModifiers();

	Object getValue(Object obj);

	void setValue(Object obj, Object value);

	Annotation[] getAnnotations();

	<V extends Annotation> V getAnnotation(Class<V> annotationType);

	<V extends Annotation> boolean isAnnotationPresent(Class<V> annotationType);
}
