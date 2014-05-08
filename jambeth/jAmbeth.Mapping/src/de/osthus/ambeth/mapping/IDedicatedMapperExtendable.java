package de.osthus.ambeth.mapping;

public interface IDedicatedMapperExtendable
{
	void registerDedicatedMapper(IDedicatedMapper dedicatedMapper, Class<?> entityType);

	void unregisterDedicatedMapper(IDedicatedMapper dedicatedMapper, Class<?> entityType);
}
