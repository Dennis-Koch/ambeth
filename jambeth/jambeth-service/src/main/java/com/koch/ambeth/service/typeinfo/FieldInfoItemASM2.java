package com.koch.ambeth.service.typeinfo;

import java.lang.reflect.Field;

import com.koch.ambeth.ioc.accessor.AbstractAccessor;

public class FieldInfoItemASM2 extends FieldInfoItem
{
	protected final AbstractAccessor accessor;

	public FieldInfoItemASM2(Field field, AbstractAccessor accessor)
	{
		this(field, true, accessor);
	}

	public FieldInfoItemASM2(Field field, boolean allowNullEquivalentValue, AbstractAccessor accessor)
	{
		this(field, allowNullEquivalentValue, field.getName(), accessor);
	}

	public FieldInfoItemASM2(Field field, String propertyName, AbstractAccessor accessor)
	{
		this(field, true, propertyName, accessor);
	}

	public FieldInfoItemASM2(Field field, boolean allowNullEquivalentValue, String propertyName, AbstractAccessor accessor)
	{
		super(field, allowNullEquivalentValue, propertyName);
		this.accessor = accessor;
	}

	@Override
	public Object getValue(Object obj, boolean allowNullEquivalentValue)
	{
		Object value = accessor.getValue(obj);
		Object nullEquivalentValue = this.nullEquivalentValue;
		if (nullEquivalentValue != null && nullEquivalentValue.equals(value))
		{
			if (allowNullEquivalentValue)
			{
				return nullEquivalentValue;
			}
			return null;
		}
		return value;
	}

	@Override
	public void setValue(Object obj, Object value)
	{
		if (value == null && allowNullEquivalentValue)
		{
			value = nullEquivalentValue;
		}
		accessor.setValue(obj, value);
	}
}
