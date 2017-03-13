package com.koch.ambeth.merge.orm;

public interface IOrmEntityTypeProvider
{
	Class<?> resolveEntityType(String entityTypeName);
}
