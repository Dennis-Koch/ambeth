package de.osthus.ambeth.persistence.blueprint;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.orm.blueprint.IEntityTypeBlueprint;
import de.osthus.ambeth.orm.blueprint.IOrmBlueprintProvider;

public class SQLOrmBlueprintProvider implements IOrmBlueprintProvider, IInitializingBean
{
	@Autowired
	protected ICache cache;

	@Override
	public void afterPropertiesSet() throws Throwable
	{

	}

	@Override
	public IEntityTypeBlueprint resolveEntityTypeBlueprint(String entityTypeName)
	{
		return cache.getObject(IEntityTypeBlueprint.class, IEntityTypeBlueprint.NAME, entityTypeName);
	}

}
