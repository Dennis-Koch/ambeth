package de.osthus.ambeth.util;

public interface IPrefetchConfig
{
	IPrefetchConfig add(Class<?> entityType, String propertyPath);

	IPrefetchHandle build();
}
