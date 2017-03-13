package com.koch.ambeth.service.metadata;

import java.lang.annotation.Annotation;

import com.koch.ambeth.ioc.accessor.AbstractAccessor;

public abstract class Member extends AbstractAccessor implements Comparable<Member>
{
	public abstract Class<?> getElementType();

	public abstract Class<?> getDeclaringType();

	public abstract Class<?> getRealType();

	public abstract Class<?> getEntityType();

	public abstract boolean isToMany();

	public abstract Object getNullEquivalentValue();

	public abstract <V extends Annotation> V getAnnotation(Class<V> annotationType);

	public abstract String getName();

	@Override
	public int compareTo(Member o)
	{
		return getName().compareTo(o.getName());
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (obj == null || !(obj instanceof Member))
		{
			return false;
		}
		Member other = (Member) obj;
		return getEntityType().equals(other.getEntityType()) && getName().equals(other.getName());
	}

	@Override
	public int hashCode()
	{
		return getEntityType().hashCode() ^ getName().hashCode();
	}

	@Override
	public String toString()
	{
		return "Member " + getName();
	}
}
