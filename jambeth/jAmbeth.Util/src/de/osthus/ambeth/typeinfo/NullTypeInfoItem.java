package de.osthus.ambeth.typeinfo;

import java.lang.annotation.Annotation;
import java.util.Collection;

public final class NullTypeInfoItem implements ITypeInfoItem
{
	public static final NullTypeInfoItem INSTANCE = new NullTypeInfoItem();

	private NullTypeInfoItem()
	{
		// Intended blank
	}

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public Object getDefaultValue()
	{
		return null;
	}

	@Override
	public void setDefaultValue(Object defaultValue)
	{
	}

	@Override
	public Object getNullEquivalentValue()
	{
		return null;
	}

	@Override
	public void setNullEquivalentValue(Object nullEquivalentValue)
	{
	}

	@Override
	public Class<?> getRealType()
	{
		return null;
	}

	@Override
	public Class<?> getElementType()
	{
		return null;
	}

	@Override
	public Class<?> getDeclaringType()
	{
		return null;
	}

	@Override
	public boolean canRead()
	{
		return false;
	}

	@Override
	public boolean canWrite()
	{
		return false;
	}

	@Override
	public boolean isTechnicalMember()
	{
		return false;
	}

	@Override
	public void setTechnicalMember(boolean b)
	{
	}

	@Override
	public Object getValue(Object obj)
	{
		return null;
	}

	@Override
	public Object getValue(Object obj, boolean allowNullEquivalentValue)
	{
		return null;
	}

	@Override
	public void setValue(Object obj, Object value)
	{
	}

	@Override
	public <V extends Annotation> V getAnnotation(Class<V> annotationType)
	{
		return null;
	}

	@Override
	public String getXMLName()
	{
		return null;
	}

	@Override
	public boolean isXMLIgnore()
	{
		return false;
	}

	@Override
	public Collection<?> createInstanceOfCollection()
	{
		return null;
	}
}
