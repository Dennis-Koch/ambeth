package de.osthus.ambeth.orm;

public interface IOrmConfigGroup
{
	Iterable<IEntityConfig> getLocalEntityConfigs();

	Iterable<IEntityConfig> getExternalEntityConfigs();
}
