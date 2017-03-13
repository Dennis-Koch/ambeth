package com.koch.ambeth.util.typeinfo;

import java.lang.annotation.Annotation;
import java.util.Collection;

import com.koch.ambeth.util.INamed;

public interface ITypeInfoItem extends INamed
{
	Object getDefaultValue();

	void setDefaultValue(Object defaultValue);

	Object getNullEquivalentValue();

	void setNullEquivalentValue(Object nullEquivalentValue);

	Class<?> getRealType();

	Class<?> getElementType();

	Class<?> getDeclaringType();

	boolean canRead();

	boolean canWrite();

	boolean isTechnicalMember();

	void setTechnicalMember(boolean b);

	Object getValue(Object obj);

	Object getValue(Object obj, boolean allowNullEquivalentValue);

	void setValue(Object obj, Object value);

	<V extends Annotation> V getAnnotation(Class<V> annotationType);

	String getXMLName();

	boolean isXMLIgnore();

	Collection<?> createInstanceOfCollection();
}
