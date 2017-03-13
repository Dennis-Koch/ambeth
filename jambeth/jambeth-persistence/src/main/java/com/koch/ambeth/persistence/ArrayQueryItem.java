package com.koch.ambeth.persistence;

public class ArrayQueryItem
{
	private final Object values;

	private final Class<?> fieldType;

	public ArrayQueryItem(Object values, Class<?> fieldType)
	{
		this.values = values;
		this.fieldType = fieldType;
	}

	public Object getValues()
	{
		return values;
	}

	public Class<?> getFieldType()
	{
		return fieldType;
	}
}
