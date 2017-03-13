package com.koch.ambeth.merge.orm;

public interface IOrmConfigGroup
{
	Iterable<IEntityConfig> getLocalEntityConfigs();

	Iterable<IEntityConfig> getExternalEntityConfigs();
}
