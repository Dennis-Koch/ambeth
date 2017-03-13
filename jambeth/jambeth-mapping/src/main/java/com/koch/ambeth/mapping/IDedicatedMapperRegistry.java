package com.koch.ambeth.mapping;

public interface IDedicatedMapperRegistry
{
	IDedicatedMapper getDedicatedMapper(Class<?> entityType);
}
