package com.koch.ambeth.bytecode;

public class NoOpValueResolveDelegate implements IValueResolveDelegate
{
	private final Object value;

	public NoOpValueResolveDelegate(Object value)
	{
		this.value = value;
	}

	@Override
	public Class<?> getValueType()
	{
		return value.getClass();
	}

	@Override
	public Object invoke(String fieldName, Class<?> enhancedType)
	{
		return value;
	}
}
