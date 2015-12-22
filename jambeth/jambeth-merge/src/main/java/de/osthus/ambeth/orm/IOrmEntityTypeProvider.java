package de.osthus.ambeth.orm;

public interface IOrmEntityTypeProvider
{
	Class<?> resolveEntityType(String entityTypeName);
}
