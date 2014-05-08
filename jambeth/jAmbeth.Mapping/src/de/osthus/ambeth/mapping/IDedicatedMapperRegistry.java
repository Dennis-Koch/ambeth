package de.osthus.ambeth.mapping;

public interface IDedicatedMapperRegistry
{
	IDedicatedMapper getDedicatedMapper(Class<?> entityType);
}
