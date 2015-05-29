package de.osthus.ambeth.orm;

import de.osthus.ambeth.collections.ISet;

public class OrmConfigGroup implements IOrmConfigGroup
{
	protected final ISet<IEntityConfig> localEntityConfigs;

	protected final ISet<IEntityConfig> externalEntityConfigs;

	public OrmConfigGroup(ISet<IEntityConfig> localEntityConfigs, ISet<IEntityConfig> externalEntityConfigs)
	{
		this.localEntityConfigs = localEntityConfigs;
		this.externalEntityConfigs = externalEntityConfigs;
	}

	@Override
	public Iterable<IEntityConfig> getExternalEntityConfigs()
	{
		return externalEntityConfigs;
	}

	@Override
	public Iterable<IEntityConfig> getLocalEntityConfigs()
	{
		return localEntityConfigs;
	}
}
