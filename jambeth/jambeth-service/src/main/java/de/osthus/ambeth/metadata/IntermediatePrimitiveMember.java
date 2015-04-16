package de.osthus.ambeth.metadata;

import java.lang.annotation.Annotation;
import java.util.Collection;

import de.osthus.ambeth.collections.HashMap;

public class IntermediatePrimitiveMember extends PrimitiveMember implements IPrimitiveMemberWrite
{
	protected final String propertyName;

	protected final Class<?> entityType;

	protected final Class<?> declaringType;

	protected final Class<?> realType;

	protected final Class<?> elementType;

	protected final HashMap<Class<?>, Annotation> annotationMap;

	protected boolean technicalMember;

	protected boolean isTransient;

	public IntermediatePrimitiveMember(Class<?> declaringType, Class<?> entityType, Class<?> realType, Class<?> elementType, String propertyName,
			Annotation[] annotations)
	{
		this.declaringType = declaringType;
		this.entityType = entityType;
		this.realType = realType;
		this.elementType = elementType;
		this.propertyName = propertyName;
		if (annotations != null)
		{
			annotationMap = new HashMap<Class<?>, Annotation>();
			for (Annotation annotation : annotations)
			{
				annotationMap.put(annotation.annotationType(), annotation);
			}
		}
		else
		{
			annotationMap = null;
		}
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
	public String getName()
	{
		return propertyName;
	}

	@Override
	public Class<?> getDeclaringType()
	{
		return declaringType;
	}

	@Override
	public Class<?> getRealType()
	{
		return realType;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends Annotation> V getAnnotation(Class<V> annotationType)
	{
		return (V) annotationMap.get(annotationType);
	}

	protected RuntimeException createException()
	{
		return new UnsupportedOperationException("This in an intermediate member which works only as a stub for a later bytecode-enhanced member");
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
	public boolean isTransient()
	{
		return isTransient;
	}

	@Override
	public void setTransient(boolean isTransient)
	{
		this.isTransient = isTransient;
	}

	@Override
	public Object getNullEquivalentValue()
	{
		throw createException();
	}

	@Override
	public boolean isToMany()
	{
		return Collection.class.isAssignableFrom(getRealType());
	}

	@Override
	public Class<?> getElementType()
	{
		return elementType;
	}

	@Override
	public Class<?> getEntityType()
	{
		return entityType;
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
