package com.koch.ambeth.ioc.typeinfo;

import java.lang.reflect.Field;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class FieldPropertyInfo extends AbstractPropertyInfo
{
	public FieldPropertyInfo(Class<?> entityType, String propertyName, Field field)
	{
		this(entityType, propertyName, field, null);
	}

	public FieldPropertyInfo(Class<?> entityType, String propertyName, Field field, IThreadLocalObjectCollector objectCollector)
	{
		super(entityType, objectCollector);
		field.setAccessible(true);
		backingField = field;
		modifiers = field.getModifiers();
		name = propertyName;
		declaringType = field.getDeclaringClass();
		propertyType = field.getType();
		elementType = TypeInfoItemUtil.getElementTypeUsingReflection(propertyType, field.getGenericType());
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
		return true;
	}

	@Override
	public boolean isWritable()
	{
		return true;
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
