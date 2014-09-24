package de.osthus.ambeth.metadata;

import java.lang.annotation.Annotation;

import de.osthus.ambeth.accessor.AbstractAccessor;
import de.osthus.ambeth.typeinfo.IPropertyInfo;

public abstract class Member extends AbstractAccessor implements Comparable<Member>
{
	protected Member(Class<?> type, IPropertyInfo property)
	{
		super(type, property);
	}

	@Override
	public int compareTo(Member o)
	{
		return getName().compareTo(o.getName());
	}

	public abstract Class<?> getElementType();

	public abstract Class<?> getDeclaringType();

	public abstract Class<?> getRealType();

	public abstract boolean isToMany();

	public abstract Object getNullEquivalentValue();

	public abstract <V extends Annotation> V getAnnotation(Class<V> annotationType);

	public abstract String getName();

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (obj == null || !obj.getClass().equals(getClass()))
		{
			return false;
		}
		Member other = (Member) obj;
		return getDeclaringType().equals(other.getDeclaringType()) && getName().equals(other.getName());
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ getDeclaringType().hashCode() ^ getName().hashCode();
	}

	@Override
	public String toString()
	{
		return "Member " + getName();
	}
}
