package de.osthus.ambeth.metadata;

import java.lang.annotation.Annotation;

import de.osthus.ambeth.accessor.AbstractAccessor;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.typeinfo.IPropertyInfo;

public abstract class Member extends AbstractAccessor implements Comparable<Member>
{
	protected Class<?> elementType;

	protected final HashMap<Class<? extends Annotation>, Annotation> typeToAnnotation = new HashMap<Class<? extends Annotation>, Annotation>();

	protected Member(Class<?> type, IPropertyInfo property)
	{
		super(type, property);
	}

	@Override
	public int compareTo(Member o)
	{
		return getName().compareTo(o.getName());
	}

	public Class<?> getElementType()
	{
		return elementType;
	}

	public void setElementType(Class<?> elementType)
	{
		this.elementType = elementType;
	}

	public abstract Class<?> getDeclaringType();

	public abstract Class<?> getRealType();

	public abstract Object getNullEquivalentValue();

	@SuppressWarnings("unchecked")
	public <V extends Annotation> V getAnnotation(Class<V> annotationType)
	{
		return (V) typeToAnnotation.get(annotationType);
	}

	public abstract String getName();

	@Override
	public String toString()
	{
		return "Property " + getName();
	}
}
