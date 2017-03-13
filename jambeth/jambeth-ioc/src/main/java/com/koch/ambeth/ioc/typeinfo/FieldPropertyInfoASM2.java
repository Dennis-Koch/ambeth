package com.koch.ambeth.ioc.typeinfo;

import java.lang.reflect.Field;

import com.koch.ambeth.ioc.accessor.AbstractAccessor;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class FieldPropertyInfoASM2 extends FieldPropertyInfo
{
	protected final AbstractAccessor accessor;

	public FieldPropertyInfoASM2(Class<?> entityType, String propertyName, Field field, IThreadLocalObjectCollector objectCollector, AbstractAccessor accessor)
	{
		super(entityType, propertyName, field, objectCollector);
		this.accessor = accessor;
	}

	@Override
	public void setValue(Object obj, Object value)
	{
		accessor.setValue(obj, value);
	}

	@Override
	public Object getValue(Object obj)
	{
		return accessor.getValue(obj);
	}
}
