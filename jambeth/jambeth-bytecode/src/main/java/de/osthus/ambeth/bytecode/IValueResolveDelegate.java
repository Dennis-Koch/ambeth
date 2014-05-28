package de.osthus.ambeth.bytecode;

public interface IValueResolveDelegate
{
	Class<?> getValueType();

	Object invoke(String fieldName, Class<?> enhancedType);
}
