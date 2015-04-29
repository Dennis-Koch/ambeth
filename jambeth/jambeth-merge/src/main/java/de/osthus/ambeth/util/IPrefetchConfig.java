package de.osthus.ambeth.util;

public interface IPrefetchConfig
{
	IPrefetchConfig add(Class<?> entityType, String propertyPath);

	IPrefetchConfig add(Class<?> entityType, String... propertyPaths);

	IPrefetchHandle build();
}
