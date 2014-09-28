package de.osthus.ambeth.metadata;

import java.lang.annotation.Annotation;

public class IntermediatePrimitiveMember extends PrimitiveMember
{
	protected final String propertyName;

	protected final Class<?> type;

	protected final Class<?> realType;

	protected final Class<?> elementType;

	public IntermediatePrimitiveMember(Class<?> type, Class<?> realType, Class<?> elementType, String propertyName)

	{
		super(type, null);
		this.type = type;
		this.realType = realType;
		this.elementType = elementType;
		this.propertyName = propertyName;
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
	public java.lang.String getName()
	{
		return propertyName;
	}

	@Override
	public Class<?> getDeclaringType()
	{
		return type;
	}

	@Override
	public Class<?> getRealType()
	{
		return realType;
	}

	@Override
	public <V extends Annotation> V getAnnotation(Class<V> annotationType)
	{
		throw createException();
	}

	protected RuntimeException createException()
	{
		return new UnsupportedOperationException("This in an intermediate member which works only as a stub for a later bytecode-enhanced member");
	}

	@Override
	public boolean isTechnicalMember()
	{
		throw createException();
	}

	@Override
	public Object getNullEquivalentValue()
	{
		throw createException();
	}

	@Override
	public boolean isToMany()
	{
		throw createException();
	}

	@Override
	public Class<?> getElementType()
	{
		return elementType;
	}

	@Override
	public Class<?> getEntityType()
	{
		return type;
	}

	@Override
	public Object getValue(Object obj)
	{
		throw createException();
	}

	@Override
	public Object getValue(Object obj, boolean allowNullEquivalentValue)
	{
		throw createException();
	}

	@Override
	public void setValue(Object obj, Object value)
	{
		throw createException();
	}
}
