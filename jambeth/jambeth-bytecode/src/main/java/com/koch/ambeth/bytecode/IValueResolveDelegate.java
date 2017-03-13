package com.koch.ambeth.bytecode;

public interface IValueResolveDelegate
{
	Class<?> getValueType();

	Object invoke(String fieldName, Class<?> enhancedType);
}
