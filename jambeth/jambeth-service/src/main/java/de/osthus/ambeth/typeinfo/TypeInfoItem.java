package de.osthus.ambeth.typeinfo;

import java.util.Collection;
import java.util.Set;

import de.osthus.ambeth.collections.ObservableArrayList;
import de.osthus.ambeth.collections.ObservableHashSet;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.merge.model.IPrimitiveMember;

public abstract class TypeInfoItem implements ITypeInfoItem, IPrimitiveMember
{
	public static void setEntityType(Class<?> entityType, ITypeInfoItem member, IProperties properties)
	{
		if (member instanceof TypeInfoItem)
		{
			((TypeInfoItem) member).setElementType(entityType);
		}
		else
		{
			throw new IllegalStateException("TypeInfoItem not supported: " + member);
		}
	}

	protected Class<?> elementType;

	protected Class<?> declaringType;

	protected boolean technicalMember;

	@Override
	public Class<?> getElementType()
	{
		return elementType;
	}

	public void setElementType(Class<?> elementType)
	{
		this.elementType = elementType;
	}

	@Override
	public Class<?> getDeclaringType()
	{
		return declaringType;
	}

	@Override
	public boolean canRead()
	{
		return true;
	}

	@Override
	public boolean canWrite()
	{
		return true;
	}

	@Override
	public boolean isTechnicalMember()
	{
		return technicalMember;
	}

	@Override
	public void setTechnicalMember(boolean technicalMember)
	{
		this.technicalMember = technicalMember;
	}

	@Override
	public abstract void setNullEquivalentValue(Object nullEquivalentValue);

	protected abstract FastConstructorAccess<?> getConstructorOfRealType();

	@Override
	public Collection<?> createInstanceOfCollection()
	{
		// OneToMany or ManyToMany Relationship
		Class<?> realType = getRealType();
		if (Iterable.class.isAssignableFrom(realType))
		{
			if (realType.isInterface())
			{
				if (Set.class.isAssignableFrom(realType))
				{
					return new ObservableHashSet<Object>();
				}
				return new ObservableArrayList<Object>();
			}
			try
			{
				return (Collection<?>) getConstructorOfRealType().newInstance();
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return null;
	}
}
