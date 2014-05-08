package de.osthus.ambeth.util;

public interface IDedicatedConverterExtendable
{
	void registerDedicatedConverter(IDedicatedConverter dedicatedConverter, Class<?> sourceType, Class<?> targetType);

	void unregisterDedicatedConverter(IDedicatedConverter dedicatedConverter, Class<?> sourceType, Class<?> targetType);
}