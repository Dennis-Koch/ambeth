package de.osthus.ambeth.typeinfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;

public class FieldPropertyInfo extends AbstractPropertyInfo
{
	protected final boolean writable;

	public FieldPropertyInfo(Class<?> entityType, String propertyName, Field field)
	{
		this(entityType, propertyName, field, null);
	}

	public FieldPropertyInfo(Class<?> entityType, String propertyName, Field field, IThreadLocalObjectCollector objectCollector)
	{
		super(entityType, objectCollector);
		this.backingField = field;
		this.name = propertyName;
		this.declaringType = field.getDeclaringClass();
		this.propertyType = field.getType();
		this.elementType = TypeInfoItemUtil.getElementTypeUsingReflection(propertyType, field.getGenericType());
		writable = Modifier.isPublic(field.getModifiers()) || Modifier.isProtected(field.getModifiers());
		init(objectCollector);
	}

	@Override
	protected void init(IThreadLocalObjectCollector objectCollector)
	{
		putAnnotations(backingField);
		super.init(objectCollector);
	}

	@Override
	public boolean isReadable()
	{
		return writable;
	}

	@Override
	public boolean isWritable()
	{
		return writable;
	}

	@Override
	public Object getValue(Object obj)
	{
		try
		{
			return backingField.get(obj);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void setValue(Object obj, Object value)
	{
		try
		{
			backingField.set(obj, value);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
